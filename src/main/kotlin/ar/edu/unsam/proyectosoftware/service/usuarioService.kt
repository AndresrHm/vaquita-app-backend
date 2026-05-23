package ar.edu.unsam.proyectosoftware.service

import ar.edu.unsam.proyectosoftware.BusinessException
import ar.edu.unsam.proyectosoftware.security.JwtUtils
import ar.edu.unsam.proyectosoftware.domain.SolicitudAmistad
import ar.edu.unsam.proyectosoftware.domain.SolicitudAmistadFront
import ar.edu.unsam.proyectosoftware.domain.Usuario
import ar.edu.unsam.proyectosoftware.domain.Gasto
import ar.edu.unsam.proyectosoftware.dto.CambioContrasenaDTO
import ar.edu.unsam.proyectosoftware.dto.GastoPendienteDTO
import ar.edu.unsam.proyectosoftware.dto.UsuarioBusquedaDTO
import ar.edu.unsam.proyectosoftware.dto.UsuarioConBalanceDTO
import ar.edu.unsam.proyectosoftware.dto.UsuarioEditarDTO
import ar.edu.unsam.proyectosoftware.dto.UsuarioLoginDTO
import ar.edu.unsam.proyectosoftware.dto.UsuarioLogueadoDTO
import ar.edu.unsam.proyectosoftware.dto.UsuarioRegistradoDTO
import ar.edu.unsam.proyectosoftware.dto.TipoActividad
import ar.edu.unsam.proyectosoftware.errors.ErrorStrings
import ar.edu.unsam.proyectosoftware.mapper.UsuarioMapper
import ar.edu.unsam.proyectosoftware.repository.FraccionGastoRepository
import ar.edu.unsam.proyectosoftware.repository.GrupoRepository
import ar.edu.unsam.proyectosoftware.repository.IntegranteGrupoRepository
import ar.edu.unsam.proyectosoftware.repository.SolicitudAmistadRepository
import ar.edu.unsam.proyectosoftware.repository.UsuarioRepository
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.ByteArrayOutputStream

@Service
@Transactional
class UsuarioService(
    val usuarioRepository: UsuarioRepository,
    val grupoRepository: GrupoRepository,
    val fraccionGastoRepository: FraccionGastoRepository,
    val integranteGrupoRepository: IntegranteGrupoRepository,
    val solicitudAmistadRepository: SolicitudAmistadRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtils: JwtUtils,
    private val actividadService: ActividadService,
) {

    private val usernameRegex = Regex("^[a-zA-Z0-9._]{3,20}$")

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

        if (dto.username.isBlank()) {
            errores.add("• El nombre de usuario no puede estar vacío")
        } else if (!usernameRegex.matches(dto.username)) {
            errores.add("• El nombre de usuario solo puede contener letras, números, puntos y guiones bajos (3-20 caracteres)")
        } else if (usuarioRepository.existsByUsername(dto.username)) {
            errores.add("• El nombre de usuario ya está en uso")
        }

        if (errores.isNotEmpty()) {
            val mensajeFinal = errores.joinToString("\n")
            throw Exception(mensajeFinal)
        }

        val contrasenaHasheada = passwordEncoder.encode(dto.contrasena)

        val nuevoUsuario = Usuario(nombre = dto.nombre, email = dto.email, contrasena = contrasenaHasheada, username = dto.username)
        val usuarioCreado = usuarioRepository.save(nuevoUsuario)

        actividadService.registrarActividad(
            usuario = usuarioCreado,
            tipo = TipoActividad.CUENTA_CREADA,
            descripcion = "Tu cuenta fue creada exitosamente.",
            metadata = mapOf(
                "usuarioId" to usuarioCreado.id,
                "email" to usuarioCreado.email,
            )
        )

        return usuarioCreado
    }

    @Transactional
    fun login(dataUser: UsuarioLoginDTO): UsuarioLogueadoDTO {
        val usuario = usuarioRepository.findByEmail(dataUser.email)
            .orElseThrow { Exception("Email o contraseña incorrectos") }

        if (!passwordEncoder.matches(dataUser.contrasena, usuario.contrasena)) {
            throw Exception("Email o contraseña incorrectos")
        }

        val accessTokenGenerado = jwtUtils.generarAccessToken(usuario)
        val refreshTokenGenerado = jwtUtils.generarRefreshToken(usuario)

        return UsuarioLogueadoDTO(
            id = usuario.id!!,
            nombre = usuario.nombre,
            email = usuario.email,
            accessToken = accessTokenGenerado,   // Acá va la pulsera de 15 minutos
            refreshToken = refreshTokenGenerado,
            username = usuario.username,
            imagenUrl = usuario.imagenUrl
        )
    }

    fun refrescarToken(refreshToken: String): UsuarioLogueadoDTO {
        // 1. Validamos que el refresh token no esté trucho ni vencido (esos 7 días)
        if (!jwtUtils.esTokenValido(refreshToken)) {
            throw BusinessException("Refresh token inválido o expirado. Por favor, iniciá sesión nuevamente.")
        }

        // 2. Sacamos de adentro del token a quién le pertenece
        val email = jwtUtils.obtenerEmailDeToken(refreshToken)
        val usuario = usuarioRepository.findByEmail(email)
            .orElseThrow { BusinessException("Usuario no encontrado") }

        // 3. Le fabricamos un combo de llaves nuevitas
        val nuevoAccessToken = jwtUtils.generarAccessToken(usuario)
        val nuevoRefreshToken = jwtUtils.generarRefreshToken(usuario) // Rotamos también este por seguridad

        // 4. Se lo devolvemos empaquetado en tu DTO
        return UsuarioLogueadoDTO(
            id = usuario.id!!,
            accessToken = nuevoAccessToken,
            refreshToken = nuevoRefreshToken,
            nombre = usuario.nombre,
            email = usuario.email,
            username = usuario.username,
            imagenUrl = usuario.imagenUrl ?: ""
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

    fun sonAmigos(idUsuario: Long, idAmigo: Long): Boolean =
        usuarioRepository.existsByIdAndAmigosId(idUsuario, idAmigo)

    fun tieneSolicitudPendiente(idEmisor: Long, idReceptor: Long): Boolean =
        solicitudAmistadRepository.existsByIdEmisorAndIdReceptor(idEmisor, idReceptor)

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

        if (dto.username != null) {
            if (!usernameRegex.matches(dto.username)) {
                errores.add("• El nombre de usuario solo puede contener letras, números, puntos y guiones bajos (3-20 caracteres)")
            } else if (dto.username != usuario.username && usuarioRepository.existsByUsername(dto.username)) {
                errores.add("• El nombre de usuario ya está en uso")
            }
        }

        if (errores.isNotEmpty()) throw Exception(errores.joinToString("\n"))

        usuario.nombre = dto.nombre
        usuario.email = dto.email
        if (dto.imagenUrl != null) {
            usuario.imagenUrl = dto.imagenUrl
        }
        if (dto.username != null) {
            usuario.username = dto.username
        }

        val usuarioGuardado = usuarioRepository.save(usuario)

        val nuevoAccessToken = jwtUtils.generarAccessToken(usuarioGuardado)
        val nuevoRefreshToken = jwtUtils.generarRefreshToken(usuarioGuardado)

        return UsuarioLogueadoDTO(
            id = usuarioGuardado.id!!,
            nombre = usuarioGuardado.nombre,
            email = usuarioGuardado.email,
            accessToken = nuevoAccessToken,
            refreshToken = nuevoRefreshToken,
            username = usuario.username,
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
                username = amigo.username,
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

        if (usuarioRepository.existsByIdAndAmigosId(emisor.id!!, idReceptor))
            throw Exception("Este usuario ya es tu amigo")

        if (solicitudAmistadRepository.existsByIdEmisorAndIdReceptor(idReceptor, emisor.id!!))
            throw Exception("Este usuario ya te envió una solicitud de amistad")

        if (solicitudAmistadRepository.existsByIdEmisorAndIdReceptor(emisor.id!!, idReceptor))
            throw Exception("Ya le enviaste una solicitud de amistad a este usuario")

        val solicitud = SolicitudAmistad(emisor.id!!, idReceptor)
        solicitudAmistadRepository.save(solicitud)
        return idReceptor
    }

    @Transactional
    fun eliminarAmigo(idUsuario: Long, idAmigo: Long) {
        if (!usuarioRepository.existsByIdAndAmigosId(idUsuario, idAmigo))
            throw Exception("Este usuario no es tu amigo")

        if (fraccionGastoRepository.findGastosPendientesBetween(idUsuario, idAmigo).isNotEmpty())
            throw Exception(ErrorStrings.DEUDA_PENDIENTE_CON_AMIGO)

        val usuario = obtenerUsuario(idUsuario)
        val amigo = obtenerUsuario(idAmigo)
        usuario.eliminarAmigo(amigo)
        amigo.eliminarAmigo(usuario)
        usuarioRepository.save(usuario)
        usuarioRepository.save(amigo)

        // Clean up any stale pending requests between them
        solicitudAmistadRepository.deleteByIdEmisorAndIdReceptor(idUsuario, idAmigo)
        solicitudAmistadRepository.deleteByIdEmisorAndIdReceptor(idAmigo, idUsuario)
    }


    @Transactional(readOnly = true)
    fun obtenerGastosPendientes(deudorId: Long): List<GastoPendienteDTO> {
        return fraccionGastoRepository.findGastosPendientesByDeudorId(deudorId).map { fraccion ->
            GastoPendienteDTO(
                id = fraccion.id!!,
                descripcion = fraccion.gastoAsociado.descripcion,
                monto = fraccion.saldoPendiente(),
                acreedorNombre = fraccion.acreedor.nombre,
                grupoNombre = fraccion.gastoAsociado.grupo.nombre,
                fecha = fraccion.gastoAsociado.fecha
            )
        }
    }

    @Transactional(readOnly = true)
    fun obtenerGastosEntreUsuarios(logueadoId: Long, otroId: Long): List<Gasto> {
        val fraccionesQueDebo = fraccionGastoRepository.findGastosPendientesBetween(logueadoId, otroId)
        val fraccionesQueMeDeben = fraccionGastoRepository.findGastosPendientesBetween(otroId, logueadoId)
        return (fraccionesQueDebo + fraccionesQueMeDeben)
            .map { it.gastoAsociado }
            .distinctBy { it.id }
    }

    @Transactional(readOnly = true)
    fun buscarPorUsername(query: String, solicitanteId: Long): List<UsuarioBusquedaDTO> {
        return usuarioRepository.findByUsernameContainingIgnoreCase(query)
            .filter { it.id != solicitanteId }
            .map { UsuarioMapper.toBusquedaDTO(it) }
    }

    @Transactional(readOnly = true)
    fun generarQrUsuario(userId: Long): ByteArray {
        val usuario = obtenerUsuario(userId)
        val contenido = "@${usuario.username}"
        val hints = mapOf(EncodeHintType.CHARACTER_SET to "UTF-8", EncodeHintType.MARGIN to 1)
        val bitMatrix = QRCodeWriter().encode(contenido, BarcodeFormat.QR_CODE, 300, 300, hints)
        val outputStream = ByteArrayOutputStream()
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream)
        return outputStream.toByteArray()
    }

    fun responderSolicitudAmistad(auth: Authentication, idSolicitud: Long, aceptar: Boolean): Long? {
        val usuarioLogueado = obtenerUsuarioLogueado(auth)


        val solicitud = solicitudAmistadRepository.findById(idSolicitud)
            .orElseThrow { Exception("Solicitud inexistente") }

        if (solicitud.idReceptor != usuarioLogueado.id) {
            throw Exception("No tenés permiso para responder esta solicitud")
        }

        if (aceptar) {
            if (usuarioRepository.existsByIdAndAmigosId(usuarioLogueado.id!!, solicitud.idEmisor))
                throw Exception("Ya son amigos")

            val emisor = obtenerUsuario(solicitud.idEmisor)
            usuarioLogueado.agregarAmigo(emisor)
            emisor.agregarAmigo(usuarioLogueado)

            usuarioRepository.save(usuarioLogueado)
            usuarioRepository.save(emisor)

            actividadService.registrarAmistadAceptada(usuarioLogueado, emisor)

            // Clean up any reverse pending request between the two users
            solicitudAmistadRepository.deleteByIdEmisorAndIdReceptor(usuarioLogueado.id!!, solicitud.idEmisor)
        }

        solicitudAmistadRepository.delete(solicitud)
        return usuarioLogueado.id
    }
}