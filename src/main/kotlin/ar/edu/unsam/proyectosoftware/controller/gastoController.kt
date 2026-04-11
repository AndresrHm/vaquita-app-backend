package ar.edu.unsam.proyectosoftware.controller

import ar.edu.unsam.proyectosoftware.service.GastoService
import ar.edu.unsam.proyectosoftware.dto.CreacionGastoDTO
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
) {
    @PostMapping
    fun crearGasto(@RequestBody gastoDTO: CreacionGastoDTO): String {
        val gasto = gastoService.crearGasto(gastoDTO)

        return "Gasto de $${gasto.monto} creado exitosamente en ${gasto.grupo.nombre}."
    }
}