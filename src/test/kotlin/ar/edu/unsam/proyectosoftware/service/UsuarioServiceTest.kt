package ar.edu.unsam.proyectosoftware.service

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

    // 1. INYECTAMOS LOS SIMULACROS
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
    // TESTS DE LOGIN
    // ==========================================

    @Test
    fun `login - camino feliz - devuelve DTO con token si las credenciales son correctas`() {
        // Arrange (Preparar)
        val loginDTO = UsuarioLoginDTO("giuli.m@mail.com", "mipassword123")
        val usuarioMock = Usuario(nombre = "Giuliana", email = "giuli.m@mail.com", contrasena = "hash").apply {
            id = 1L
            imagenUrl = ""
        }

        every { usuarioRepository.findByEmail(loginDTO.email) } returns Optional.of(usuarioMock)
        every { passwordEncoder.matches(loginDTO.contrasena, usuarioMock.contrasena) } returns true
        every { jwtUtils.generarToken(usuarioMock) } returns "token_vip_123"

        // Act (Ejecutar)
        val resultado = usuarioService.login(loginDTO)

        // Assert (Verificar)
        assertThat(resultado.email).isEqualTo("giuli.m@mail.com")
        assertThat(resultado.token).isEqualTo("token_vip_123")
    }

    @Test
    fun `login - camino triste - lanza excepcion si la contrasena es incorrecta`() {
        // Arrange
        val loginDTO = UsuarioLoginDTO("giuli.m@mail.com", "password_equivocada")
        val usuarioMock = Usuario(nombre = "Giuliana", email = "giuli.m@mail.com", contrasena = "hash").apply { id = 1L }

        every { usuarioRepository.findByEmail(loginDTO.email) } returns Optional.of(usuarioMock)
        // Simulamos que el validador de contraseñas dice "No coinciden"
        every { passwordEncoder.matches(loginDTO.contrasena, usuarioMock.contrasena) } returns false

        // Act & Assert
        // Verificamos que el service explote y tire el mensaje exacto que programaste
        assertThatThrownBy {
            usuarioService.login(loginDTO)
        }.hasMessage("Email o contraseña incorrectos")
    }

    // ==========================================
    // TESTS DE REGISTRO
    // ==========================================

    @Test
    fun `registrarUsuario - camino feliz - guarda el usuario con contrasena encriptada`() {
        // Arrange
        val registroDTO = UsuarioRegistradoDTO("Giuliana", "giuli.m@mail.com", "secreta123", "giuliana")

        // Simulamos que el email no existe en la base de datos
        every { usuarioRepository.findByEmail(registroDTO.email) } returns Optional.empty()
        every { usuarioRepository.existsByUsername(registroDTO.username) } returns false

        // Simulamos la encriptación
        every { passwordEncoder.encode(registroDTO.contrasena) } returns "secreta123_hasheada"

        // Simulamos el guardado en la base de datos (devuelve lo mismo que recibe)
        every { usuarioRepository.save(any()) } answers { firstArg() }

        // Act
        val usuarioGuardado = usuarioService.registrarUsuario(registroDTO)

        // Assert
        assertThat(usuarioGuardado.nombre).isEqualTo("Giuliana")
        assertThat(usuarioGuardado.email).isEqualTo("giuli.m@mail.com")
        // Verificamos que no se guardó la contraseña en texto plano
        assertThat(usuarioGuardado.contrasena).isEqualTo("secreta123_hasheada")
    }

    @Test
    fun `registrarUsuario - camino triste - lanza excepcion si el email ya existe`() {
        // Arrange
        val registroDTO = UsuarioRegistradoDTO("Giuliana", "giuli.m@mail.com", "secreta123", "giuliana")
        val usuarioExistente = Usuario(nombre = "Otra Persona", email = "giuli.m@mail.com", contrasena = "hash")

        // Simulamos que el repositorio SÍ encuentra a alguien con ese mail
        every { usuarioRepository.findByEmail(registroDTO.email) } returns Optional.of(usuarioExistente)
        every { usuarioRepository.existsByUsername(registroDTO.username) } returns false

        // Act & Assert
        assertThatThrownBy {
            usuarioService.registrarUsuario(registroDTO)
        }.hasMessageContaining("El email ya está registrado")
    }

    // ==========================================
    // TESTS DE PERFIL Y CONTRASEÑA
    // ==========================================

    @Test
    fun `actualizarPerfil - camino feliz - actualiza los datos y devuelve un nuevo token`() {
        // Arrange
        // Creamos una credencial falsa que dice que somos Giuli
        val authMock = mockk<Authentication>()
        every { authMock.name } returns "giuli.m@mail.com"

        val usuarioMock = Usuario(nombre = "Giuliana Vieja", email = "giuli.m@mail.com", contrasena = "hash").apply {
            id = 1L
            imagenUrl = "foto_vieja.png"
        }

        val dtoActualizacion = UsuarioEditarDTO(
            nombre = "Giuliana Nueva",
            email = "giuli.nueva@mail.com",
            imagenUrl = "nueva_foto.png"
        )

        // 1. Nos busca por el mail de la credencial
        every { usuarioRepository.findByEmail("giuli.m@mail.com") } returns Optional.of(usuarioMock)
        // 2. Revisa que el mail nuevo no esté robado por otro
        every { usuarioRepository.findByEmail("giuli.nueva@mail.com") } returns Optional.empty()
        // 3. Guarda los cambios
        every { usuarioRepository.save(any()) } answers { firstArg() }
        // 4. Genera la credencial VIP nueva
        every { jwtUtils.generarToken(any()) } returns "token_super_nuevo"

        // Act
        val resultado = usuarioService.actualizarPerfil(authMock, dtoActualizacion)

        // Assert
        assertThat(resultado.nombre).isEqualTo("Giuliana Nueva")
        assertThat(resultado.email).isEqualTo("giuli.nueva@mail.com")
        assertThat(resultado.imagenUrl).isEqualTo("nueva_foto.png")
        assertThat(resultado.token).isEqualTo("token_super_nuevo")
    }

    @Test
    fun `cambiarContrasena - camino triste - falla si la contrasena actual no coincide`() {
        // Arrange
        val authMock = mockk<Authentication>()
        every { authMock.name } returns "giuli.m@mail.com"

        val usuarioMock = Usuario(nombre = "Giuliana", email = "giuli.m@mail.com", contrasena = "hash_viejo").apply { id = 1L }
        val dtoPass = CambioContrasenaDTO(contrasenaActual = "clave_equivocada", contrasenaNueva = "nueva12345")

        every { usuarioRepository.findByEmail("giuli.m@mail.com") } returns Optional.of(usuarioMock)

        // Simulamos que el inspector de contraseñas dice: "Esta no es la llave correcta"
        every { passwordEncoder.matches(dtoPass.contrasenaActual, usuarioMock.contrasena) } returns false

        // Act & Assert
        assertThatThrownBy {
            usuarioService.cambiarContrasena(authMock, dtoPass)
        }.hasMessage("La contraseña actual es incorrecta")
    }

    // ==========================================
    // TESTS DE CONSULTAS Y AMISTAD
    // ==========================================

    @Test
    fun `obtenerUsuario - camino triste - lanza excepcion si el ID no existe`() {
        // Arrange: Simulamos que buscamos un ID que no está en la base
        every { usuarioRepository.findById(99L) } returns Optional.empty()

        // Act & Assert
        assertThatThrownBy {
            usuarioService.obtenerUsuario(99L)
        }.hasMessage("Usuario inexistente")
    }

    @Test
    fun `obtenerAmigos - camino feliz - devuelve la lista mapeada a DTO`() {
        // Arrange
        val authMock = mockk<Authentication>()
        every { authMock.name } returns "giuli@mail.com"

        // Creamos un amigo de prueba
        val amigoMock = Usuario(nombre = "Hermano", email = "hermano@mail.com", contrasena = "hash").apply {
            id = 2L
            imagenUrl = ""
        }

        // Creamos nuestro usuario y le agregamos el amigo a su lista
        val usuarioMock = Usuario(nombre = "Giuliana", email = "giuli@mail.com", contrasena = "hash").apply {
            id = 1L
            amigos = mutableListOf(amigoMock) // Simulamos que ya son amigos
        }

        every { usuarioRepository.findByEmail("giuli@mail.com") } returns Optional.of(usuarioMock)
        every { fraccionGastoRepository.findFraccionesPendientesEntre(any(), any(), any()) } returns emptyList()

        // Act
        val resultado = usuarioService.obtenerAmigos(authMock)

        // Assert: Verificamos que se mapeó bien al DTO
        assertThat(resultado).hasSize(1)
        assertThat(resultado[0].id).isEqualTo(2L)
        assertThat(resultado[0].nombre).isEqualTo("Hermano")
    }

    @Test
    fun `enviarSolicitudAmistad - camino feliz - guarda la solicitud y devuelve el ID`() {
        // Arrange
        val authMock = mockk<Authentication>()
        every { authMock.name } returns "giuli@mail.com"

        val emisorMock = Usuario(nombre = "Giuliana", email = "giuli@mail.com", contrasena = "hash").apply { id = 1L }

        every { usuarioRepository.findByEmail("giuli@mail.com") } returns Optional.of(emisorMock)

        // Simulamos que el repositorio guarda la solicitud sin problemas
        every { solicitudAmistadRepository.save(any()) } answers { firstArg() }
        every { usuarioRepository.existsByIdAndAmigosId(any(), any()) } returns false

        // ¡Acá está el machete que faltaba!
        every { solicitudAmistadRepository.existsByIdEmisorAndIdReceptor(any(), any()) } returns false

        val idReceptor = 2L

        // Act
        val resultado = usuarioService.enviarSolicitudAmistad(authMock, idReceptor)

        // Assert
        assertThat(resultado).isEqualTo(idReceptor)
    }

    @Test
    fun `enviarSolicitudAmistad - camino triste - falla si se envia a si mismo`() {
        // Arrange
        val authMock = mockk<Authentication>()
        every { authMock.name } returns "giuli@mail.com"

        // El usuario tiene ID 1
        val emisorMock = Usuario(nombre = "Giuliana", email = "giuli@mail.com", contrasena = "hash").apply { id = 1L }
        every { usuarioRepository.findByEmail("giuli@mail.com") } returns Optional.of(emisorMock)

        // Act & Assert: Intentamos mandar una solicitud al mismo ID 1
        assertThatThrownBy {
            usuarioService.enviarSolicitudAmistad(authMock, 1L)
        }.hasMessage("No podés enviarte una solicitud a vos mismo")
    }

    // ==========================================
    // TESTS RESTANTES: AMISTAD Y GRUPOS
    // ==========================================

    @Test
    fun `responderSolicitudAmistad - camino feliz - acepta solicitud y se agregan como amigos`() {
        // Arrange
        val authMock = mockk<Authentication>()
        every { authMock.name } returns "giuli@mail.com"

        val usuarioLogueado = Usuario(nombre = "Giuliana", email = "giuli@mail.com", contrasena = "").apply { id = 1L }
        val emisorMock = Usuario(nombre = "Hermano", email = "hermano@mail.com", contrasena = "").apply { id = 2L }

        // La solicitud dice: el emisor 2 le mandó al receptor 1
        val solicitudMock = ar.edu.unsam.proyectosoftware.domain.SolicitudAmistad(2L, 1L).apply { id = 100L }

        every { usuarioRepository.findByEmail("giuli@mail.com") } returns Optional.of(usuarioLogueado)
        every { solicitudAmistadRepository.findById(100L) } returns Optional.of(solicitudMock)
        every { usuarioRepository.findById(2L) } returns Optional.of(emisorMock) // Busca al emisor

        every { usuarioRepository.save(any()) } answers { firstArg() }
        every { solicitudAmistadRepository.delete(any()) } returns Unit // Unit es el "void" de Kotlin
        every { usuarioRepository.existsByIdAndAmigosId(any(), any()) } returns false
        every { solicitudAmistadRepository.deleteByIdEmisorAndIdReceptor(any(), any()) } returns Unit

        // Act
        val resultado = usuarioService.responderSolicitudAmistad(authMock, 100L, aceptar = true)

        // Assert
        assertThat(resultado).isEqualTo(1L)
        assertThat(usuarioLogueado.amigos).contains(emisorMock)
        assertThat(emisorMock.amigos).contains(usuarioLogueado)
    }

    @Test
    fun `responderSolicitudAmistad - camino triste - falla si respondes la solicitud de otro`() {
        // Arrange
        val authMock = mockk<Authentication>()
        every { authMock.name } returns "giuli@mail.com"
        val usuarioLogueado = Usuario(nombre = "Giuliana", email = "giuli@mail.com", contrasena = "").apply { id = 1L }

        // Una solicitud que era para el usuario 99, no para el 1
        val solicitudAjena = ar.edu.unsam.proyectosoftware.domain.SolicitudAmistad(2L, 99L).apply { id = 100L }

        every { usuarioRepository.findByEmail("giuli@mail.com") } returns Optional.of(usuarioLogueado)
        every { solicitudAmistadRepository.findById(100L) } returns Optional.of(solicitudAjena)

        // Act & Assert
        assertThatThrownBy {
            usuarioService.responderSolicitudAmistad(authMock, 100L, aceptar = true)
        }.hasMessage("No tenés permiso para responder esta solicitud")
    }

    @Test
    fun `obtenerUsuariosPorGrupo - camino feliz - devuelve los integrantes`() {
        // Arrange
        val grupoId = 10L
        val usuario1 = Usuario(nombre = "Giuli", email = "g@g.com", contrasena = "").apply { id = 1L }
        val usuario2 = Usuario(nombre = "Novio", email = "n@n.com", contrasena = "").apply { id = 2L }

        // Simulamos que el grupo existe
        every { grupoRepository.findById(grupoId) } returns Optional.of(mockk())
        // Simulamos que el repo de integrantes devuelve a nuestros dos usuarios
        every { integranteGrupoRepository.findAllByGrupoId(grupoId) } returns listOf(usuario1, usuario2)

        // Act
        val resultado = usuarioService.obtenerUsuariosPorGrupo(grupoId)

        // Assert
        assertThat(resultado).hasSize(2)
        assertThat(resultado.map { it.nombre }).containsExactly("Giuli", "Novio")
    }
}

