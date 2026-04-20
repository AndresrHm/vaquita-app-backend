package ar.edu.unsam.proyectosoftware.dto

data class GrupoDTO(
    val id: Long,
    val nombre: String,
    val imagenUrl: String,
    val estadoGrupo: String, // TODO: Falta implementar
)

data class GrupoCreacionDTO(
    val nombre: String,
    val imagenUrl: String,
    val descripcion: String,
)

// Para /groups
data class GrupoNombreIdDTO(
    val id: Long,
    val nombre: String
)

// Para /groupDetail
data class GrupoDetalleDTO(
    val id: Long,
    val nombre: String,
    val descripcion: String,
    val imagenUrl: String,
    val cantidadIntegrantes: Long,
)

// DTOs para /groupSettings
data class GrupoSettingsDTO(
    val id: Long,
    val nombre: String,
    val descripcion: String,
    val imagenUrl: String,
    val moneda: String,
    val simplificarDeudas: Boolean,
    val notificaciones: Boolean,
    val modoRata: Boolean,
)

data class GrupoContextoUsuarioDTO (
    val usuarioId: Long,
    val esAdmin: Boolean,
    val tieneDeudas: Boolean,
)

data class MiembroDTO (
    val id: Long,
    val nombre: String,
    val rol: String,
    val imagenUrl: String,
)

data class AgregarMiembrosDTO(
    val usuarioIds: List<Long>
)

enum class RolMiembro(val descripcion: String) {
    ADMIN("Administrador"),
    MIEMBRO("Miembro"),
}