package ar.edu.unsam.proyectosoftware.mapper

import ar.edu.unsam.proyectosoftware.domain.Usuario
import ar.edu.unsam.proyectosoftware.dto.GastosPorAnioDTO
import ar.edu.unsam.proyectosoftware.dto.GrupoNombreIdDTO
import ar.edu.unsam.proyectosoftware.dto.UsuarioBusquedaDTO
import ar.edu.unsam.proyectosoftware.dto.UsuarioInfoDTO

object UsuarioMapper {
    fun toInfoDTO(
        usuarioBuscado: Usuario,
        esAmigo: Boolean,
        solicitudPendiente: Boolean,
        gruposCompartidos: List<GrupoNombreIdDTO>,
        gastosPendientes: List<GastosPorAnioDTO>
    ): UsuarioInfoDTO {
        return UsuarioInfoDTO(
            nombre = usuarioBuscado.nombre,
            username = usuarioBuscado.username,
            imagenUrl = usuarioBuscado.imagenUrl,
            esAmigo = esAmigo,
            solicitudPendiente = solicitudPendiente,
            gruposCompartidos = gruposCompartidos,
            gastosPendientes = gastosPendientes
        )
    }

    fun toBusquedaDTO(usuario: Usuario): UsuarioBusquedaDTO {
        return UsuarioBusquedaDTO(
            id = usuario.id!!,
            nombre = usuario.nombre,
            username = usuario.username,
            imagenUrl = usuario.imagenUrl
        )
    }
}
