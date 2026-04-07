package ar.edu.unsam.proyectosoftware.controller

import ar.edu.unsam.proyectosoftware.dto.GroupPageResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("grupo")
class GrupoController (

) {
    @GetMapping
    fun obtenerGrupos(usuarioId: Long): GroupPageResponse {
        // TODO
    }
}