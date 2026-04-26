package ar.edu.unsam.proyectosoftware.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class ReceiptScanResultDto(
    @JsonProperty("title")
    val title: String,

    @JsonProperty("description")
    val description: String? = null,

    @JsonProperty("totalAmount")
    val totalAmount: Double,

    @JsonProperty("currency")
    val currency: String = "ARS",

    @JsonProperty("date")
    val date: String,

    @JsonProperty("category")
    val category: String,

    @JsonProperty("items")
    val items: List<ReceiptItemDto>? = null
)

data class ReceiptItemDto(
    @JsonProperty("name")
    val name: String,

    @JsonProperty("quantity")
    val quantity: Int? = 1,

    @JsonProperty("price")
    val price: Double
)