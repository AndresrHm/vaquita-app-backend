package ar.edu.unsam.proyectosoftware.controller

import ar.edu.unsam.proyectosoftware.GastoService
import ar.edu.unsam.proyectosoftware.domain.Gasto
import ar.edu.unsam.proyectosoftware.dto.GastoDTO
import ar.edu.unsam.proyectosoftware.service.GrupoService
import ar.edu.unsam.proyectosoftware.service.UsuarioService
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("gasto")
@CrossOrigin(origins = ["*"])
class GastoController(
    val gastoService: GastoService,
    val usuarioService: UsuarioService,
    val grupoService: GrupoService,
) {

    @PostMapping
    fun crearGasto(@RequestBody gastoDTO: GastoDTO): String {
        val gasto = gastoService.crearGasto(gastoDTO)

        return "Gasto de $${gasto.monto} creado exitosamente en ${gasto.grupo.nombre}."
    }

}