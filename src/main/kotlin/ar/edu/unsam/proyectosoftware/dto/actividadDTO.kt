package ar.edu.unsam.proyectosoftware.dto

import java.time.LocalDateTime

enum class TipoActividad {
    MIS_DEUDAS, // Los gastos son cuando tú le debes a otra persona
    MIS_DEUDAS_SALDADAS,
    MIS_PAGOS, // Cuando otras personas te deben a ti
    COBROS_RECIBIDOS,
    INGRESO_GRUPO,
    EGRESO_GRUPO,
    CUENTA_CREADA,
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
    val search: String? = null,
)