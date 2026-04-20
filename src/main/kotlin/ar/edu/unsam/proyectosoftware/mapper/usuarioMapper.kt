package ar.edu.unsam.proyectosoftware.mapper

import ar.edu.unsam.proyectosoftware.domain.Usuario
import ar.edu.unsam.proyectosoftware.dto.UsuarioInfoDTO

object UsuarioMapper {
    fun toInfoDTO(usuarioBuscado: Usuario, usuario: Usuario) : UsuarioInfoDTO{
        return UsuarioInfoDTO(
            nombre = usuarioBuscado.nombre,
            imagenUrl = usuarioBuscado.imagenUrl,
            esAmigo = usuarioBuscado.esAmigo(usuario)
        )
    }
}