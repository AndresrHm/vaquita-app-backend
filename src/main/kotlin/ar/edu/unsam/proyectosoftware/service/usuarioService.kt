package ar.edu.unsam.proyectosoftware.service

import ar.edu.unsam.proyectosoftware.domain.Usuario
import org.springframework.stereotype.Service

@Service
class UsuarioService(
) {
    fun obtenerUsuario(usuarioID: Long): Usuario {
        return Usuario() //Mock
    }
}