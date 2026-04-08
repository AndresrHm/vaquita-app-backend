package ar.edu.unsam.proyectosoftware.mapper

import ar.edu.unsam.proyectosoftware.domain.Grupo
import ar.edu.unsam.proyectosoftware.dto.GrupoDTO

object GrupoMapper {
    fun grupoToGrupoDTO(grupo: Grupo): GrupoDTO {
        return GrupoDTO(
            id = grupo.id!!,
            nombre = grupo.nombre,
            imagen = grupo.imagen,
        )
    }
}