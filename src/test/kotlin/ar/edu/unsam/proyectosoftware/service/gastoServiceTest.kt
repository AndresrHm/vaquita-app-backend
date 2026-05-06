package ar.edu.unsam.proyectosoftware.service

import ar.edu.unsam.proyectosoftware.BusinessException
import ar.edu.unsam.proyectosoftware.domain.enums.CategoriaGasto
import ar.edu.unsam.proyectosoftware.domain.enums.TipoGasto
import ar.edu.unsam.proyectosoftware.domain.Gasto
import ar.edu.unsam.proyectosoftware.domain.Grupo
import ar.edu.unsam.proyectosoftware.domain.Usuario
import ar.edu.unsam.proyectosoftware.dto.CreacionGastoDTO
import ar.edu.unsam.proyectosoftware.dto.ItemGastoDTO
import ar.edu.unsam.proyectosoftware.repository.*
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.core.Authentication
import java.time.LocalDate
import java.util.*

@ExtendWith(MockKExtension::class)
class GastoServiceTest {

    @MockK lateinit var fraccionGastoRepository: FraccionGastoRepository
    @MockK lateinit var gastoRepository: GastoRepository
    @MockK lateinit var integranteGrupoRepository: IntegranteGrupoRepository
    @MockK lateinit var grupoRepository: GrupoRepository
    @MockK lateinit var usuarioRepository: UsuarioRepository
    @MockK lateinit var usuarioService: UsuarioService
    @MockK(relaxed = true) lateinit var actividadService: ActividadService

    @InjectMockKs
    lateinit var gastoService: GastoService

    // ==========================================
    // TESTS DE CREACIÓN DE GASTOS
    // ==========================================

    @Test
    fun `crearGasto - camino feliz - guarda el gasto y las fracciones correctamente`() {
        // Arrange
        val authMock = mockk<Authentication>()
        val usuarioLogueado = Usuario(nombre = "Giuliana", email = "g@g.com", contrasena = "").apply { id = 1L }

        // Agregamos la fecha de creación al grupo
        val grupoMock = Grupo(nombre = "Asado", descripcion = "", imagenUrl = "", fechaCreacion = LocalDate.now()).apply { id = 10L }

        // DTO actualizado con todos tus parámetros obligatorios
        val dto = CreacionGastoDTO(
            id = null,
            descripcion = "Carne y leña",
            categoria = CategoriaGasto.values().first(), // Tomamos una categoría cualquiera válida
            monto = 1000.0,
            fecha = LocalDate.now(),
            grupoID = 10L,
            pagadorID = 1L,
            participantesIDs = listOf(1L, 2L),
            items = listOf(ItemGastoDTO("Corte", 1.0, 1000.0)), // Actualizado a ItemGastoDTO con cantidad y precio
            tipoGasto = TipoGasto.PARTES_IGUALES,
            asignacionesExactas = null
        )

        // Mocks de dependencias
        every { usuarioService.obtenerUsuarioLogueado(authMock) } returns usuarioLogueado
        every { grupoRepository.findById(10L) } returns Optional.of(grupoMock)
        every { usuarioRepository.findById(1L) } returns Optional.of(usuarioLogueado)

        val participante2 = Usuario(nombre = "Amigo", email = "a@a.com", contrasena = "").apply { id = 2L }
        every { integranteGrupoRepository.findUsuariosByGrupoAndIds(10L, listOf(1L, 2L)) } returns listOf(usuarioLogueado, participante2)

        every { integranteGrupoRepository.existsByGrupoIdAndUsuarioId(10L, 1L) } returns true
        every { integranteGrupoRepository.findIdsQuePertenecen(10L, any()) } returns listOf(1L, 2L)

        every { gastoRepository.save(any()) } answers { firstArg() }
        // Le explicamos explícitamente a Kotlin qué tipo de iterable estamos esperando
        every { fraccionGastoRepository.saveAll(any<Iterable<ar.edu.unsam.proyectosoftware.domain.FraccionGasto>>()) } returns emptyList()

        // Act
        val gastoCreado = gastoService.crearGasto(authMock, dto)

        // Assert
        assertThat(gastoCreado.monto).isEqualTo(1000.0)
        assertThat(gastoCreado.descripcion).isEqualTo("Carne y leña")
        verify { gastoRepository.save(any()) }
        verify { fraccionGastoRepository.saveAll(any<Iterable<ar.edu.unsam.proyectosoftware.domain.FraccionGasto>>()) }
    }

    @Test
    fun `crearGasto - camino triste - falla si el monto es negativo`() {
        // Arrange
        val authMock = mockk<Authentication>()
        val dtoNegativo = CreacionGastoDTO(
            id = null,
            descripcion = "Gasto Fantasma",
            categoria = CategoriaGasto.values().first(),
            monto = -50.0,
            fecha = LocalDate.now(),
            grupoID = 10L,
            pagadorID = 1L,
            participantesIDs = listOf(1L),
            items = emptyList(),
            tipoGasto = TipoGasto.PARTES_IGUALES,
            asignacionesExactas = null
        )

        every { usuarioService.obtenerUsuarioLogueado(authMock) } returns mockk()
        every { grupoRepository.findById(10L) } returns Optional.of(mockk(relaxed = true))
        every { usuarioRepository.findById(1L) } returns Optional.of(mockk())
        every { integranteGrupoRepository.findUsuariosByGrupoAndIds(any(), any()) } returns listOf(mockk())

        // Act & Assert
        assertThatThrownBy {
            gastoService.crearGasto(authMock, dtoNegativo)
        }.isInstanceOf(BusinessException::class.java)
            .hasMessageContaining("monto") // Atrapa cualquier frase sobre el monto
    }

    @Test
    fun `crearGasto - camino triste - falla si la descripcion es demasiado larga`() {
        // Arrange
        val authMock = mockk<Authentication>()
        val descMuyLarga = "a".repeat(51) // El máximo es 50
        val dtoLargo = CreacionGastoDTO(
            id = null,
            descripcion = descMuyLarga,
            categoria = CategoriaGasto.values().first(),
            monto = 100.0,
            fecha = LocalDate.now(),
            grupoID = 10L,
            pagadorID = 1L,
            participantesIDs = listOf(1L),
            items = listOf(ItemGastoDTO("X", 1.0, 100.0)),
            tipoGasto = TipoGasto.PARTES_IGUALES,
            asignacionesExactas = null
        )

        every { usuarioService.obtenerUsuarioLogueado(authMock) } returns mockk()
        every { grupoRepository.findById(10L) } returns Optional.of(mockk(relaxed = true))
        every { usuarioRepository.findById(1L) } returns Optional.of(mockk())
        every { integranteGrupoRepository.findUsuariosByGrupoAndIds(any(), any()) } returns listOf(mockk())

        // Act & Assert
        assertThatThrownBy {
            gastoService.crearGasto(authMock, dtoLargo)
        }.isInstanceOf(BusinessException::class.java)
            .hasMessageContaining("50") // Atrapa la validación de longitud
    }

    // ==========================================
    // TESTS DE DEUDA Y FILTROS
    // ==========================================

    @Test
    fun `obtenerDeuda - camino feliz - suma correctamente los saldos pendientes`() {
        // Arrange
        val authMock = mockk<Authentication>()
        val usuario = Usuario(nombre = "Giuli", email = "g@g.com", contrasena = "").apply { id = 1L }

        every { usuarioService.obtenerUsuarioLogueado(authMock) } returns usuario
        every { usuarioRepository.findById(1L) } returns Optional.of(usuario)

        // Magia de Kotlin: creamo mocks anónimos al vuelo para la interfaz SaldoPendiente
        every { fraccionGastoRepository.findByDeudorId(1L) } returns listOf(
            mockk { every { saldoPendiente() } returns 500.0 },
            mockk { every { saldoPendiente() } returns 300.0 }
        )

        // Act
        val deudaTotal = gastoService.obtenerDeuda(authMock)

        // Assert
        assertThat(deudaTotal).isEqualTo(800.0)
    }
}