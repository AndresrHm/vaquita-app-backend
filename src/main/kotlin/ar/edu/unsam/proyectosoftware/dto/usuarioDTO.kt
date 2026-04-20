package ar.edu.unsam.proyectosoftware.dto

data class UsuarioRegistradoDTO(
    val nombre: String,
    val email: String,
    val contrasena: String
)

data class UsuarioLoginDTO(
    val email: String,
    val contrasena: String
)

data class UsuarioLogueadoDTO (
    val id: Long,
    val token: String,
    val nombre: String,
    val email: String,
)

data class UsuarioNombreIdDTO(
    val nombre: String,
    val id: Long
)

data class UsuarioInfoDTO(
    val nombre: String,
    val imagenUrl: String,
    val esAmigo: Boolean
)