package ar.edu.unsam.proyectosoftware.dto
import ar.edu.unsam.proyectosoftware.domain.CategoriaGasto
import ar.edu.unsam.proyectosoftware.domain.ItemGasto
import ar.edu.unsam.proyectosoftware.domain.TipoGasto
import ar.edu.unsam.proyectosoftware.domain.Usuario
import java.time.LocalDate
import java.util.Collections.emptyList

data class CreacionGastoDTO(
    val id: Long?,
    val descripcion: String,
    val categoria: CategoriaGasto,
    val monto: Double,
    val fecha: LocalDate,
    val grupoID: Long,
    val pagadorID: Long,
    val participantesIDs: List<Long>,
    val items: List<ItemGastoDTO> = emptyList(),
    val tipoGasto: TipoGasto
)

data class ItemGastoDTO(
    val nombre: String,
    val cantidad: Double,
    val precioUnitario: Double
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
data class GastoDetalleDTO(
    val id: Long,
    val descripcion: String,
    val categoria: CategoriaGasto,
    val monto: Double,
    val fecha: LocalDate,
    val pagador: String,
    val participantes: List<Usuario>,
    val tipoGasto: TipoGasto
)
data class FilterGastosCustom(
    val vista: TipoRelacionVista? = null,
    val categoria: List<CategoriaGasto>? = null,
    val fechaInicio: LocalDate? = null,
    val fechaFin: LocalDate? = null,
    val amigoID: Long? = null,
    val search: String? = null,
    val rangoIni: Double? = null,
    val rangoFin: Double? = null,
    val page: Int = 0,
    val size: Int = 10,
)

enum class TipoRelacionVista {
    PROPIA, GRUPO
}
