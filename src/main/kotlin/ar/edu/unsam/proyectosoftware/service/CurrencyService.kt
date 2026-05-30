package ar.edu.unsam.proyectosoftware.service


import ar.edu.unsam.proyectosoftware.domain.enums.SupportedCurrency
import ar.edu.unsam.proyectosoftware.dto.ConversionResult
import ar.edu.unsam.proyectosoftware.dto.DolarApiResponse
import ar.edu.unsam.proyectosoftware.dto.FrankfurterResponse
import ar.edu.unsam.proyectosoftware.dto.OpenErApiResponse
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class CurrencyService {
    private val restClient = RestClient.create()

    companion object {
        private val ONE = BigDecimal.ONE
        private const val RATE_SCALE = 8
        private const val AMOUNT_SCALE = 2
    }

    fun getExchangeRate(from: SupportedCurrency, to: SupportedCurrency): BigDecimal {
        return getExchangeRate(from.name, to.name)
    }

    fun getExchangeRate(fromCode: String, toCode: String): BigDecimal {
        val from = fromCode.uppercase()
        val to = toCode.uppercase()

        if (from == to) return ONE

        return try {
            // Caso 1: USD a ARS (Usamos Dólar Blue)
            if (from == "USD" && to == "ARS") {
                val response = restClient.get().uri("https://dolarapi.com/v1/dolares/blue")
                    .retrieve().body(DolarApiResponse::class.java)
                return (response?.venta ?: ONE).setScale(RATE_SCALE, RoundingMode.HALF_UP)
            }
            // Caso 2: ARS a USD
            if (from == "ARS" && to == "USD") {
                val response = restClient.get().uri("https://dolarapi.com/v1/dolares/blue")
                    .retrieve().body(DolarApiResponse::class.java)
                val venta = response?.venta ?: return ONE
                return ONE.divide(venta, RATE_SCALE, RoundingMode.HALF_UP)
            }
            // Caso 3: Involucra EUR u otros (Usamos Frankfurter)
            // Si el destino es ARS, preferimos pasar todo vía USD y luego usar Dólar Blue
            if (to == "ARS") {
                // Si ya venimos en USD, usamos Dólar Blue directo
                if (from == "USD") {
                    val response = restClient.get().uri("https://dolarapi.com/v1/dolares/blue")
                        .retrieve().body(DolarApiResponse::class.java)
                    return (response?.venta ?: ONE).setScale(RATE_SCALE, RoundingMode.HALF_UP)
                }

                // Caso general: convertimos from -> USD con Frankfurter o open.er-api,
                // luego USD -> ARS usando Dólar Blue
                val rateFromToUsd = getRateToUsd(from)
                if (rateFromToUsd != ONE) {
                    val usdToArs = getExchangeRate("USD", "ARS") // usa Dólar Blue
                    return rateFromToUsd.multiply(usdToArs).setScale(RATE_SCALE, RoundingMode.HALF_UP)
                }
            }

            // Caso ARS -> USD (invertir Dólar Blue)
            if (from == "ARS" && to == "USD") {
                val response = restClient.get().uri("https://dolarapi.com/v1/dolares/blue")
                    .retrieve().body(DolarApiResponse::class.java)
                val venta = response?.venta ?: return ONE
                return ONE.divide(venta, RATE_SCALE, RoundingMode.HALF_UP)
            }

            // Intentamos obtener tasa directa con Frankfurter
            val response = restClient.get()
                .uri("https://api.frankfurter.app/latest?from=$from&to=$to")
                .retrieve().body(FrankfurterResponse::class.java)
            val direct = (response?.rates?.get(to) ?: ONE).setScale(RATE_SCALE, RoundingMode.HALF_UP)
            if (direct != ONE) return direct

            // Fallback genérico: open.er-api directo desde cualquier moneda
            val openErApiDirect = restClient.get()
                .uri("https://open.er-api.com/v6/latest/$from")
                .retrieve().body(OpenErApiResponse::class.java)
            val openDirect = (openErApiDirect?.rates?.get(to) ?: ONE).setScale(RATE_SCALE, RoundingMode.HALF_UP)
            if (openDirect != ONE) return openDirect

            // Si no hay tasa directa, intentamos una conversión vía USD (hub)
            if (from != "USD" && to != "USD") {
                val viaUsdFrom = try { getExchangeRate(from, "USD") } catch (e: Exception) { ONE }
                val viaUsdTo = try { getExchangeRate("USD", to) } catch (e: Exception) { ONE }
                if (viaUsdFrom != ONE && viaUsdTo != ONE) {
                    return viaUsdFrom.multiply(viaUsdTo).setScale(RATE_SCALE, RoundingMode.HALF_UP)
                }
            }

            // Si sigue sin resultado, intentamos vía EUR
            if (from != "EUR" && to != "EUR") {
                val viaEurFrom = try { getExchangeRate(from, "EUR") } catch (e: Exception) { ONE }
                val viaEurTo = try { getExchangeRate("EUR", to) } catch (e: Exception) { ONE }
                if (viaEurFrom != ONE && viaEurTo != ONE) {
                    return viaEurFrom.multiply(viaEurTo).setScale(RATE_SCALE, RoundingMode.HALF_UP)
                }
            }

            ONE

        } catch (e: Exception) {
            // Si las APIs fallan, por defecto devolvemos 1.0 para no romper la app (o podrías lanzar una BusinessException)
            ONE
        }
    }

    private fun getRateToUsd(from: String): BigDecimal {
        if (from == "USD") return ONE

        // 1) Frankfurter: from -> USD
        try {
            val frankfurter = restClient.get()
                .uri("https://api.frankfurter.app/latest?from=$from&to=USD")
                .retrieve().body(FrankfurterResponse::class.java)
            val frankfurterRate = frankfurter?.rates?.get("USD")
            if (frankfurterRate != null && frankfurterRate.signum() > 0) return frankfurterRate.setScale(RATE_SCALE, RoundingMode.HALF_UP)
        } catch (_: Exception) {
            // seguimos con el fallback
        }

        // 2) open.er-api: base = from, rates[USD]
        return try {
            val openErApi = restClient.get()
                .uri("https://open.er-api.com/v6/latest/$from")
                .retrieve().body(OpenErApiResponse::class.java)
            val openRate = openErApi?.rates?.get("USD") ?: ONE
            openRate.setScale(RATE_SCALE, RoundingMode.HALF_UP)
        } catch (_: Exception) {
            ONE
        }
    }

    fun convertCurrency(from: SupportedCurrency, to: SupportedCurrency, amount: Double): ConversionResult {
        return convertCurrency(from.name, to.name, amount)
    }

    fun convertCurrency(fromCode: String, toCode: String, amount: Double): ConversionResult {
        return convertCurrency(fromCode, toCode, amount.toBigDecimal())
    }

    fun convertCurrency(fromCode: String, toCode: String, amount: BigDecimal): ConversionResult {
        val rate = getExchangeRate(fromCode, toCode)
        val from = fromCode.uppercase()
        val to = toCode.uppercase()
        val normalizedAmount = amount.setScale(AMOUNT_SCALE, RoundingMode.HALF_UP)
        val convertedAmount = normalizedAmount.multiply(rate).setScale(AMOUNT_SCALE, RoundingMode.HALF_UP)
        return ConversionResult(from, to, normalizedAmount, convertedAmount, rate)
    }
}

