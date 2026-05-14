package ar.edu.unsam.proyectosoftware.dto

data class UsuarioRegistradoDTO(
    val nombre: String,
    val email: String,
    val contrasena: String,
    val username: String
)

data class UsuarioLoginDTO(
    val email: String,
    val contrasena: String
)

data class UsuarioLogueadoDTO (
    val id: Long,
    val accessToken: String,
    val refreshToken: String,
    val nombre: String,
    val email: String,
    val username: String,
    val imagenUrl: String? = ""
)

data class UsuarioNombreIdDTO(
    val nombre: String,
    val id: Long,
    val username: String
)

data class UsuarioConBalanceDTO(
    val id: Long,
    val nombre: String,
    val username: String,
    val monto: Double,
)

data class UsuarioInfoDTO(
    val nombre: String,
    val username: String,
    val imagenUrl: String,
    val esAmigo: Boolean,
    val solicitudPendiente: Boolean,
    val gruposCompartidos: List<GrupoNombreIdDTO>,
    val balances: List<BalanceConGrupoDTO>
)

data class UsuarioEditarDTO(
    val nombre: String,
    val email: String,
    val imagenUrl: String?,
    val username: String? = null
)

data class CambioContrasenaDTO(
    val contrasenaActual: String,
    val contrasenaNueva: String
)

data class UsuarioBusquedaDTO(
    val id: Long,
    val nombre: String,
    val username: String,
    val imagenUrl: String
)
