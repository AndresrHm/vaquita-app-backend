package ar.edu.unsam.proyectosoftware.controller

import ar.edu.unsam.proyectosoftware.domain.Grupo
import ar.edu.unsam.proyectosoftware.dto.GastosPorAnioDTO
import ar.edu.unsam.proyectosoftware.dto.GastosPorMesDTO
import ar.edu.unsam.proyectosoftware.dto.GrupoCreacionDTO
import ar.edu.unsam.proyectosoftware.dto.GrupoDTO
import ar.edu.unsam.proyectosoftware.dto.GrupoDetalleDTO
import ar.edu.unsam.proyectosoftware.dto.GrupoNombreIdDTO
import ar.edu.unsam.proyectosoftware.dto.PageResponse
import ar.edu.unsam.proyectosoftware.dto.toPageResponse
import ar.edu.unsam.proyectosoftware.mapper.GastoMapper
import ar.edu.unsam.proyectosoftware.mapper.GrupoMapper
import ar.edu.unsam.proyectosoftware.service.GrupoService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.time.format.TextStyle
import java.util.Locale

// todo meter autenticación

@RestController
@RequestMapping("grupo")
@CrossOrigin(origins = ["http://localhost:5173"])
class GrupoController {
    @Autowired
    lateinit var grupoService: GrupoService

    @GetMapping
    fun obtenerGrupos(
        @RequestParam usuarioId: Long,
        @RequestParam page: Int,
        @RequestParam size: Int,
        // falta un poco para meter esto @RequestParam tipoFiltrado: TipoFiltrado,
    ): PageResponse<GrupoDTO> {
        val resultPage = grupoService.obtenerGrupos(usuarioId, page, size)

        return resultPage.toPageResponse { GrupoMapper.toGrupoDTO(it) }
    }

    @GetMapping("/usuario/{usuarioId}")
    fun obtenerGruposDeUsuario(
        @PathVariable usuarioId: Long,
    ): ResponseEntity<Iterable<GrupoNombreIdDTO>> {
        val grupos = grupoService.obtenerGruposDeUsuario(usuarioId)
        val response = grupos.map { Grupo.toGrupoNombreIdDTO(it) }
        return ResponseEntity.ok(response)
    }

    @PostMapping
    fun crearGrupo(
        @RequestParam usuarioId: Long,
        @RequestBody dto: GrupoCreacionDTO,
    ): ResponseEntity<GrupoDTO> {

        val grupo = grupoService.crearGrupo(usuarioId, dto)

        return ResponseEntity
            .created(URI.create("/grupos/${grupo.id}"))
            .body(GrupoMapper.toGrupoDTO(grupo))
    }

    // Podríamos pasar un type para obtener el detalle en groupDetail y en groupSettings
    @GetMapping("/{grupoId}/detalle")
    fun obtenerDetalleGrupo(
        @PathVariable grupoId: Long,
//        @RequestParam usuarioId: Long TODO: Meter autenticación
    ): ResponseEntity<GrupoDetalleDTO> {
        val grupo = grupoService.obtenerDetalleGrupo(grupoId)
        val cantIntegrantes = grupoService.obtenerCantIntegrantes(grupoId)
        return ResponseEntity.ok(GrupoMapper.toGrupoDetalleDTO(grupo, cantIntegrantes))
    }

    @GetMapping("/{id}/gastos")
    fun obtenerGastosPaginados(
        @PathVariable id: Long,
        @RequestParam page: Int = 0,
        @RequestParam size: Int = 10,
    ): ResponseEntity<PageResponse<GastosPorAnioDTO>> {

        val gastosPage = grupoService.obtenerGastosGrupo(id, page, size)

        val locale = Locale.forLanguageTag("es-AR")

        val agrupado = gastosPage.content
            .sortedByDescending { it.fecha }
            .groupBy { it.fecha.year }
            .toSortedMap(compareByDescending { it })
            .map { (anio, gastosDelAnio) ->
                GastosPorAnioDTO(
                    year = anio,
                    months = gastosDelAnio
                        .groupBy { it.fecha.month }
                        .toSortedMap(compareByDescending { it.value })
                        .map { (mes, gastosDelMes) ->
                            GastosPorMesDTO(
                                month = mes.getDisplayName(TextStyle.FULL, locale),
                                expenses = gastosDelMes.map {
                                    GastoMapper.toGastoItemDTO(it, 1L) // userId hardcodeado por ahora
                                }
                            )
                        }
                )
            }

        val response = PageResponse(
            content = agrupado,
            page = gastosPage.number,
            size = gastosPage.size,
            totalElements = gastosPage.totalElements,
            totalPages = gastosPage.totalPages,
            hasNext = gastosPage.hasNext()
        )

        return ResponseEntity.ok(response)
    }

    enum class TipoFiltrado {
        TODOS_LOS_GRUPOS,
        SALDOS_PENDIENTES,
        GRUPOS_A_LOS_QUE_DEBES,
        GRUPOS_QUE_TE_DEBEN
    }
}