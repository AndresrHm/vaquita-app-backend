package ar.edu.unsam.proyectosoftware.mapper

import ar.edu.unsam.proyectosoftware.domain.Usuario
import ar.edu.unsam.proyectosoftware.dto.GrupoNombreIdDTO
import ar.edu.unsam.proyectosoftware.dto.UsuarioInfoDTO

object UsuarioMapper {
    fun toInfoDTO(usuarioBuscado: Usuario, esAmigo: Boolean, solicitudPendiente: Boolean, gruposCompartidos: List<GrupoNombreIdDTO>): UsuarioInfoDTO {
        return UsuarioInfoDTO(
            nombre = usuarioBuscado.nombre,
            imagenUrl = usuarioBuscado.imagenUrl,
            esAmigo = esAmigo,
            solicitudPendiente = solicitudPendiente,
            gruposCompartidos = gruposCompartidos
        )
    }
}