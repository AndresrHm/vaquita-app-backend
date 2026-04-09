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
    val participantesIDs: List<Long>,
    val items: List<ItemGastoDTO> = emptyList()
)

data class ItemGastoDTO(
    val nombre: String,
    val cantidad: Double,
    val precioUnitario: Double
)

data class GrupoDetalleDTO(
    val id: Long,
    val name: String,
    val status: String,
    val outstanding: Double,
    val currency: String,
    val imageUrl: String,
    val expensesByYear: List<GastosPorAnioDTO>
)

data class GastosPorAnioDTO(
    val year: Int,
    val months: List<GastosPorMesDTO>
)

data class GastosPorMesDTO(
    val month: String,
    val expenses: List<GastoItemDTO>
)

data class GastoItemDTO(
    val id: Long,
    val day: Int,
    val title: String,
    val amount: Double,
    val currency: String,
    val paymentType: String,
    val imageUrl: String
)