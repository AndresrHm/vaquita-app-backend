package ar.edu.unsam.proyectosoftware.controller

import ar.edu.unsam.proyectosoftware.domain.enums.CategoriaGasto
import ar.edu.unsam.proyectosoftware.domain.enums.TipoGasto
import ar.edu.unsam.proyectosoftware.domain.Gasto
import ar.edu.unsam.proyectosoftware.domain.Grupo
import ar.edu.unsam.proyectosoftware.dto.CreacionGastoDTO
import ar.edu.unsam.proyectosoftware.dto.GastoDetalleDTO
import ar.edu.unsam.proyectosoftware.dto.PageResponse
import ar.edu.unsam.proyectosoftware.security.JwtUtils
import ar.edu.unsam.proyectosoftware.service.GastoService
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.mockk
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
import java.time.LocalDate

@WebMvcTest(GastoController::class)
@AutoConfigureMockMvc(addFilters = false) // Apagamos los filtros de seguridad globales para probar directo el controller
class GastoControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockkBean
    lateinit var gastoService: GastoService

    // El escáner de credenciales para que no explote Spring Security
    @MockkBean
    lateinit var jwtUtils: JwtUtils

    @Test
    fun `POST crearGasto - debe devolver 200 OK y el mensaje de exito formateado`() {
        // Arrange
        val authFalsa = UsernamePasswordAuthenticationToken("giuli@mail.com", null, emptyList())
        val dtoPeticion = CreacionGastoDTO(
            id = null,
            descripcion = "Cena de festejo",
            categoria = CategoriaGasto.values().first(), // Tomamos una categoría por defecto
            monto = 15000.0,
            fecha = LocalDate.now(),
            grupoID = 10L,
            pagadorID = 1L,
            participantesIDs = listOf(1L, 2L),
            items = emptyList(),
            tipoGasto = TipoGasto.PARTES_IGUALES,
            asignacionesExactas = null
        )

        val grupoMock = Grupo(nombre = "Salidas", descripcion = "", imagenUrl = "", fechaCreacion = LocalDate.now()).apply { id = 10L }

        // Magia MockK: Simulamos el gasto abstracto sin importar qué tipo exacto sea
        val gastoMock = mockk<Gasto>(relaxed = true)
        every { gastoMock.monto } returns 15000.0
        every { gastoMock.grupo } returns grupoMock

        every { gastoService.crearGasto(any(), any()) } returns gastoMock

        // Act & Assert
        mockMvc.perform(
            post("/gasto")
                .principal(authFalsa) // Mandamos la credencial VIP
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dtoPeticion)) // Convertimos el DTO a JSON
        )
            .andExpect(status().isOk)
            // Tu controller devuelve un String puro, así que verificamos el 'content().string()' literal
            .andExpect(content().string("Gasto de $15000.0 creado exitosamente en Salidas."))
    }

    @Test
    fun `GET obtenerDeuda - debe devolver 200 OK y el monto exacto de la deuda`() {
        // Arrange
        val authFalsa = UsernamePasswordAuthenticationToken("giuli@mail.com", null, emptyList())

        // Simulamos que el usuario debe unos buenos 4500.50
        every { gastoService.obtenerDeuda(any()) } returns 4500.50

        // Act & Assert
        mockMvc.perform(
            get("/gasto/me/deuda")
                .principal(authFalsa)
        )
            .andExpect(status().isOk)
            .andExpect(content().string("4500.5")) // Ojo que los Double a veces omiten el 0 final
    }

    @Test
    fun `GET obtenerGastosFiltrados - debe devolver 200 OK y una pagina vacia si no hay gastos`() {
        // Arrange
        val authFalsa = UsernamePasswordAuthenticationToken("giuli@mail.com", null, emptyList())

        // Simulamos la respuesta de la paginación vacía
        val pageResponse = PageResponse<GastoDetalleDTO>(
            content = emptyList(),
            page = 0, // <-- Acá estaba el error, decía 'number'
            size = 10,
            totalElements = 0,
            totalPages = 0,
            hasNext = false
        )

        // No importa qué filtros le pasemos en este test básico, devolvemos la página mockeada
        every { gastoService.getGastosByFilters(any(), any()) } returns pageResponse

        // Act & Assert
        mockMvc.perform(
            get("/gasto/filtrar")
                .principal(authFalsa)
                .param("page", "0")
                .param("size", "10")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.totalElements").value(0)) // Validamos que el total de elementos sea 0
    }
}