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
@AutoConfigureMockMvc(addFilters = false)
class UsuarioControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockkBean
    lateinit var usuarioService: UsuarioService

    @MockkBean
    lateinit var grupoService: GrupoService

    @MockkBean
    lateinit var jwtUtils: JwtUtils

    @Test
    fun `POST login - debe devolver 200 OK y ambos tokens`() {
        // Arrange
        val peticionLogin = UsuarioLoginDTO("giuli@mail.com", "miclave123")
        val respuestaEsperada = UsuarioLogueadoDTO(
            1L,
            "access_token_falso",
            "refresh_token_falso",
            "Giuliana",
            "giuli@mail.com",
            "giuli",
            null
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
            .andExpect(jsonPath("$.accessToken").value("access_token_falso")) // Cambio de $.token a $.accessToken
            .andExpect(jsonPath("$.refreshToken").value("refresh_token_falso"))
    }

    @Test
    fun `POST refresh - debe devolver 200 OK y nuevos tokens al renovar`() {
        // Arrange
        val peticion = mapOf("refreshToken" to "token_largo_viejo")
        val respuestaEsperada = UsuarioLogueadoDTO(
            1L,
            "NUEVO_access",
            "NUEVO_refresh",
            "Giuliana",
            "giuli@mail.com",
            "giuli",
            null
        )

        every { usuarioService.refrescarToken(any()) } returns respuestaEsperada

        // Act & Assert
        mockMvc.perform(
            post("/usuarios/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peticion))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").value("NUEVO_access"))
            .andExpect(jsonPath("$.refreshToken").value("NUEVO_refresh"))
    }

    @Test
    fun `PUT actualizarMiPerfil - debe devolver 200 OK y tokens renovados`() {
        // Arrange
        val authFalsa = UsernamePasswordAuthenticationToken("giuli@mail.com", null, emptyList())
        val peticionEdicion = UsuarioEditarDTO("Giuli Dev", "giuli.dev@mail.com", "foto.png")

        val respuestaEsperada = UsuarioLogueadoDTO(
            1L,
            "access_actualizado",
            "refresh_actualizado",
            "Giuli Dev",
            "giuli.dev@mail.com",
            "giuli.dev",
            "foto_teclado.png"
        )

        every { usuarioService.actualizarPerfil(any(), any()) } returns respuestaEsperada

        // Act & Assert
        mockMvc.perform(
            put("/usuarios/me")
                .principal(authFalsa)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peticionEdicion))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").value("access_actualizado"))
    }

    @Test
    fun `POST register - debe devolver 200 OK y el JSON del usuario`() {
        val peticionRegistro = UsuarioRegistradoDTO("Giuliana Nueva", "giulinueva@mail.com", "secreta123", "giulianan")
        val usuarioGuardado = Usuario(nombre = "Giuliana Nueva", email = "giulinueva@mail.com", contrasena = "hash", username = "giulianan").apply { id = 2L }
        every { usuarioService.registrarUsuario(any()) } returns usuarioGuardado

        mockMvc.perform(
            post("/usuarios/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peticionRegistro))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(2L))
    }

    @Test
    fun `GET obtenerMiPerfil - debe devolver 200 OK y mi nombre`() {
        val authFalsa = UsernamePasswordAuthenticationToken("giuli@mail.com", null, emptyList())
        val miUsuario = Usuario(nombre = "Giuliana", email = "giuli@mail.com", contrasena = "", username = "giuli").apply { id = 1L }
        every { usuarioService.obtenerUsuarioLogueado(any()) } returns miUsuario

        mockMvc.perform(get("/usuarios/me").principal(authFalsa))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.nombre").value("Giuliana"))
    }

    @Test
    fun `PATCH cambiarContrasena - debe devolver 200 OK`() {
        val authFalsa = UsernamePasswordAuthenticationToken("giuli@mail.com", null, emptyList())
        val peticionClave = ar.edu.unsam.proyectosoftware.dto.CambioContrasenaDTO("vieja", "nueva")
        every { usuarioService.cambiarContrasena(any(), any()) } returns Unit

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
    fun `POST enviarSolicitudAmistad - debe devolver 200 OK`() {
        val authFalsa = UsernamePasswordAuthenticationToken("giuli@mail.com", null, emptyList())
        every { usuarioService.enviarSolicitudAmistad(any(), 5L) } returns 5L

        mockMvc.perform(post("/usuarios/me/invitar/5").principal(authFalsa))
            .andExpect(status().isOk)
            .andExpect(content().string("5"))
    }

    @Test
    fun `PATCH responderSolicitudAmistad - debe devolver 200 OK`() {
        val authFalsa = UsernamePasswordAuthenticationToken("giuli@mail.com", null, emptyList())
        every { usuarioService.responderSolicitudAmistad(any(), 10L, true) } returns 1L

        mockMvc.perform(
            patch("/usuarios/me/solicitud/10")
                .principal(authFalsa)
                .param("aceptar", "true")
        )
            .andExpect(status().isOk)
            .andExpect(content().string("1"))
    }

    @Test
    fun `DELETE eliminarAmigo - debe devolver 204 No Content`() {
        every { usuarioService.eliminarAmigo(1L, 2L) } returns Unit
        mockMvc.perform(delete("/usuarios/1/amigos/2"))
            .andExpect(status().isNoContent)
    }
}