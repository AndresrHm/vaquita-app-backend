package ar.edu.unsam.proyectosoftware.controller

import ar.edu.unsam.proyectosoftware.domain.Usuario
import ar.edu.unsam.proyectosoftware.dto.UsuarioEditarDTO
import ar.edu.unsam.proyectosoftware.dto.UsuarioLoginDTO
import ar.edu.unsam.proyectosoftware.dto.UsuarioLogueadoDTO
import ar.edu.unsam.proyectosoftware.dto.UsuarioRegistradoDTO
import ar.edu.unsam.proyectosoftware.security.JwtUtils
import ar.edu.unsam.proyectosoftware.service.GrupoService
import ar.edu.unsam.proyectosoftware.service.UsuarioService
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(UsuarioController::class)
@AutoConfigureMockMvc(addFilters = false) // Apagamos los filtros globales para no bloquear las rutas libres
class UsuarioControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockkBean
    lateinit var usuarioService: UsuarioService

    @MockkBean
    lateinit var grupoService: GrupoService

    // El escáner de credenciales falso para que no explote Spring Security
    @MockkBean
    lateinit var jwtUtils: JwtUtils

    @Test
    fun `POST login - debe devolver 200 OK y la credencial VIP del usuario`() {
        // Arrange
        val peticionLogin = UsuarioLoginDTO("giuli@mail.com", "miclave123")
        val respuestaEsperada = UsuarioLogueadoDTO(
            id = 1L,
            nombre = "Giuliana",
            email = "giuli@mail.com",
            token = "token_simulado_super_seguro",
            imagenUrl = null
        )

        every { usuarioService.login(any()) } returns respuestaEsperada

        // Act & Assert
        mockMvc.perform(
            post("/usuarios/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peticionLogin))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.nombre").value("Giuliana"))
            .andExpect(jsonPath("$.token").value("token_simulado_super_seguro"))
    }

    @Test
    fun `POST register - debe devolver 200 OK y el JSON del usuario recien creado`() {
        // Arrange
        val peticionRegistro = UsuarioRegistradoDTO("Giuliana Nueva", "giulinueva@mail.com", "secreta123")
        val usuarioGuardado = Usuario(nombre = "Giuliana Nueva", email = "giulinueva@mail.com", contrasena = "hash").apply { id = 2L }

        every { usuarioService.registrarUsuario(any()) } returns usuarioGuardado

        // Act & Assert
        mockMvc.perform(
            post("/usuarios/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peticionRegistro))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(2L))
            .andExpect(jsonPath("$.email").value("giulinueva@mail.com"))
    }

    @Test
    fun `GET obtenerMiPerfil - debe devolver 200 OK y mi nombre e ID`() {
        // Arrange
        val authFalsa = UsernamePasswordAuthenticationToken("giuli@mail.com", null, emptyList())
        val miUsuario = Usuario(nombre = "Giuliana", email = "giuli@mail.com", contrasena = "").apply { id = 1L }

        every { usuarioService.obtenerUsuarioLogueado(authFalsa) } returns miUsuario

        // Act & Assert
        mockMvc.perform(
            get("/usuarios/me")
                .principal(authFalsa) // Mostramos la credencial en la puerta
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.nombre").value("Giuliana"))
    }

    @Test
    fun `PUT actualizarMiPerfil - debe devolver 200 OK y el DTO con el perfil renovado`() {
        // Arrange
        val authFalsa = UsernamePasswordAuthenticationToken("giuli@mail.com", null, emptyList())

        val peticionEdicion = UsuarioEditarDTO(
            nombre = "Giuli Programadora",
            email = "giuli.dev@mail.com",
            imagenUrl = "foto_teclado.png"
        )

        val respuestaEsperada = UsuarioLogueadoDTO(
            id = 1L,
            nombre = "Giuli Programadora",
            email = "giuli.dev@mail.com",
            token = "token_renovado",
            imagenUrl = "foto_teclado.png"
        )

        every { usuarioService.actualizarPerfil(authFalsa, peticionEdicion) } returns respuestaEsperada

        // Act & Assert
        mockMvc.perform(
            put("/usuarios/me")
                .principal(authFalsa)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peticionEdicion))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.nombre").value("Giuli Programadora"))
            .andExpect(jsonPath("$.imagenUrl").value("foto_teclado.png"))
            .andExpect(jsonPath("$.token").value("token_renovado"))
    }

    // ==========================================
    // TESTS DE AMISTAD Y SEGURIDAD
    // ==========================================

    @Test
    fun `PATCH cambiarContrasena - debe devolver 200 OK y el mensaje de exito`() {
        // Arrange
        val authFalsa = UsernamePasswordAuthenticationToken("giuli@mail.com", null, emptyList())
        val peticionClave = ar.edu.unsam.proyectosoftware.dto.CambioContrasenaDTO("clave_vieja123", "clave_nueva_456")

        // El service devuelve Unit (void) cuando todo sale bien
        every { usuarioService.cambiarContrasena(any(), any()) } returns Unit

        // Act & Assert
        mockMvc.perform(
            patch("/usuarios/me/contrasena")
                .principal(authFalsa)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peticionClave))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.message").value("Contraseña actualizada correctamente"))
    }

    @Test
    fun `POST enviarSolicitudAmistad - debe devolver 200 OK y el ID del receptor`() {
        // Arrange
        val authFalsa = UsernamePasswordAuthenticationToken("giuli@mail.com", null, emptyList())
        val idReceptor = 5L

        every { usuarioService.enviarSolicitudAmistad(any(), idReceptor) } returns idReceptor

        // Act & Assert
        mockMvc.perform(
            post("/usuarios/me/invitar/$idReceptor")
                .principal(authFalsa)
        )
            .andExpect(status().isOk)
            .andExpect(content().string("5")) // Como devuelve un Long directo, validamos el body como texto
    }

    @Test
    fun `PATCH responderSolicitudAmistad - debe devolver 200 OK pasando el parametro aceptar`() {
        // Arrange
        val authFalsa = UsernamePasswordAuthenticationToken("giuli@mail.com", null, emptyList())
        val idSolicitud = 10L
        val miId = 1L

        every { usuarioService.responderSolicitudAmistad(any(), idSolicitud, true) } returns miId

        // Act & Assert
        // Acá probamos cómo pasarle el @RequestParam("aceptar") usando .param()
        mockMvc.perform(
            patch("/usuarios/me/solicitud/$idSolicitud")
                .principal(authFalsa)
                .param("aceptar", "true")
        )
            .andExpect(status().isOk)
            .andExpect(content().string("1"))
    }

    @Test
    fun `DELETE eliminarAmigo - debe devolver 204 No Content`() {
        // Arrange
        val idUsuario = 1L
        val idAmigo = 2L

        every { usuarioService.eliminarAmigo(idUsuario, idAmigo) } returns Unit

        // Act & Assert
        mockMvc.perform(
            delete("/usuarios/$idUsuario/amigos/$idAmigo")
        )
            .andExpect(status().isNoContent) // Verifica el código HTTP 204 que pusiste en el controller
    }
}