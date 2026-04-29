package ar.edu.unsam.proyectosoftware.dto

import java.time.LocalDateTime

enum class TipoActividad {
    CUENTA_CREADA,
    GASTO_DEUDA, // Los gastos son cuando tu le debes a otra persona
    GASTO_SALDADO,
    DEUDA_GENERADA, // Cuando otras personas te deben a ti
    DEUDA_SALDADA,
    INGRESO_GRUPO,
    EGRESO_GRUPO,
}

data class ActividadDTO(
    val id: Long,
    val tipo: TipoActividad,
    val fecha: LocalDateTime,
    val descripcion: String?,
    val metadata: String?,
)

data class FilterActividadDTO(
    val tipo: TipoActividad? = null,
    val desde: LocalDateTime? = null,
    val hasta: LocalDateTime? = null,
    val page: Int = 0,
    val size: Int = 6,
)
