package ar.edu.unsam.proyectosoftware.controller

import ar.edu.unsam.proyectosoftware.domain.Grupo
import ar.edu.unsam.proyectosoftware.dto.GrupoCreacionDTO
import ar.edu.unsam.proyectosoftware.dto.GrupoNombreIdDTO
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

// Le decimos a Spring: "Levantá solo la capa web (los controllers) para este test"
@WebMvcTest(GrupoController::class)
@AutoConfigureMockMvc(addFilters = false) // Apagamos los filtros de seguridad globales para probar el controller puro
class GrupoControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc // Nuestro simulador de peticiones HTTP (Postman/Axios automático)

    @Autowired
    lateinit var objectMapper: ObjectMapper // Transforma objetos de Kotlin a JSON y viceversa

    // Usamos @MockkBean en vez de @MockK para que Spring reemplace los services reales en su contexto web
    @MockkBean
    lateinit var grupoService: GrupoService

    @MockkBean
    lateinit var usuarioService: UsuarioService

    @MockkBean
    lateinit var jwtUtils: ar.edu.unsam.proyectosoftware.security.JwtUtils

    @Test
    fun `GET grupo por ID - debe devolver 200 OK y el JSON con el nombre del grupo`() {
        // Arrange
        val grupoId = 1L
        val dtoEsperado = GrupoNombreIdDTO(grupoId, "Viaje a Bariloche")

        // El controller va a llamar a esta función, así que le decimos qué devolver
        every { grupoService.obtenerGrupoNombrePorId(grupoId) } returns dtoEsperado

        // Act & Assert: Simulamos el GET a la URL exacta
        mockMvc.perform(get("/grupo/$grupoId"))
                .andExpect(status().isOk) // Verificamos el código HTTP 200
                .andExpect(jsonPath("$.id").value(1L)) // Inspeccionamos que el JSON tenga el ID
                .andExpect(jsonPath("$.nombre").value("Viaje a Bariloche")) // Inspeccionamos que el JSON tenga el nombre
    }

    @Test
    fun `POST crearGrupo - debe devolver 201 Created y el JSON del nuevo grupo`() {
        // Arrange
        // Simulamos una credencial de usuario para pasar la validación del controller
        val authFalsa = UsernamePasswordAuthenticationToken("giuli@mail.com", null, emptyList())

        val dtoPeticion = GrupoCreacionDTO(
                nombre = "Asado del finde",
                descripcion = "Comida",
                imagenUrl = null,
                integrantesIds = emptyList()
        )

        val grupoGuardado = Grupo(nombre = "Asado del finde", descripcion = "Comida", imagenUrl = "", fechaCreacion = java.time.LocalDate.now()).apply { id = 10L }

        // Cuando el controller llame al service para crear el grupo, devolvemos el grupo creado
        every { grupoService.crearGrupo(any(), any()) } returns grupoGuardado

        // Act & Assert: Simulamos el POST con un Body JSON
        mockMvc.perform(
                        post("/grupo")
                                .principal(authFalsa) // Mandamos el "token" en la petición
                                .contentType(MediaType.APPLICATION_JSON) // Avisamos que mandamos JSON
                                .content(objectMapper.writeValueAsString(dtoPeticion)) // Convertimos el DTO a JSON de texto
                )
                .andExpect(status().isCreated) // Verificamos el código HTTP 201
                .andExpect(header().string("Location", "/grupo/10")) // Verificamos que devuelva la URL del nuevo recurso
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.nombre").value("Asado del finde"))
    }

    @Test
    fun `PATCH actualizarSettings - debe devolver 200 OK y el grupo actualizado`() {
        // Arrange
        val authFalsa = UsernamePasswordAuthenticationToken("giuli@mail.com", null, emptyList())
        val grupoId = 1L

        // El DTO que entra
        val dtoPeticion = ar.edu.unsam.proyectosoftware.dto.GrupoSettingsDTO(
            id = grupoId,
            nombre = "Nombre Nuevo",
            descripcion = "Descripcion Nueva",
            imagenUrl = "",
            moneda = "ARS",
            simplificarDeudas = true,
            notificaciones = true,
            modoRata = false
        )

        // El Grupo que simula devolver el Service
        val grupoActualizado = Grupo(nombre = "Nombre Nuevo", descripcion = "Descripcion Nueva", imagenUrl = "", fechaCreacion = java.time.LocalDate.now()).apply {
            id = grupoId
            moneda = "ARS"
            simplificarDeudas = true
            notificaciones = true
            modoRata = false
        }

        every { grupoService.actualizarGrupo(grupoId, any(), any()) } returns grupoActualizado

        // Act & Assert
        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/grupo/$grupoId/settings")
                .principal(authFalsa)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dtoPeticion))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.nombre").value("Nombre Nuevo"))
            .andExpect(jsonPath("$.simplificarDeudas").value(true))
    }

    @Test
    fun `DELETE eliminarGrupo - debe devolver 200 OK y el ID del grupo borrado`() {
        // Arrange
        val authFalsa = UsernamePasswordAuthenticationToken("giuli@mail.com", null, emptyList())
        val grupoId = 10L

        // Como la función en el Service no devuelve nada (es Unit/void), le decimos que simplemente retorne Unit
        every { grupoService.eliminarGrupo(grupoId, any()) } returns Unit

        // Act & Assert
        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/grupo/$grupoId")
                .principal(authFalsa)
        )
            .andExpect(status().isOk)
            .andExpect(content().string("10")) // Tu controller devuelve el ID suelto como respuesta
    }
}