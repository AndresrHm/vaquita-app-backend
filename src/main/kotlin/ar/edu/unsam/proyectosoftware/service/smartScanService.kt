package ar.edu.unsam.proyectosoftware.service

import ar.edu.unsam.proyectosoftware.dto.ReceiptItemDto
import ar.edu.unsam.proyectosoftware.dto.ReceiptScanResultDto
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.util.Base64

// ============================================================================
// Enums
// ============================================================================

enum class Moneda(val codigo: String, val simbolo: String) {
    ARS("ARS", "$"),
    USD("USD", "$"),
    EUR("EUR", "€"),
    UYU("UYU", "$"),
    BRL("BRL", "R$"),
    CLP("CLP", "$"),
    COP("COP", "$")
}

@Service
class SmartScanService(
    private val objectMapper: ObjectMapper,
    @Value("\${gemini.api.key}") private val apiKey: String,
    @Value("\${gemini.api.url}") private val apiUrl: String,
    @Value("\${gemini.model}") private val model: String
) {

    private val webClient = WebClient.builder()
        .baseUrl(apiUrl)
        .defaultHeader("Content-Type", "application/json")
        .build()

    fun scanTestReceipt(): ReceiptScanResultDto {
        val imageBase64 = getHardcodedReceiptBase64()
        val payload = buildGeminiPayload(imageBase64)
        val responseJson = callGeminiApi(payload)
        return parseGeminiResponse(responseJson)
    }

    fun scanReceipt(imageBase64: String, mimeType: String = "image/jpeg"): ReceiptScanResultDto {
        val payload = buildGeminiPayload(imageBase64, mimeType)
        val responseJson = callGeminiApi(payload)
        return parseGeminiResponse(responseJson)
    }

    // ========================================================================
    // Imagen hardcodeada: URL pública de un ticket de prueba
    // Podés reemplazar esto por un Base64 real de un ticket argentino
    // ========================================================================
    private fun getHardcodedReceiptBase64(): String {
        // Opción A: descargás la imagen de una URL pública y la convertís a base64
        val imageBytes = WebClient.create()
            .get()
            .uri("https://media-cdn.tripadvisor.com/media/photo-s/10/14/90/97/ticket-comprobante.jpg")
            // 👆 Reemplazá esta URL por una imagen real de ticket cuando tengas una
            .retrieve()
            .bodyToMono<ByteArray>()
            .block()!!
        return Base64.getEncoder().encodeToString(imageBytes)
    }

    // ========================================================================
    // Construcción del payload con Structured Output (responseSchema)
    // ========================================================================
    private fun buildGeminiPayload(
        imageBase64: String,
        mimeType: String = "image/jpeg"
    ): Map<String, Any> {
        return mapOf(
            "system_instruction" to mapOf(
                "parts" to listOf(
                    mapOf("text" to buildSystemPrompt())
                )
            ),
            "contents" to listOf(
                mapOf(
                    "parts" to listOf(
                        mapOf(
                            "inline_data" to mapOf(
                                "mime_type" to mimeType,
                                "data" to imageBase64
                            )
                        ),
                        mapOf(
                            "text" to "Extraé todos los datos de este ticket/factura. " +
                                    "Si no podés determinar un campo, omitilo o dejalo en null. " +
                                    "Las fechas deben estar en formato YYYY-MM-DD. " +
                                    "Intenta inferir una descripción detallada del gasto."
                        )
                    )
                )
            ),
            "generationConfig" to mapOf(
                "temperature" to 0,
                "responseMimeType" to "application/json",
                "responseSchema" to buildResponseSchema()
            )
        )
    }

    // ========================================================================
    // Construcción dinámica del prompt del sistema basado en enums
    // ========================================================================
    private fun buildSystemPrompt(): String {
        val categoriasStr = CategoriaGasto.entries
            .joinToString(", ") { it.name }

        val monedasStr = Moneda.entries
            .joinToString(", ") { "${it.codigo} (${it.simbolo})" }

        return """
            Eres un asistente especializado en extraer datos de tickets y facturas.
            Analizás la imagen y extraés la información estructurada del comprobante.
            
            Sigue estas reglas estrictamente:
            
            1. CATEGORÍAS válidas: $categoriasStr
            2. MONEDAS soportadas: $monedasStr
            3. Las fechas deben estar en formato YYYY-MM-DD. Si no hay fecha, usa null.
            4. El campo "title" es el nombre del comercio o establecimiento (REQUERIDO)
            5. El campo "description" es un resumen detallado de qué se compró (OPCIONAL)
            6. Para "currency", detectá la moneda del ticket. Si es ambigua, intenta inferir por contexto regional.
            7. Si no podés determinar un campo opcional, omitilo o dejalo en null.
            8. "items" es un array con cada producto/servicio. Solo incluilo si podés extraer datos.
        """.trimIndent()
    }

    // ========================================================================
    // Schema JSON para Structured Output — Gemini lo respeta estrictamente
    // Se construye dinámicamente basado en los enums reales de la app
    // ========================================================================
    private fun buildResponseSchema(): Map<String, Any> {
        val categoriasEnum = CategoriaGasto.entries.map { it.name }
        val monedasEnum = Moneda.entries.map { it.codigo }

        return mapOf(
            "type" to "OBJECT",
            "properties" to mapOf(
                "title" to mapOf(
                    "type" to "STRING",
                    "description" to "Nombre del comercio, establecimiento o descripción breve del lugar"
                ),
                "description" to mapOf(
                    "type" to "STRING",
                    "description" to "Descripción detallada de qué se compró o el motivo del gasto"
                ),
                "totalAmount" to mapOf(
                    "type" to "NUMBER",
                    "description" to "Monto total del ticket (solo el número, sin símbolo de moneda)"
                ),
                "currency" to mapOf(
                    "type" to "STRING",
                    "enum" to monedasEnum,
                    "description" to "Código ISO de la moneda (ARS, USD, EUR, etc.)"
                ),
                "date" to mapOf(
                    "type" to "STRING",
                    "description" to "Fecha del comprobante en formato YYYY-MM-DD"
                ),
                "category" to mapOf(
                    "type" to "STRING",
                    "enum" to categoriasEnum,
                    "description" to "Categoría del gasto según las definidas en la app"
                ),
                "items" to mapOf(
                    "type" to "ARRAY",
                    "items" to mapOf(
                        "type" to "OBJECT",
                        "properties" to mapOf(
                            "name" to mapOf(
                                "type" to "STRING",
                                "description" to "Nombre del producto o servicio"
                            ),
                            "quantity" to mapOf(
                                "type" to "INTEGER",
                                "description" to "Cantidad comprada (opcional)"
                            ),
                            "price" to mapOf(
                                "type" to "NUMBER",
                                "description" to "Precio unitario o total del item"
                            )
                        ),
                        "required" to listOf("name", "price")
                    ),
                    "description" to "Desglose de items comprados"
                )
            ),
            "required" to listOf("title", "totalAmount", "currency", "date", "category")
        )
    }

    // ========================================================================
    // Llamada a la API de Gemini con WebClient
    // ========================================================================
    private fun callGeminiApi(payload: Map<String, Any>): String {
        val endpoint = "/models/$model:generateContent?key=$apiKey"

        return webClient.post()
            .uri(endpoint)
            .bodyValue(payload)
            .retrieve()
            .onStatus({ it.is4xxClientError || it.is5xxServerError }) { response ->
                response.bodyToMono<String>().flatMap { errorBody ->
                    println("=== ERROR DE GEMINI ===")
                    println(errorBody)
                    println("======================")
                    reactor.core.publisher.Mono.error(RuntimeException("Gemini error: $errorBody"))
                }
            }
            .bodyToMono<Map<String, Any>>()
            .block()
            ?.let { response ->
                @Suppress("UNCHECKED_CAST")
                val candidates = response["candidates"] as? List<Map<String, Any>>
                val content = candidates?.firstOrNull()?.get("content") as? Map<*, *>
                val parts = content?.get("parts") as? List<Map<String, Any>>
                parts?.firstOrNull()?.get("text") as? String
            }
            ?: throw RuntimeException("Respuesta vacía o inesperada de Gemini")
    }

    // ========================================================================
    // Parseo del JSON estructurado al DTO
    // Con manejo de enums y valores por defecto
    // ========================================================================
    private fun parseGeminiResponse(json: String): ReceiptScanResultDto {
        val parsed = objectMapper.readValue(json, Map::class.java)

        @Suppress("UNCHECKED_CAST")
        return ReceiptScanResultDto(
            title = parsed["title"] as String,
            description = parsed["description"] as? String,
            totalAmount = (parsed["totalAmount"] as? Number)?.toDouble() ?: 0.0,
            currency = parsed["currency"] as? String ?: "ARS",
            date = parsed["date"] as? String ?: "",
            category = mapearCategoria(parsed["category"] as? String),
            items = mapearItems(parsed["items"] as? List<Map<String, Any>>)
        )
    }

    /**
     * Mapea el nombre de la categoría desde Gemini al formato esperado por el DTO
     * Gemini devuelve nombres de enum como "COMIDA", "TRANSPORTE", etc.
     * El DTO espera strings simplificados como "food", "transport", etc.
     */
    private fun mapearCategoria(categoriaStr: String?): String {
        if (categoriaStr == null) return "otros"

        return try {
            val categoria = CategoriaGasto.valueOf(categoriaStr.uppercase())
            when (categoria) {
                CategoriaGasto.COMIDA -> "food"
                CategoriaGasto.BEBIDA -> "food"
                CategoriaGasto.TRANSPORTE -> "transport"
                CategoriaGasto.ENTRETENIMIENTO -> "entertainment"
                CategoriaGasto.HOGAR, CategoriaGasto.SUPERMERCADO,
                CategoriaGasto.VARIOS, CategoriaGasto.OTROS -> "other"
            }
        } catch (e: IllegalArgumentException) {
            "other"
        }
    }

    /**
     * Mapea los items desde la respuesta de Gemini al formato del DTO
     */
    private fun mapearItems(itemsRaw: List<Map<String, Any>>?): List<ReceiptItemDto>? {
        if (itemsRaw == null || itemsRaw.isEmpty()) return null

        return itemsRaw.mapNotNull { item ->
            try {
                ReceiptItemDto(
                    name = item["name"] as? String ?: return@mapNotNull null,
                    quantity = (item["quantity"] as? Number)?.toInt(),
                    price = (item["price"] as? Number)?.toDouble() ?: return@mapNotNull null
                )
            } catch (e: Exception) {
                null
            }
        }.ifEmpty { null }
    }
}