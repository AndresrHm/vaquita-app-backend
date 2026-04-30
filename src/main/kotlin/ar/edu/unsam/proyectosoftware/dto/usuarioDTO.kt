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
    val imagenUrl: String? = ""
)

data class UsuarioNombreIdDTO(
    val nombre: String,
    val id: Long
)

data class UsuarioInfoDTO(
    val nombre: String,
    val imagenUrl: String,
    val esAmigo: Boolean,
    val solicitudPendiente: Boolean,
    val gruposCompartidos: List<GrupoNombreIdDTO>
)

data class UsuarioEditarDTO(
    val nombre: String,
    val email: String,
    val imagenUrl: String?
)

data class CambioContrasenaDTO(
    val contrasenaActual: String,
    val contrasenaNueva: String
)