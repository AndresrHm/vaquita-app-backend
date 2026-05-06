package ar.edu.unsam.proyectosoftware.service

import ar.edu.unsam.proyectosoftware.BusinessException
import ar.edu.unsam.proyectosoftware.domain.Grupo
import ar.edu.unsam.proyectosoftware.domain.Usuario
import ar.edu.unsam.proyectosoftware.dto.GrupoCreacionDTO
import ar.edu.unsam.proyectosoftware.repository.*
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.core.Authentication
import java.util.Optional

@ExtendWith(MockKExtension::class)
class GrupoServiceTest {

    // 1. INYECTAMOS LOS SIMULACROS
    @MockK lateinit var grupoRepository: GrupoRepository
    @MockK lateinit var integranteGrupoRepository: IntegranteGrupoRepository
    @MockK lateinit var usuarioRepository: UsuarioRepository
    @MockK lateinit var gastoRepository: GastoRepository
    @MockK lateinit var usuarioService: UsuarioService // Simulamos al otro empleado

    @InjectMockKs
    lateinit var grupoService: GrupoService

    // ==========================================
    // TESTS DE CREACIÓN DE GRUPOS
    // ==========================================

    @Test
    fun `crearGrupo - camino feliz - guarda el grupo y asigna al creador como ADMIN`() {
        // Arrange
        val authMock = mockk<Authentication>()
        val usuarioCreador = Usuario(nombre = "Giuliana", email = "giuli@mail.com", contrasena = "").apply { id = 1L }

        val dto = GrupoCreacionDTO(
            nombre = "Viaje al Sur",
            descripcion = "Gastos compartidos",
            imagenUrl = null,
            integrantesIds = emptyList() // Sin integrantes extra por ahora
        )

        // Simulamos que el UsuarioService nos dice quién está logueado
        every { usuarioService.obtenerUsuarioLogueado(authMock) } returns usuarioCreador
        // Simulamos que la base de datos guarda y devuelve el mismo grupo
        every { grupoRepository.save(any()) } answers { firstArg() }

        // Act
        val grupoGuardado = grupoService.crearGrupo(authMock, dto)

        // Assert
        assertThat(grupoGuardado.nombre).isEqualTo("Viaje al Sur")
        assertThat(grupoGuardado.integrantes).hasSize(1)

        val integranteCreador = grupoGuardado.integrantes.first()
        assertThat(integranteCreador.usuario.id).isEqualTo(1L)
        // Verificamos el rol directamente
        assertThat(integranteCreador.rol).isEqualTo(ar.edu.unsam.proyectosoftware.domain.RolIntegrante.ADMIN)
    }

    @Test
    fun `crearGrupo - camino triste - falla si el nombre viene vacio o nulo`() {
        // Arrange
        val authMock = mockk<Authentication>()
        val usuarioCreador = Usuario(nombre = "Giuliana", email = "giuli@mail.com", contrasena = "").apply { id = 1L }

        val dtoSinNombre = GrupoCreacionDTO(
            nombre = "   ", // Nombre en blanco
            descripcion = "Test",
            imagenUrl = null,
            integrantesIds = emptyList()
        )

        every { usuarioService.obtenerUsuarioLogueado(authMock) } returns usuarioCreador

        // Act & Assert
        assertThatThrownBy {
            grupoService.crearGrupo(authMock, dtoSinNombre)
        }.isInstanceOf(BusinessException::class.java)
            .hasMessage("El nombre del grupo es obligatorio")
    }

    // ==========================================
    // TESTS DE OBTENCIÓN Y VALIDACIÓN
    // ==========================================

    @Test
    fun `obtenerDetalleGrupo - camino triste - rechaza el acceso si el usuario no es miembro`() {
        // Arrange
        val authMock = mockk<Authentication>()
        val usuarioIntruso = Usuario(nombre = "Intruso", email = "intruso@mail.com", contrasena = "").apply { id = 99L }
        val grupoId = 10L

        every { usuarioService.obtenerUsuarioLogueado(authMock) } returns usuarioIntruso
        // El validador dice: "Este usuario NO es miembro de este grupo"
        every { integranteGrupoRepository.esMiembro(grupoId, 99L) } returns false

        // Act & Assert
        assertThatThrownBy {
            grupoService.obtenerDetalleGrupo(grupoId, authMock)
        }.isInstanceOf(BusinessException::class.java)
            .hasMessageContaining("El usuario no pertenece al grupo seleccionado")
    }

    // ==========================================
    // TESTS DE ELIMINACIÓN DE MIEMBROS
    // ==========================================

    @Test
    fun `eliminarMiembro - camino triste - falla si un miembro intenta echar a otro y no es admin`() {
        // Arrange
        val authMock = mockk<Authentication>()
        val usuarioMiembroNormal = Usuario(nombre = "Comun", email = "c@mail.com", contrasena = "").apply { id = 2L }
        val grupoId = 10L
        val usuarioAEliminarId = 3L

        val grupoMock = mockk<Grupo>(relaxed = true)

        // Simulamos la proyección que devuelve la query de tu repositorio
        val projectionMiembroActual = mockk<MiembroProjection>()
        every { projectionMiembroActual.id } returns 2L
        every { projectionMiembroActual.esAdmin } returns false // <-- NO ES ADMIN

        val projectionAEliminar = mockk<MiembroProjection>()
        every { projectionAEliminar.id } returns 3L
        every { projectionAEliminar.esAdmin } returns false

        every { usuarioService.obtenerUsuarioLogueado(authMock) } returns usuarioMiembroNormal
        every { grupoRepository.findById(grupoId) } returns Optional.of(grupoMock)
        every { integranteGrupoRepository.obtenerMiembrosConRol(grupoId) } returns listOf(projectionMiembroActual, projectionAEliminar)

        // Act & Assert
        assertThatThrownBy {
            grupoService.eliminarMiembro(grupoId, authMock, usuarioAEliminarId)
        }.isInstanceOf(BusinessException::class.java)
            .hasMessageContaining("El usuario no es administrador del grupo.") // <-- Texto actualizado
    }

    @Test
    fun `eliminarMiembro - camino triste - falla si el usuario intenta eliminarse a si mismo`() {
        // Arrange
        val authMock = mockk<Authentication>()
        val usuarioLogueadoId = 1L
        val grupoId = 10L

        val usuarioLogueado = Usuario(nombre = "Admin", email = "admin@mail.com", contrasena = "").apply { id = usuarioLogueadoId }
        val grupoMock = mockk<Grupo>(relaxed = true)

        // Creamos la proyección de forma limpia, sin anidamientos raros
        val projectionAdmin = mockk<MiembroProjection>()
        every { projectionAdmin.id } returns usuarioLogueadoId
        every { projectionAdmin.esAdmin } returns true

        every { usuarioService.obtenerUsuarioLogueado(authMock) } returns usuarioLogueado
        every { grupoRepository.findById(grupoId) } returns Optional.of(grupoMock)
        every { integranteGrupoRepository.obtenerMiembrosConRol(grupoId) } returns listOf(projectionAdmin)

        // Act & Assert
        assertThatThrownBy {
            // Intentamos eliminar el mismo ID del usuario logueado
            grupoService.eliminarMiembro(grupoId, authMock, usuarioLogueadoId)
        }.isInstanceOf(BusinessException::class.java)
            .hasMessageContaining("No podés eliminarte a vos mismo") // Ajustalo a tu ErrorStrings.NO_PODES_ELIMINARTE si es distinto
    }
}