package ar.edu.unsam.proyectosoftware.dto

class PagoDTO(
    val gastoId: Long,
    val monto: Double
)

class PagoGlobalDTO(
    val acreedorId: Long,
    val monto: Double
)

class PagoDeudaGrupoDTO(
    val acreedorId: Long,
    val monto: Double,
    val grupoId: Long
)
