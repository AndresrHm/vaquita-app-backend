package ar.edu.unsam.proyectosoftware.dto

import ar.edu.unsam.proyectosoftware.service.GrupoService

data class GrupoDTO(
    val id: Long,
    val nombre: String,
    val imagenUrl: String,
    val estadoGrupo: String = "Pendiente de pago" // hardcodeado
    // TODO: Mandar estado actual del grupo (ej. pendiente de pago)
)

data class GrupoCreacionDTO(
    val nombre: String,
    val imagenUrl: String = "",
    val descripcion: String = "",
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
    val moneda: String, // TODO: Hardcodeado
    val simplificarDeudas: Boolean = true, // TODO: Hardcodeado
    val notificaciones: Boolean = false, // TODO: Hardcodeado
)

data class GrupoContextoUsuarioDTO (
    val usuarioId: Long,
    val esAdmin: Boolean = false, // TODO: Hardcodeado
    val tieneDeudas: Boolean = true, // TODO: Hardcodeado
)

data class MiembroDTO (
    val id: Long,
    val nombre: String,
    val rol: RolMiembro = RolMiembro.MIEMBRO, // TODO: Hardcodeado
    val imagenUrl: String,
)

enum class RolMiembro {
    ADMIN,
    MIEMBRO
}