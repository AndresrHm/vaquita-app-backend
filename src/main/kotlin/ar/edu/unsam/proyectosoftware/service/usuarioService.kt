package ar.edu.unsam.proyectosoftware.service

import ar.edu.unsam.proyectosoftware.security.JwtUtils
import ar.edu.unsam.proyectosoftware.domain.SolicitudAmistad
import ar.edu.unsam.proyectosoftware.domain.SolicitudAmistadFront
import ar.edu.unsam.proyectosoftware.domain.Usuario
import ar.edu.unsam.proyectosoftware.dto.CambioContrasenaDTO
import ar.edu.unsam.proyectosoftware.dto.UsuarioConBalanceDTO
import ar.edu.unsam.proyectosoftware.dto.UsuarioEditarDTO
import ar.edu.unsam.proyectosoftware.dto.UsuarioLoginDTO
import ar.edu.unsam.proyectosoftware.dto.UsuarioLogueadoDTO
import ar.edu.unsam.proyectosoftware.dto.UsuarioRegistradoDTO
import ar.edu.unsam.proyectosoftware.errors.ErrorStrings
import ar.edu.unsam.proyectosoftware.repository.FraccionGastoRepository
import ar.edu.unsam.proyectosoftware.repository.GrupoRepository
import ar.edu.unsam.proyectosoftware.repository.IntegranteGrupoRepository
import ar.edu.unsam.proyectosoftware.repository.SolicitudAmistadRepository
import ar.edu.unsam.proyectosoftware.repository.UsuarioRepository
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UsuarioService(
    val usuarioRepository: UsuarioRepository,
    val grupoRepository: GrupoRepository,
    val fraccionGastoRepository: FraccionGastoRepository,
    val integranteGrupoRepository: IntegranteGrupoRepository,
    val solicitudAmistadRepository: SolicitudAmistadRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtils: JwtUtils
) {

    @Transactional
    fun registrarUsuario(dto: UsuarioRegistradoDTO): Usuario {

        val errores = mutableListOf<String>()

        if (dto.nombre.isBlank()) {
            errores.add("• El nombre no puede estar vacío")
        }

        if (dto.email.isBlank()) {
            errores.add("• El email no puede estar vacío")
        } else if (usuarioRepository.findByEmail(dto.email).isPresent) {
            errores.add("• El email ya está registrado")
        }

        if (dto.contrasena.isBlank()) {
            errores.add("• La contraseña no puede estar vacía")
        } else if (dto.contrasena.length < 6) {
            errores.add("• La contraseña debe tener al menos 6 caracteres")
        }

        if (errores.isNotEmpty()) {
            val mensajeFinal = errores.joinToString("\n")
            throw Exception(mensajeFinal)
        }

        val contrasenaHasheada = passwordEncoder.encode(dto.contrasena)

        val nuevoUsuario = Usuario(nombre = dto.nombre, email = dto.email, contrasena = contrasenaHasheada)

        return usuarioRepository.save(nuevoUsuario)
    }

    @Transactional
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
            token = token,
            imagenUrl = usuario.imagenUrl
        )
    }

    fun obtenerUsuarioLogueado(auth: Authentication): Usuario {
        return usuarioRepository.findByEmail(auth.name)
            .orElseThrow { Exception("Sesión inválida o usuario no encontrado") }
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

        return integranteGrupoRepository.findAllByGrupoId(grupoId)
    }

    @Transactional
    fun actualizarPerfil(auth: Authentication, dto: UsuarioEditarDTO): UsuarioLogueadoDTO { // <-- 1. Cambiamos lo que devuelve
        val usuario = obtenerUsuarioLogueado(auth)

        val errores = mutableListOf<String>()

        if (dto.nombre.isBlank()) errores.add("• El nombre no puede estar vacío")

        if (dto.email.isBlank()) {
            errores.add("• El email no puede estar vacío")
        } else if (dto.email != usuario.email) {
            if (usuarioRepository.findByEmail(dto.email).isPresent) {
                errores.add("• El email ya está registrado por otro usuario")
            }
        }

        if (errores.isNotEmpty()) throw Exception(errores.joinToString("\n"))

        usuario.nombre = dto.nombre
        usuario.email = dto.email
        if (dto.imagenUrl != null) {
            usuario.imagenUrl = dto.imagenUrl
        }

        val usuarioGuardado = usuarioRepository.save(usuario)

        val nuevoToken = jwtUtils.generarToken(usuarioGuardado)

        return UsuarioLogueadoDTO(
            id = usuarioGuardado.id!!,
            nombre = usuarioGuardado.nombre,
            email = usuarioGuardado.email,
            token = nuevoToken, // <--- Entregamos la credencial nueva
            imagenUrl = usuarioGuardado.imagenUrl
        )
    }

    @Transactional
    fun cambiarContrasena(auth: Authentication, dto: CambioContrasenaDTO) {
        val usuario = obtenerUsuarioLogueado(auth)

        // 1. Validamos que la contraseña actual sea la correcta
        if (!passwordEncoder.matches(dto.contrasenaActual, usuario.contrasena)) {
            throw Exception("La contraseña actual es incorrecta")
        }

        // 2. Validaciones de la nueva contraseña
        if (dto.contrasenaNueva.isBlank() || dto.contrasenaNueva.length < 6) {
            throw Exception("La nueva contraseña debe tener al menos 6 caracteres")
        }

        // 3. Hasheamos la nueva y guardamos
        usuario.contrasena = passwordEncoder.encode(dto.contrasenaNueva)
        usuarioRepository.save(usuario)
    }

    @Transactional(readOnly = true)
    fun obtenerAmigos(auth: Authentication): List<UsuarioConBalanceDTO> {
        val usuarioLogueado = obtenerUsuarioLogueado(auth)
        val logueadoId = usuarioLogueado.id!!

        return usuarioLogueado.amigos.map { amigo ->
            val amigoId = amigo.id!!

            // Calcular lo que yo le debo a él
            val loQueDebo = fraccionGastoRepository
                .findFraccionesPendientesEntre(logueadoId, amigoId, null)
                .sumOf { it.saldoPendiente() }

            // Calcular lo que él me debe a mí
            val loQueMeDebe = fraccionGastoRepository
                .findFraccionesPendientesEntre(amigoId, logueadoId, null)
                .sumOf { it.saldoPendiente() }

            // Balance neto: Positivo = Me debe /// Negativo = Le debo
            val balanceNeto = Math.round((loQueDebo - loQueMeDebe) * 100) / 100.0

            UsuarioConBalanceDTO(
                id = amigoId,
                nombre = amigo.nombre,
                monto = balanceNeto
            )
        }
    }

    @Transactional(readOnly = true)
    fun obtenerSolicitudesPendientes(auth: Authentication): List<SolicitudAmistadFront> {
        val usuarioLogueado = obtenerUsuarioLogueado(auth)

        return solicitudAmistadRepository.findByIdReceptor(usuarioLogueado.id!!).map { solicitud ->
            val emisor = obtenerUsuario(solicitud.idEmisor)
            SolicitudAmistadFront(
                id = solicitud.id!!,
                nombre = emisor.nombre,
                imagenUrl = emisor.imagenUrl
            )
        }
    }

    @Transactional
    fun enviarSolicitudAmistad(auth: Authentication, idReceptor: Long): Long? {
        val emisor = obtenerUsuarioLogueado(auth)

        if (emisor.id == idReceptor) throw Exception("No podés enviarte una solicitud a vos mismo")

        val solicitud = SolicitudAmistad(emisor.id!!, idReceptor)
        solicitudAmistadRepository.save(solicitud)
        return idReceptor
    }

    @Transactional
    fun responderSolicitudAmistad(auth: Authentication, idSolicitud: Long, aceptar: Boolean): Long? {
        val usuarioLogueado = obtenerUsuarioLogueado(auth)

        val solicitud = solicitudAmistadRepository.findById(idSolicitud)
            .orElseThrow { Exception("Solicitud inexistente") }

        if (solicitud.idReceptor != usuarioLogueado.id) {
            throw Exception("No tenés permiso para responder esta solicitud")
        }

        if (aceptar) {
            val emisor = obtenerUsuario(solicitud.idEmisor)
            usuarioLogueado.agregarAmigo(emisor)
            emisor.agregarAmigo(usuarioLogueado)

            usuarioRepository.save(usuarioLogueado)
            usuarioRepository.save(emisor)
        }

        solicitudAmistadRepository.delete(solicitud)
        return usuarioLogueado.id
    }
}