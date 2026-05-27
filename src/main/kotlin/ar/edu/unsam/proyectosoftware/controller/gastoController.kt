package ar.edu.unsam.proyectosoftware.controller

import ar.edu.unsam.proyectosoftware.BusinessException
import ar.edu.unsam.proyectosoftware.errors.ErrorStrings
import ar.edu.unsam.proyectosoftware.service.GastoService
import ar.edu.unsam.proyectosoftware.dto.CreacionGastoDTO
import ar.edu.unsam.proyectosoftware.dto.OrdenGasto
import ar.edu.unsam.proyectosoftware.dto.TipoRelacionVista
import ar.edu.unsam.proyectosoftware.dto.GastoDetalleDTO
import ar.edu.unsam.proyectosoftware.dto.GastoDetalleModalDTO
import ar.edu.unsam.proyectosoftware.dto.GastoAnalyticsResponse
import ar.edu.unsam.proyectosoftware.dto.PageResponse
import ar.edu.unsam.proyectosoftware.domain.enums.CategoriaGasto
import ar.edu.unsam.proyectosoftware.dto.ActionResponse
import ar.edu.unsam.proyectosoftware.dto.FilterGastosCustom
import ar.edu.unsam.proyectosoftware.dto.PagoDTO
import ar.edu.unsam.proyectosoftware.dto.PagoGlobalDTO
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.time.format.DateTimeParseException

@RestController
@RequestMapping("gasto")
class GastoController(
    val gastoService: GastoService,
) {
    @PostMapping
    fun crearGasto(auth: Authentication, @RequestBody gastoDTO: CreacionGastoDTO): ActionResponse {
        val gasto = gastoService.crearGasto(auth, gastoDTO)

        return ActionResponse(
            id = gasto.id!!,
            message = "Gasto de $${gasto.monto} creado exitosamente en ${gasto.grupo.nombre}."
        )
    }

    @GetMapping("/listaItems")
    fun obtenerListaItemModal(
        auth: Authentication,
        @RequestParam gastoId: Long,
    ): GastoDetalleModalDTO {
        return gastoService.obtenerListaItems(auth, gastoId)
    }

    @GetMapping("/me/deuda")
    fun obtenerDeuda(auth: Authentication): Double {
        return gastoService.obtenerDeuda(auth)
    }

    @PostMapping("/pagar")
    fun pagarGasto(
        auth: Authentication,
        @RequestBody pagoDTO: PagoDTO
    ): ResponseEntity<String> {
        gastoService.realizarPagoPorGasto(auth, pagoDTO)

        return ResponseEntity.ok("Pago registrado correctamente")
    }

    @PostMapping("/pagar/global")
    fun pagarDeudaGlobal(
        auth: Authentication,
        @RequestBody pagoDTO: PagoGlobalDTO
    ): ResponseEntity<String> {
        gastoService.realizarPagoGlobalAUsuario(auth, pagoDTO.acreedorId, pagoDTO.monto)
        return ResponseEntity.ok("Pago global procesado y deudas simplificadas")
    }

    @GetMapping("/filtrar")
    fun obtenerGastosFiltrados(
        auth: Authentication,
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
        @RequestParam(required = false) orden: OrdenGasto?
    ): PageResponse<GastoDetalleDTO> {

        val vistaEnum = vista?.let {
            try {
                TipoRelacionVista.valueOf(it.trim().uppercase())
            } catch (_: IllegalArgumentException) {
                throw BusinessException(ErrorStrings.INFORMACION_INVALIDA)
            }
        }

        val categoriasEnum = categoria?.let { categorias ->
            categorias.mapNotNull { catStr ->
                try {
                    CategoriaGasto.valueOf(catStr.trim().uppercase())
                } catch (_: IllegalArgumentException) {
                    throw BusinessException(ErrorStrings.INFORMACION_INVALIDA)
                }
            }
        }?.takeIf { it.isNotEmpty() }

        val fechaInicioLocal = fechaInicio?.let {
            try {
                java.time.LocalDate.parse(it)
            } catch (_: Exception) {
                null
            }
        }
        val fechaFinLocal = fechaFin?.let {
            try {
                java.time.LocalDate.parse(it)
            } catch (_: Exception) {
                null
            }
        }


        val filters = FilterGastosCustom(
            vista = vistaEnum,
            categoria = categoriasEnum,
            fechaInicio = fechaInicioLocal,
            fechaFin = fechaFinLocal,
            amigoID = amigoID,
            search = search,
            rangoIni = rangoIni,
            rangoFin = rangoFin,
            page = page,
            size = size,
            orden = orden,
        )

        return gastoService.getGastosByFilters(auth, filters)
    }

    @GetMapping("/me/analytics")
    fun obtenerMisGastosAnalytics(
        auth: Authentication,
        @RequestParam desde: String,
        @RequestParam hasta: String,
    ) : GastoAnalyticsResponse {
        val desdeDate = try {
            LocalDate.parse(desde)
        } catch (_: DateTimeParseException) {
            throw BusinessException(ErrorStrings.FECHA_INVALIDA)
        }

        val hastaDate = try {
            LocalDate.parse(hasta)
        } catch (_: DateTimeParseException) {
            throw BusinessException(ErrorStrings.FECHA_INVALIDA)
        }

        if (desdeDate.isAfter(hastaDate)) {
            throw BusinessException(ErrorStrings.RANGO_FECHAS_INVALIDO)
        }

        return gastoService.obtenerAnalyticsMisGastos(auth, desdeDate, hastaDate)
    }
}