package ar.edu.unsam.proyectosoftware.controller

import ar.edu.unsam.proyectosoftware.dto.GrupoDTO
import ar.edu.unsam.proyectosoftware.mapper.GrupoMapper
import ar.edu.unsam.proyectosoftware.service.GrupoService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

// todo meter autenticación

@RestController
@RequestMapping("grupo")
class GrupoController {
    @Autowired
    lateinit var grupoService: GrupoService

    @GetMapping
    fun obtenerGrupos(
        @RequestParam usuarioId: Long,
        @RequestParam page: Int,
        @RequestParam size: Int,
    ): Page<GrupoDTO> {
        return grupoService.obtenerGrupos(usuarioId, page, size).map { it -> GrupoMapper.grupoToGrupoDTO(it) }
    }
}