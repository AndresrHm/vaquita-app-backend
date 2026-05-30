package ar.edu.unsam.proyectosoftware.controller

import ar.edu.unsam.proyectosoftware.BusinessException
import ar.edu.unsam.proyectosoftware.dto.ActividadDTO
import ar.edu.unsam.proyectosoftware.dto.FilterActividadDTO
import ar.edu.unsam.proyectosoftware.dto.PageResponse
import ar.edu.unsam.proyectosoftware.dto.TipoActividad
import ar.edu.unsam.proyectosoftware.errors.ErrorStrings
import ar.edu.unsam.proyectosoftware.service.ActividadService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.time.LocalDate
import java.time.format.DateTimeParseException
import ar.edu.unsam.proyectosoftware.dto.ActividadAnalyticsResponse

@RestController
@RequestMapping("/actividad")
@CrossOrigin(origins = ["http://localhost:5173"])
class ActividadController(
	private val actividadService: ActividadService,
) {
	@GetMapping("/me")
	fun obtenerMisActividades(
		auth: Authentication?,
		@RequestParam(required = false) tipo: TipoActividad?,
		@RequestParam(required = false)
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
		desde: LocalDateTime?,
		@RequestParam(required = false)
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
		hasta: LocalDateTime?,
		@RequestParam(defaultValue = "0") page: Int,
		@RequestParam(defaultValue = "6") size: Int,
		@RequestParam(required = false, name = "search") search: String?,
		@RequestParam(required = false, name = "searchText") searchText: String?,
	): PageResponse<ActividadDTO> {
		// Soportamos ambos nombres de parámetro: `search` y `searchText` (legacy del frontend)
		val finalSearch = (search ?: searchText)?.trim()
		val filters = FilterActividadDTO(
			tipo = tipo,
			desde = desde,
			hasta = hasta,
			page = page,
			size = size,
			search = finalSearch,
		)

		return actividadService.obtenerActividadesDelUsuarioLogueado(auth, filters)
	}

	@GetMapping("/grupo/{grupoId}")
	fun obtenerActividadesDeGastoPorGrupo(
		@PathVariable grupoId: Long,
		@RequestParam(defaultValue = "0") page: Int,
		@RequestParam(defaultValue = "10") size: Int,
	): PageResponse<ActividadDTO> {
		return actividadService.obtenerActividadesDeGastoPorGrupo(grupoId, page, size)
	}

	@GetMapping("/analytics")
	fun obtenerActividadAnalytics(
		@RequestParam desde: String,
		@RequestParam hasta: String,
	): ActividadAnalyticsResponse {
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

		return actividadService.obtenerAnalytics(desdeDate, hastaDate)
	}
}