package ar.edu.unsam.proyectosoftware.dto

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls

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
    @param:JsonAlias("integrantesIds")
    @param:JsonSetter(nulls = Nulls.AS_EMPTY)
    val usuarioIds: List<Long> = emptyList(),
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
    @param:JsonProperty("usuarioIds")
    @param:JsonAlias("integrantesIds")
    @param:JsonSetter(nulls = Nulls.AS_EMPTY)
    val usuarioIds: List<Long> = emptyList()
)

enum class RolMiembro(val descripcion: String) {
    ADMIN("Administrador"),
    MIEMBRO("Miembro"),
}