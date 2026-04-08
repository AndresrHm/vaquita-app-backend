package ar.edu.unsam.proyectosoftware.dto

data class UsuarioRegistradoDTO(
    val nombre: String,
    val email: String,
    val contrasena: String
)

data class UsuarioNombreIdDTO(
    val nombre: String,
    val id: Long
)