package ar.edu.unsam.proyectosoftware.service

import ar.edu.unsam.proyectosoftware.config.JwtUtils
import ar.edu.unsam.proyectosoftware.domain.Usuario
import ar.edu.unsam.proyectosoftware.dto.UsuarioLoginDTO
import ar.edu.unsam.proyectosoftware.dto.UsuarioLogueadoDTO
import ar.edu.unsam.proyectosoftware.dto.UsuarioRegistradoDTO
import ar.edu.unsam.proyectosoftware.errors.ErrorStrings
import ar.edu.unsam.proyectosoftware.repository.GrupoRepository
import ar.edu.unsam.proyectosoftware.repository.UsuarioRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.lang.invoke.MethodHandles.throwException

@Service
class UsuarioService(
    val usuarioRepository: UsuarioRepository,
    val grupoRepository: GrupoRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtils: JwtUtils
) {
    @Transactional
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

        //Hash de contraseña
        val contrasenaHasheada = passwordEncoder.encode(dto.contrasena)

        //Se instancia un nuevo usuario
        val nuevoUsuario = Usuario(nombre = dto.nombre, email = dto.email, contrasena = contrasenaHasheada)

        return usuarioRepository.save(nuevoUsuario)
    }

    fun login(dataUser: UsuarioLoginDTO): UsuarioLogueadoDTO {
        val usuario = usuarioRepository.findByEmail(dataUser.email)
            .orElseThrow { Exception("Email o contraseña incorrectos") }

        if (!passwordEncoder.matches(dataUser.contrasena, usuario.contrasena)) {
            throw Exception("Email o contraseña incorrectos")
        }

        val token = jwtUtils.generarToken(usuario)

        return UsuarioLogueadoDTO(
            id = usuario.id!!,
            nombre = usuario.nombre,
            email = usuario.email,
            token = token
        )
    }

    fun obtenerUsuario(usuarioID: Long): Usuario {
        return usuarioRepository.findById(usuarioID)
            .orElseThrow { Exception("Usuario inexistente") }
    }

    fun obtenerTodosLosUsuarios(): Iterable<Usuario> {
        return usuarioRepository.findAll()
    }

    fun obtenerUsuariosPorGrupo(grupoId: Long): List<Usuario> {
        grupoRepository.findById(grupoId)
            .orElseThrow { Exception(ErrorStrings.GRUPO_NO_ENCONTRADO) }

        return usuarioRepository.findByGrupos_Id(grupoId)
    }

}