package ar.edu.unsam.proyectosoftware.dto

import java.time.LocalDate

data class RangoDTO(val desde: LocalDate, val hasta: LocalDate)

// Actividad analytics
data class ActividadPorTipoDTO(val tipo: String, val cantidad: Long)
data class ActividadPorDiaDTO(val fecha: String, val cantidad: Long)
data class ActividadPorGrupoDTO(val grupoId: Long, val grupoNombre: String, val cantidad: Long)

data class ActividadAnalyticsResponse(
    val rango: RangoDTO,
    val resumen: Map<String, Any>,
    val porTipo: List<ActividadPorTipoDTO>,
    val porDia: List<ActividadPorDiaDTO>,
    val porGrupo: List<ActividadPorGrupoDTO>
)

// Gasto analytics (mis gastos)
data class GastoPorMesDTO(val mes: String, val total: Double)
data class GastoPorCategoriaDTO(val categoria: String, val total: Double)
data class GastoPorGrupoDTO(val grupoId: Long, val grupoNombre: String, val total: Double)
data class GastoPorPagadorDTO(val pagadorId: Long, val pagadorNombre: String, val total: Double)

data class GastoAnalyticsResponse(
    val rango: RangoDTO,
    val resumen: Map<String, Any>,
    val porMes: List<GastoPorMesDTO>,
    val porCategoria: List<GastoPorCategoriaDTO>,
    val porGrupo: List<GastoPorGrupoDTO>,
    val porPagador: List<GastoPorPagadorDTO>
)

// Grupo analytics
data class BalanceUsuarioDTO(val usuarioId: Long, val usuarioNombre: String, val saldo: Double)

data class GrupoAnalyticsResponse(
    val grupoId: Long,
    val grupoNombre: String,
    val rango: RangoDTO,
    val resumen: Map<String, Any>,
    val porMes: List<GastoPorMesDTO>,
    val porCategoria: List<GastoPorCategoriaDTO>,
    val porPagador: List<GastoPorPagadorDTO>,
    val balances: List<BalanceUsuarioDTO>
)

