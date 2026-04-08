package ar.edu.unsam.proyectosoftware.dto

data class GrupoDTO(
    val id: Long,
    val nombre: String,
    val imagen: String,
    val estadoGrupo: String = "Pendiente de pago" // hardcodeado
    // TODO: Mandar estado actual del grupo (ej. pendiente de pago)
)

data class GrupoCreacionDTO(
    val nombre: String,
    val imagen: String = "",
    val descripcion: String = "",
)

data class GrupoNombreIdDTO(
    val id: Long,
    val nombre: String
)