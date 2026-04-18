package ar.edu.unsam.proyectosoftware.dto

data class BalanceDTO(
    val deudorId: Long,
    val deudorNombre: String,
    val acreedorId: Long,
    val acreedorNombre: String,
    val monto: Double
)