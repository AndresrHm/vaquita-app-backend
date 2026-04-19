package ar.edu.unsam.proyectosoftware.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class ReceiptScanResultDto(
    @JsonProperty("title")
    val title: String,                          // requerido

    @JsonProperty("totalAmount")
    val totalAmount: Double,                    // requerido

    @JsonProperty("date")
    val date: String? = null,                   // opcional, formato "YYYY-MM-DD"

    @JsonProperty("category")
    val category: String? = null,               // opcional: food | transport | entertainment | other

    @JsonProperty("items")
    val items: List<ReceiptItemDto> = emptyList() // opcional
)

data class ReceiptItemDto(
    @JsonProperty("name")
    val name: String,

    @JsonProperty("quantity")
    val quantity: Int = 1,

    @JsonProperty("price")
    val price: Double
)