package ar.edu.unsam.proyectosoftware.service

import ar.edu.unsam.proyectosoftware.BusinessException
import ar.edu.unsam.proyectosoftware.domain.Usuario
import ar.edu.unsam.proyectosoftware.dto.CambioContrasenaDTO
import ar.edu.unsam.proyectosoftware.dto.UsuarioEditarDTO
import ar.edu.unsam.proyectosoftware.dto.UsuarioLoginDTO
import ar.edu.unsam.proyectosoftware.dto.UsuarioRegistradoDTO
import ar.edu.unsam.proyectosoftware.repository.*
import ar.edu.unsam.proyectosoftware.security.JwtUtils
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.core.Authentication
import java.util.Optional

@ExtendWith(MockKExtension::class)
class UsuarioServiceTest {

    @MockK lateinit var usuarioRepository: UsuarioRepository
    @MockK lateinit var grupoRepository: GrupoRepository
    @MockK lateinit var integranteGrupoRepository: IntegranteGrupoRepository
    @MockK lateinit var solicitudAmistadRepository: SolicitudAmistadRepository
    @MockK lateinit var passwordEncoder: PasswordEncoder
    @MockK lateinit var jwtUtils: JwtUtils
    @MockK(relaxed = true) lateinit var actividadService: ActividadService
    @MockK lateinit var fraccionGastoRepository: FraccionGastoRepository

    @InjectMockKs
    lateinit var usuarioService: UsuarioService

    // ==========================================
    // TESTS DE LOGIN Y TOKENS (ACTUALIZADOS JWT ROBUSTO)
    // ==========================================

    @Test
    fun `login - camino feliz - devuelve DTO con access y refresh tokens si credenciales son correctas`() {
        // Arrange
        val loginDTO = UsuarioLoginDTO("giuli.m@mail.com", "mipassword123")
        val usuarioMock = Usuario(nombre = "Giuliana", email = "giuli.m@mail.com", contrasena = "hash").apply {
            id = 1L
            imagenUrl = ""
        }

        every { usuarioRepository.findByEmail(loginDTO.email) } returns Optional.of(usuarioMock)
        every { passwordEncoder.matches(loginDTO.contrasena, usuarioMock.contrasena) } returns true

        // Mockeamos los dos tokens nuevos
        every { jwtUtils.generarAccessToken(usuarioMock) } returns "access_token_123"
        every { jwtUtils.generarRefreshToken(usuarioMock) } returns "refresh_token_123"

        // Act
        val resultado = usuarioService.login(loginDTO)

        // Assert
        assertThat(resultado.email).isEqualTo("giuli.m@mail.com")
        assertThat(resultado.accessToken).isEqualTo("access_token_123")
        assertThat(resultado.refreshToken).isEqualTo("refresh_token_123")
    }

    @Test
    fun `login - camino triste - lanza excepcion si la contrasena es incorrecta`() {
        val loginDTO = UsuarioLoginDTO("giuli.m@mail.com", "password_equivocada")
        val usuarioMock = Usuario(nombre = "Giuliana", email = "giuli.m@mail.com", contrasena = "hash").apply { id = 1L }

        every { usuarioRepository.findByEmail(loginDTO.email) } returns Optional.of(usuarioMock)
        every { passwordEncoder.matches(loginDTO.contrasena, usuarioMock.contrasena) } returns false

        assertThatThrownBy {
            usuarioService.login(loginDTO)
        }.hasMessageContaining("Email o contraseña incorrectos")
    }

    @Test
    fun `refrescarToken - camino feliz - devuelve nuevos tokens si el refresh token es valido`() {
        // Arrange
        val tokenViejo = "refresh_token_valido"
        val usuarioMock = Usuario(nombre = "Giuliana", email = "giuli@mail.com", contrasena = "hash").apply { id = 1L }

        every { jwtUtils.esTokenValido(tokenViejo) } returns true
        every { jwtUtils.obtenerEmailDeToken(tokenViejo) } returns "giuli@mail.com"
        every { usuarioRepository.findByEmail("giuli@mail.com") } returns Optional.of(usuarioMock)

        // Simulamos la creacion de las nuevas llaves
        every { jwtUtils.generarAccessToken(usuarioMock) } returns "access_token_NUEVO"
        every { jwtUtils.generarRefreshToken(usuarioMock) } returns "refresh_token_NUEVO"

        // Act
        val resultado = usuarioService.refrescarToken(tokenViejo)

        // Assert
        assertThat(resultado.accessToken).isEqualTo("access_token_NUEVO")
        assertThat(resultado.refreshToken).isEqualTo("refresh_token_NUEVO")
    }

    @Test
    fun `refrescarToken - camino triste - lanza excepcion si el token es invalido`() {
        // Arrange
        val tokenTrucho = "token_vencido_o_inventado"
        every { jwtUtils.esTokenValido(tokenTrucho) } returns false

        // Act & Assert
        assertThatThrownBy {
            usuarioService.refrescarToken(tokenTrucho)
        }.isInstanceOf(BusinessException::class.java)
            .hasMessageContaining("inválido o expirado")
    }

    // ==========================================
    // TESTS DE REGISTRO
    // ==========================================

    @Test
    fun `registrarUsuario - camino feliz - guarda el usuario con contrasena encriptada`() {
        val registroDTO = UsuarioRegistradoDTO("Giuliana", "giuli.m@mail.com", "secreta123")
        every { usuarioRepository.findByEmail(registroDTO.email) } returns Optional.empty()
        every { passwordEncoder.encode(registroDTO.contrasena) } returns "secreta123_hasheada"
        every { usuarioRepository.save(any()) } answers { firstArg() }

        val usuarioGuardado = usuarioService.registrarUsuario(registroDTO)

        assertThat(usuarioGuardado.nombre).isEqualTo("Giuliana")
        assertThat(usuarioGuardado.email).isEqualTo("giuli.m@mail.com")
        assertThat(usuarioGuardado.contrasena).isEqualTo("secreta123_hasheada")
    }

    @Test
    fun `registrarUsuario - camino triste - lanza excepcion si el email ya existe`() {
        val registroDTO = UsuarioRegistradoDTO("Giuliana", "giuli.m@mail.com", "secreta123")
        val usuarioExistente = Usuario(nombre = "Otra Persona", email = "giuli.m@mail.com", contrasena = "hash")
        every { usuarioRepository.findByEmail(registroDTO.email) } returns Optional.of(usuarioExistente)
        every { usuarioRepository.existsByUsername(registroDTO.username) } returns false

        assertThatThrownBy {
            usuarioService.registrarUsuario(registroDTO)
        }.hasMessageContaining("El email ya está registrado")
    }

    // ==========================================
    // TESTS DE PERFIL Y CONTRASEÑA
    // ==========================================

    @Test
    fun `actualizarPerfil - camino feliz - actualiza los datos y devuelve tokens nuevos`() {
        val authMock = mockk<Authentication>()
        every { authMock.name } returns "giuli.m@mail.com"

        val usuarioMock = Usuario(nombre = "Giuliana Vieja", email = "giuli.m@mail.com", contrasena = "hash").apply {
            id = 1L; imagenUrl = "foto_vieja.png"
        }

        val dtoActualizacion = UsuarioEditarDTO("Giuliana Nueva", "giuli.nueva@mail.com", "nueva_foto.png")

        every { usuarioRepository.findByEmail("giuli.m@mail.com") } returns Optional.of(usuarioMock)
        every { usuarioRepository.findByEmail("giuli.nueva@mail.com") } returns Optional.empty()
        every { usuarioRepository.save(any()) } answers { firstArg() }

        // Mockeamos la generacion doble de tokens
        every { jwtUtils.generarAccessToken(any()) } returns "access_super_nuevo"
        every { jwtUtils.generarRefreshToken(any()) } returns "refresh_super_nuevo"

        val resultado = usuarioService.actualizarPerfil(authMock, dtoActualizacion)

        assertThat(resultado.nombre).isEqualTo("Giuliana Nueva")
        assertThat(resultado.email).isEqualTo("giuli.nueva@mail.com")
        assertThat(resultado.accessToken).isEqualTo("access_super_nuevo")
        assertThat(resultado.refreshToken).isEqualTo("refresh_super_nuevo")
    }

    @Test
    fun `cambiarContrasena - camino triste - falla si la contrasena actual no coincide`() {
        val authMock = mockk<Authentication>()
        every { authMock.name } returns "giuli.m@mail.com"

        val usuarioMock = Usuario(nombre = "Giuliana", email = "giuli.m@mail.com", contrasena = "hash_viejo").apply { id = 1L }
        val dtoPass = CambioContrasenaDTO(contrasenaActual = "clave_equivocada", contrasenaNueva = "nueva12345")

        every { usuarioRepository.findByEmail("giuli.m@mail.com") } returns Optional.of(usuarioMock)
        every { passwordEncoder.matches(dtoPass.contrasenaActual, usuarioMock.contrasena) } returns false

        assertThatThrownBy {
            usuarioService.cambiarContrasena(authMock, dtoPass)
        }.hasMessageContaining("incorrecta")
    }

    // ==========================================
    // TESTS DE CONSULTAS Y AMISTAD
    // ==========================================

    @Test
    fun `obtenerUsuario - camino triste - lanza excepcion si el ID no existe`() {
        every { usuarioRepository.findById(99L) } returns Optional.empty()
        assertThatThrownBy {
            usuarioService.obtenerUsuario(99L)
        }.hasMessageContaining("inexistente")
    }

    @Test
    fun `obtenerAmigos - camino feliz - devuelve la lista mapeada a DTO`() {
        val authMock = mockk<Authentication>()
        every { authMock.name } returns "giuli@mail.com"

        val amigoMock = Usuario(nombre = "Hermano", email = "hermano@mail.com", contrasena = "hash").apply { id = 2L; imagenUrl = "" }
        val usuarioMock = Usuario(nombre = "Giuliana", email = "giuli@mail.com", contrasena = "hash").apply {
            id = 1L; amigos = mutableListOf(amigoMock)
        }

        every { usuarioRepository.findByEmail("giuli@mail.com") } returns Optional.of(usuarioMock)
        every { fraccionGastoRepository.findFraccionesPendientesEntre(any(), any(), any()) } returns emptyList()

        val resultado = usuarioService.obtenerAmigos(authMock)

        assertThat(resultado).hasSize(1)
        assertThat(resultado[0].id).isEqualTo(2L)
    }

    @Test
    fun `enviarSolicitudAmistad - camino feliz - guarda la solicitud y devuelve el ID`() {
        val authMock = mockk<Authentication>()
        every { authMock.name } returns "giuli@mail.com"
        val emisorMock = Usuario(nombre = "Giuliana", email = "giuli@mail.com", contrasena = "hash").apply { id = 1L }

        every { usuarioRepository.findByEmail("giuli@mail.com") } returns Optional.of(emisorMock)
        every { solicitudAmistadRepository.save(any()) } answers { firstArg() }
        every { usuarioRepository.existsByIdAndAmigosId(any(), any()) } returns false
        every { solicitudAmistadRepository.existsByIdEmisorAndIdReceptor(any(), any()) } returns false

        val idReceptor = 2L
        val resultado = usuarioService.enviarSolicitudAmistad(authMock, idReceptor)
        assertThat(resultado).isEqualTo(idReceptor)
    }

    @Test
    fun `enviarSolicitudAmistad - camino triste - falla si se envia a si mismo`() {
        val authMock = mockk<Authentication>()
        every { authMock.name } returns "giuli@mail.com"
        val emisorMock = Usuario(nombre = "Giuliana", email = "giuli@mail.com", contrasena = "hash").apply { id = 1L }

        every { usuarioRepository.findByEmail("giuli@mail.com") } returns Optional.of(emisorMock)

        assertThatThrownBy {
            usuarioService.enviarSolicitudAmistad(authMock, 1L)
        }.hasMessageContaining("mismo")
    }

    @Test
    fun `responderSolicitudAmistad - camino feliz - acepta solicitud y se agregan como amigos`() {
        val authMock = mockk<Authentication>()
        every { authMock.name } returns "giuli@mail.com"
        val usuarioLogueado = Usuario(nombre = "Giuliana", email = "giuli@mail.com", contrasena = "").apply { id = 1L }
        val emisorMock = Usuario(nombre = "Hermano", email = "hermano@mail.com", contrasena = "").apply { id = 2L }
        val solicitudMock = ar.edu.unsam.proyectosoftware.domain.SolicitudAmistad(2L, 1L).apply { id = 100L }

        every { usuarioRepository.findByEmail("giuli@mail.com") } returns Optional.of(usuarioLogueado)
        every { solicitudAmistadRepository.findById(100L) } returns Optional.of(solicitudMock)
        every { usuarioRepository.findById(2L) } returns Optional.of(emisorMock)

        every { usuarioRepository.save(any()) } answers { firstArg() }
        every { solicitudAmistadRepository.delete(any()) } returns Unit
        every { usuarioRepository.existsByIdAndAmigosId(any(), any()) } returns false
        every { solicitudAmistadRepository.deleteByIdEmisorAndIdReceptor(any(), any()) } returns Unit

        val resultado = usuarioService.responderSolicitudAmistad(authMock, 100L, aceptar = true)

        assertThat(resultado).isEqualTo(1L)
        assertThat(usuarioLogueado.amigos).contains(emisorMock)
    }

    @Test
    fun `responderSolicitudAmistad - camino triste - falla si respondes la solicitud de otro`() {
        val authMock = mockk<Authentication>()
        every { authMock.name } returns "giuli@mail.com"
        val usuarioLogueado = Usuario(nombre = "Giuliana", email = "giuli@mail.com", contrasena = "").apply { id = 1L }
        val solicitudAjena = ar.edu.unsam.proyectosoftware.domain.SolicitudAmistad(2L, 99L).apply { id = 100L }

        every { usuarioRepository.findByEmail("giuli@mail.com") } returns Optional.of(usuarioLogueado)
        every { solicitudAmistadRepository.findById(100L) } returns Optional.of(solicitudAjena)

        assertThatThrownBy {
            usuarioService.responderSolicitudAmistad(authMock, 100L, aceptar = true)
        }.hasMessageContaining("permiso")
    }

    @Test
    fun `obtenerUsuariosPorGrupo - camino feliz - devuelve los integrantes`() {
        val grupoId = 10L
        val usuario1 = Usuario(nombre = "Giuli", email = "g@g.com", contrasena = "").apply { id = 1L }
        val usuario2 = Usuario(nombre = "Novio", email = "n@n.com", contrasena = "").apply { id = 2L }

        every { grupoRepository.findById(grupoId) } returns Optional.of(mockk())
        every { integranteGrupoRepository.findAllByGrupoId(grupoId) } returns listOf(usuario1, usuario2)

        val resultado = usuarioService.obtenerUsuariosPorGrupo(grupoId)

        assertThat(resultado).hasSize(2)
        assertThat(resultado.map { it.nombre }).containsExactly("Giuli", "Novio")
    }
}