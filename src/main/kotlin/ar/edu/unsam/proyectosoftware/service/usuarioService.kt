package ar.edu.unsam.proyectosoftware.service

import ar.edu.unsam.proyectosoftware.domain.Usuario
import ar.edu.unsam.proyectosoftware.dto.UsuarioRegistradoDTO
import ar.edu.unsam.proyectosoftware.repository.UsuarioRepository
import org.springframework.stereotype.Service

@Service
class UsuarioService(val usuarioRepository: UsuarioRepository
) {

    fun registrarUsuario(dto: UsuarioRegistradoDTO): Usuario {

        //Validaciones
        if (usuarioRepository.findByEmail(dto.email).isPresent){
            throw Exception("El email ya está registrado")
            }

        if (dto.contrasena.isBlank()) {
            throw Exception("La contraseña no puede estar vacía")
        }

        if (dto.contrasena.length < 6) {
            throw Exception("La contraseña debe tener al menos 6 caracteres")
        }

        //Se instancia un nuevo usuario
        val nuevoUsuario = Usuario(nombre = dto.nombre, email = dto.email, contrasena = dto.contrasena)

        return usuarioRepository.save(nuevoUsuario)
    }

    fun obtenerUsuario(usuarioID: Long): Usuario {
        return usuarioRepository.findById(usuarioID)
            .orElseThrow { Exception("Usuario inexistente") }
    }

    fun obtenerTodosLosUsuarios(): Iterable<Usuario> {
        return usuarioRepository.findAll()
    }
}