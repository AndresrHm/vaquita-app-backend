package ar.edu.unsam.proyectosoftware.service


import ar.edu.unsam.proyectosoftware.dto.ReceiptScanResultDto
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.util.Base64

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

    // ------------------------------------------------------------
    // Imagen hardcodeada: URL pública de un ticket de prueba
    // Podés reemplazar esto por un Base64 real de un ticket argentino
    // ------------------------------------------------------------
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

    // ------------------------------------------------------------
    // Construcción del payload con Structured Output (responseSchema)
    // ------------------------------------------------------------
    private fun buildGeminiPayload(imageBase64: String, mimeType: String = "image/jpeg"): Map<String, Any> {
        return mapOf(
            "system_instruction" to mapOf(
                "parts" to listOf(
                    mapOf("text" to """
                        Eres un asistente especializado en extraer datos de tickets y facturas argentinas.
                        Analizás la imagen y extraés la información estructurada del comprobante.
                        Los precios siempre son en pesos argentinos (ARS).
                        Si no podés determinar un campo opcional, omitilo o dejalo en null.
                        Las fechas deben estar en formato YYYY-MM-DD.
                        La categoría debe ser una de: food, transport, entertainment, other.
                    """.trimIndent())
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
                            "text" to "Extraé todos los datos de este ticket/factura."
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

    // ------------------------------------------------------------
    // Schema JSON para Structured Output — Gemini lo respeta estrictamente
    // ------------------------------------------------------------
    private fun buildResponseSchema(): Map<String, Any> {
        return mapOf(
            "type" to "OBJECT",
            "properties" to mapOf(
                "title" to mapOf(
                    "type" to "STRING",
                    "description" to "Nombre del comercio o establecimiento"
                ),
                "totalAmount" to mapOf(
                    "type" to "NUMBER",
                    "description" to "Monto total del ticket en pesos argentinos"
                ),
                "date" to mapOf(
                    "type" to "STRING",
                    "description" to "Fecha del comprobante en formato YYYY-MM-DD"
                ),
                "category" to mapOf(
                    "type" to "STRING",
                    "enum" to listOf("food", "transport", "entertainment", "other"),
                    "description" to "Categoría del gasto"
                ),
                "items" to mapOf(
                    "type" to "ARRAY",
                    "items" to mapOf(
                        "type" to "OBJECT",
                        "properties" to mapOf(
                            "name" to mapOf("type" to "STRING"),
                            "quantity" to mapOf("type" to "INTEGER"),
                            "price" to mapOf("type" to "NUMBER")
                        ),
                        "required" to listOf("name", "price")
                    )
                )
            ),
            "required" to listOf("title", "totalAmount")
        )
    }

    // ------------------------------------------------------------
    // Llamada a la API de Gemini con WebClient
    // ------------------------------------------------------------
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
                val content = candidates?.firstOrNull()?.get("content") as? Map<String, Any>
                val parts = content?.get("parts") as? List<Map<String, Any>>
                parts?.firstOrNull()?.get("text") as? String
            }
            ?: throw RuntimeException("Respuesta vacía o inesperada de Gemini")
    }

    // ------------------------------------------------------------
    // Parseo del JSON estructurado al DTO
    // ------------------------------------------------------------
    private fun parseGeminiResponse(json: String): ReceiptScanResultDto {
        return objectMapper.readValue(json, ReceiptScanResultDto::class.java)
    }
}