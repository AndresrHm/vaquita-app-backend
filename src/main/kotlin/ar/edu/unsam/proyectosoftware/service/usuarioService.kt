package ar.edu.unsam.proyectosoftware.service

import ar.edu.unsam.proyectosoftware.domain.Usuario
import ar.edu.unsam.proyectosoftware.dto.UsuarioRegistradoDTO
import ar.edu.unsam.proyectosoftware.repository.UsuarioRepository
import org.springframework.stereotype.Service

@Service
class UsuarioService(val usuarioRepository: UsuarioRepository
) {

    fun registrarUsuario(dto: UsuarioRegistradoDTO): Usuario {

        val errores = mutableListOf<String>()

        // 2. Vamos revisando y anotando, PERO SIN HACER THROW TODAVÍA
        if (dto.nombre.isBlank()) {
            errores.add("• El nombre no puede estar vacío")
        }

        if (dto.email.isBlank()) {
            errores.add("• El email no puede estar vacío")
        } else if (usuarioRepository.findByEmail(dto.email).isPresent) {
            // Ponemos esto en un 'else' para no decirle que ya existe si en realidad está vacío
            errores.add("• El email ya está registrado")
        }

        if (dto.contrasena.isBlank()) {
            errores.add("• La contraseña no puede estar vacía")
        } else if (dto.contrasena.length < 6) {
            errores.add("• La contraseña debe tener al menos 6 caracteres")
        }

        // 3. El momento de la verdad: ¿Hay errores anotados?
        if (errores.isNotEmpty()) {
            // Unimos todos los mensajes con un salto de línea (\n) y explotamos
            val mensajeFinal = errores.joinToString("\n")
            throw Exception(mensajeFinal)
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