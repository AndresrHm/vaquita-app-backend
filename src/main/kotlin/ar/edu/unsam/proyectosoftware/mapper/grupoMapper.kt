package ar.edu.unsam.proyectosoftware.mapper

import ar.edu.unsam.proyectosoftware.domain.Grupo
import ar.edu.unsam.proyectosoftware.dto.GrupoDTO
import ar.edu.unsam.proyectosoftware.dto.GrupoDetalleDTO

object GrupoMapper {
    fun toGrupoDTO(grupo: Grupo): GrupoDTO {
        return GrupoDTO(
            id = grupo.id!!,
            nombre = grupo.nombre,
            imagen = grupo.imagenUrl,
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
}

//data class GrupoDetalleDTO(
//    val name: String,
//    val status: String,
//    val outstanding: Double,
//    val currency: String,
//    val imageUrl: String,
//)