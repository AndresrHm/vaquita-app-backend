package ar.edu.unsam.proyectosoftware.mapper

import ar.edu.unsam.proyectosoftware.domain.Usuario
import ar.edu.unsam.proyectosoftware.dto.BalanceConGrupoDTO
import ar.edu.unsam.proyectosoftware.dto.GrupoNombreIdDTO
import ar.edu.unsam.proyectosoftware.dto.UsuarioBusquedaDTO
import ar.edu.unsam.proyectosoftware.dto.UsuarioInfoDTO

object UsuarioMapper {
    fun toInfoDTO(
        usuarioBuscado: Usuario,
        esAmigo: Boolean,
        solicitudPendiente: Boolean,
        gruposCompartidos: List<GrupoNombreIdDTO>,
        balances: List<BalanceConGrupoDTO>
    ): UsuarioInfoDTO {
        return UsuarioInfoDTO(
            nombre = usuarioBuscado.nombre,
            username = usuarioBuscado.username,
            imagenUrl = usuarioBuscado.imagenUrl,
            esAmigo = esAmigo,
            solicitudPendiente = solicitudPendiente,
            gruposCompartidos = gruposCompartidos,
            balances = balances
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
