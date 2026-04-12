package ar.edu.unsam.proyectosoftware.mapper

import ar.edu.unsam.proyectosoftware.domain.Grupo
import ar.edu.unsam.proyectosoftware.domain.Usuario
import ar.edu.unsam.proyectosoftware.dto.GrupoContextoUsuarioDTO
import ar.edu.unsam.proyectosoftware.dto.GrupoDTO
import ar.edu.unsam.proyectosoftware.dto.GrupoDetalleDTO
import ar.edu.unsam.proyectosoftware.dto.GrupoSettingsDTO

object GrupoMapper {
    fun toGrupoDTO(grupo: Grupo): GrupoDTO {
        return GrupoDTO(
            id = grupo.id!!,
            nombre = grupo.nombre,
            imagenUrl = grupo.imagenUrl,
        )
    }

    fun toGrupoDetalleDTO(grupo: Grupo, cantIntegrantes: Long): GrupoDetalleDTO {
        return GrupoDetalleDTO(
            id = grupo.id!!,
            nombre = grupo.nombre,
            descripcion = grupo.descripcion,
            imagenUrl = grupo.imagenUrl,
            cantidadIntegrantes = cantIntegrantes
        )
    }

    fun toGrupoSettingsDTO(grupo: Grupo): GrupoSettingsDTO {
        return GrupoSettingsDTO(
            id = grupo.id!!,
            nombre = grupo.nombre,
            descripcion = grupo.descripcion,
            imagenUrl = grupo.imagenUrl,
            moneda = "ARS", // TODO: hardcodeado
            simplificarDeudas = true, // TODO: hardcodeado
            notificaciones = false // TODO hardcodeado
        )
    }
}