package ar.edu.unsam.proyectosoftware.controller

import ar.edu.unsam.proyectosoftware.domain.Grupo
import ar.edu.unsam.proyectosoftware.domain.Usuario
import ar.edu.unsam.proyectosoftware.dto.GrupoCreacionDTO
import ar.edu.unsam.proyectosoftware.dto.GrupoDTO
import ar.edu.unsam.proyectosoftware.dto.GrupoDetalleDTO
import ar.edu.unsam.proyectosoftware.dto.GrupoNombreIdDTO
import ar.edu.unsam.proyectosoftware.dto.PageResponse
import ar.edu.unsam.proyectosoftware.dto.UsuarioNombreIdDTO
import ar.edu.unsam.proyectosoftware.dto.toPageResponse
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

        return resultPage.toPageResponse { GrupoMapper.grupoToGrupoDTO(it) }
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
            .body(GrupoMapper.grupoToGrupoDTO(grupo))
    }

    @GetMapping("/{id}/detalle")
    fun obtenerDetalleGrupo(
        @PathVariable id: Long,
        @RequestParam usuarioId: Long
    ): ResponseEntity<GrupoDetalleDTO> {

        val resultado = grupoService.obtenerDetalleGrupo(id, usuarioId)
        return ResponseEntity.ok(resultado)
    }

    enum class TipoFiltrado {
        TODOS_LOS_GRUPOS,
        SALDOS_PENDIENTES,
        GRUPOS_A_LOS_QUE_DEBES,
        GRUPOS_QUE_TE_DEBEN
    }
}