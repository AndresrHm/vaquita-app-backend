package ar.edu.unsam.proyectosoftware.dto

import java.math.BigDecimal

// Lo que devolvemos al frontend
// Cambiamos los campos `from` y `to` a String para permitir monedas arbitrarias
// (ej. cuando el scanner detecta una moneda fuera de ARS/USD/EUR)
data class ConversionResult(
    val from: String,
    val to: String,
    val originalAmount: BigDecimal,
    val convertedAmount: BigDecimal,
    val exchangeRate: BigDecimal
)

// Lo que leemos de DolarAPI
data class DolarApiResponse(val venta: BigDecimal)

// Lo que leemos de Frankfurter
data class FrankfurterResponse(val rates: Map<String, BigDecimal>)

// Lo que leemos de open.er-api como fallback genérico
data class OpenErApiResponse(
    val result: String? = null,
    val rates: Map<String, BigDecimal> = emptyMap()
)
