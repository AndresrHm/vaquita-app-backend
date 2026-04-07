package ar.edu.unsam.proyectosoftware.dto

data class GrupoDTO(
    val id: Long,
    val nombre: String,
    val imagen: String,
    // TODO: Mandar estado actual del grupo (ej. pendiente de pago)
)

data class GroupPageResponse(
    val content: List<GrupoDTO>,
    val nextCursor: String?,
    val hasMore: Boolean
)