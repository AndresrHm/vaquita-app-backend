package ar.edu.unsam.proyectosoftware.dto

import java.time.LocalDate
import java.util.Collections.emptyList

data class GastoDTO(
    val id: Long?,
    val descripcion: String,
    val categoria: String,
    val monto: Double,
    val fecha: LocalDate,
    val grupoID: Long,
    val pagadorID: Long,
    val items: List<ItemGastoDTO> = emptyList()
)

data class ItemGastoDTO(
    val nombre: String,
    val cantidad: Double,
    val precioUnitario: Double
)