package ar.edu.unsam.proyectosoftware.service

import ar.edu.unsam.proyectosoftware.BusinessException
import ar.edu.unsam.proyectosoftware.domain.Actividad
import ar.edu.unsam.proyectosoftware.domain.Grupo
import ar.edu.unsam.proyectosoftware.domain.Usuario
import ar.edu.unsam.proyectosoftware.dto.FilterActividadDTO
import ar.edu.unsam.proyectosoftware.dto.TipoActividad
import ar.edu.unsam.proyectosoftware.repository.ActividadRepository
import ar.edu.unsam.proyectosoftware.repository.UsuarioRepository
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.security.core.Authentication
import java.time.LocalDateTime
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ActividadServiceTest {

    private val actividadRepository = mockk<ActividadRepository>()
    private val usuarioRepository = mockk<UsuarioRepository>()
    private val objectMapper = ObjectMapper()

    private val service = ActividadService(
        actividadRepository = actividadRepository,
        usuarioRepository = usuarioRepository,
        objectMapper = objectMapper,
    )

    @Test
    fun `obtiene actividades del usuario logueado en orden por fecha descendente`() {
        val auth = mockk<Authentication>()
        every { auth.name } returns "test@correo.com"

        val usuario = Usuario(nombre = "Pepe", email = "test@correo.com", contrasena = "hash").also { it.id = 7L }
        val actividad = Actividad(
            usuario = usuario,
            tipo = TipoActividad.INGRESO_GRUPO,
            fecha = LocalDateTime.now(),
            descripcion = "Te agregaron a un grupo",
            metadata = "{\"grupoId\":12}"
        ).also { it.id = 50L }

        every { usuarioRepository.findByEmail("test@correo.com") } returns Optional.of(usuario)

        val pageableSlot = slot<Pageable>()
        val specSlot = slot<Specification<Actividad>>()
        every {
            actividadRepository.findAll(
                capture(specSlot),
                capture(pageableSlot)
            )
        } returns PageImpl(listOf(actividad))

        val result = service.obtenerActividadesDelUsuarioLogueado(auth, FilterActividadDTO())

        assertEquals(1, result.content.size)
        assertEquals(TipoActividad.INGRESO_GRUPO, result.content.first().tipo)
        assertEquals(6, pageableSlot.captured.pageSize)
        assertTrue(pageableSlot.captured.sort.getOrderFor("fecha")?.isDescending == true)
    }

    @Test
    fun `lanza error si el rango de fechas es invalido`() {
        val auth = mockk<Authentication>()
        every { auth.name } returns "test@correo.com"

        val filters = FilterActividadDTO(
            desde = LocalDateTime.of(2026, 4, 26, 15, 0),
            hasta = LocalDateTime.of(2026, 4, 25, 15, 0),
        )

        assertThrows<BusinessException> {
            service.obtenerActividadesDelUsuarioLogueado(auth, filters)
        }
    }

    @Test
    fun `registra ingreso a grupo con metadata canonica`() {
        val usuario = Usuario(nombre = "Pepe", email = "pepe@correo.com", contrasena = "hash").also { it.id = 7L }
        val actor = Usuario(nombre = "Admin", email = "admin@correo.com", contrasena = "hash").also { it.id = 1L }
        val grupo = Grupo(nombre = "Plan de Finde", descripcion = "", imagenUrl = "", fechaCreacion = LocalDateTime.now().toLocalDate()).also { it.id = 10L }

        val savedSlot = slot<Actividad>()
        every { actividadRepository.save(capture(savedSlot)) } answers { savedSlot.captured }

        val result = service.registrarIngresoGrupo(usuario = usuario, grupo = grupo, actor = actor)
            val metadata = requireNotNull(result.metadata)

        assertEquals(TipoActividad.INGRESO_GRUPO, result.tipo)
        assertEquals(usuario, result.usuario)
            assertTrue(metadata.contains("\"grupoId\":10"))
            assertTrue(metadata.contains("\"grupoNombre\":\"Plan de Finde\""))
            assertTrue(metadata.contains("\"usuarioId\":7"))
            assertTrue(metadata.contains("\"usuarioNombre\":\"Pepe\""))
            assertTrue(metadata.contains("\"actorId\":1"))
            assertTrue(metadata.contains("\"actorNombre\":\"Admin\""))
            assertFalse(metadata.contains("Creaste el grupo"))
    }
}

