package ar.edu.unsam.proyectosoftware.controller

import ar.edu.unsam.proyectosoftware.service.GastoService
import ar.edu.unsam.proyectosoftware.dto.CreacionGastoDTO
import ar.edu.unsam.proyectosoftware.dto.GastoDetalleDTO
import ar.edu.unsam.proyectosoftware.dto.PageResponse
import ar.edu.unsam.proyectosoftware.domain.enums.CategoriaGasto
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
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

    @GetMapping("/{userId}")
    fun obtenerDeuda(@PathVariable userId: Long): Double {
        return gastoService.obtenerDeuda(userId)
    }

    @GetMapping("/filtrar")
    fun obtenerGastosFiltrados(
        @RequestParam userId: Long,
        @RequestParam(required = false) vista: String?,
        @RequestParam(required = false) categoria: List<String>?,
        @RequestParam(required = false) fechaInicio: String?,
        @RequestParam(required = false) fechaFin: String?,
        @RequestParam(required = false) amigoID: Long?,
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) rangoIni: Double?,
        @RequestParam(required = false) rangoFin: Double?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
    ): PageResponse<GastoDetalleDTO> {
        // Convertir string de vista a enum
        val vistaEnum = vista?.let {
            try {
                ar.edu.unsam.proyectosoftware.dto.TipoRelacionVista.valueOf(it.uppercase())
            } catch (e: IllegalArgumentException) {
                null
            }
        }

        // Convertir lista de categorías string a enum
        val categoriasEnum = categoria?.mapNotNull { catStr ->
            try {
                CategoriaGasto.valueOf(catStr.uppercase())
            } catch (e: IllegalArgumentException) {
                null
            }
        }?.takeIf { it.isNotEmpty() }

        // Convertir fechas string a LocalDate
        val fechaInicioLocal = fechaInicio?.let {
            try {
                java.time.LocalDate.parse(it)
            } catch (e: Exception) {
                null
            }
        }
        val fechaFinLocal = fechaFin?.let {
            try {
                java.time.LocalDate.parse(it)
            } catch (e: Exception) {
                null
            }
        }

        // Construir FilterGastosCustom con valores convertidos
        val filters = ar.edu.unsam.proyectosoftware.dto.FilterGastosCustom(
            vista = vistaEnum,
            categoria = categoriasEnum,
            fechaInicio = fechaInicioLocal,
            fechaFin = fechaFinLocal,
            amigoID = amigoID,
            search = search,
            rangoIni = rangoIni,
            rangoFin = rangoFin,
            page = page,
            size = size
        )

        return gastoService.getGastosByFilters(userId, filters)
    }
}