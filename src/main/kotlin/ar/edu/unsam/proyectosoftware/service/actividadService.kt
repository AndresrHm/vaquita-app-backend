package ar.edu.unsam.proyectosoftware.service

import ar.edu.unsam.proyectosoftware.BusinessException
import ar.edu.unsam.proyectosoftware.AnalyticsProcessingException
import ar.edu.unsam.proyectosoftware.domain.Actividad
import ar.edu.unsam.proyectosoftware.domain.Grupo
import ar.edu.unsam.proyectosoftware.domain.Usuario
import ar.edu.unsam.proyectosoftware.dto.ActividadDTO
import ar.edu.unsam.proyectosoftware.dto.FilterActividadDTO
import ar.edu.unsam.proyectosoftware.dto.PageResponse
import ar.edu.unsam.proyectosoftware.dto.TipoActividad
import ar.edu.unsam.proyectosoftware.dto.toPageResponse
import ar.edu.unsam.proyectosoftware.errors.ErrorStrings
import ar.edu.unsam.proyectosoftware.mapper.ActividadMapper
import ar.edu.unsam.proyectosoftware.repository.ActividadRepository
import ar.edu.unsam.proyectosoftware.repository.ActividadSpecification
import ar.edu.unsam.proyectosoftware.repository.UsuarioRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ar.edu.unsam.proyectosoftware.dto.ActividadAnalyticsResponse
import ar.edu.unsam.proyectosoftware.dto.ActividadPorTipoDTO
import ar.edu.unsam.proyectosoftware.dto.ActividadPorDiaDTO
import ar.edu.unsam.proyectosoftware.dto.ActividadPorGrupoDTO
import ar.edu.unsam.proyectosoftware.dto.RangoDTO
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import com.fasterxml.jackson.databind.JsonNode

private const val MAX_PAGE_SIZE_ACTIVIDADES = 50

@Service
@Transactional
class ActividadService(
	private val actividadRepository: ActividadRepository,
	private val usuarioRepository: UsuarioRepository,
	private val objectMapper: ObjectMapper,
) {
	@Transactional(readOnly = true)

	fun obtenerActividadesDelUsuarioLogueado(
		auth: Authentication?,
		filters: FilterActividadDTO,
	): PageResponse<ActividadDTO> {
		if (filters.page < 0 || filters.size <= 0 || filters.size > MAX_PAGE_SIZE_ACTIVIDADES) {
			throw BusinessException(ErrorStrings.INFORMACION_INVALIDA)
		}

		if (filters.desde != null && filters.hasta != null && filters.desde > filters.hasta) {
			throw BusinessException(ErrorStrings.INFORMACION_INVALIDA)
		}

		if (auth == null) {
			return PageResponse(emptyList(), filters.page, filters.size, 0L, 0, false)
		}

		val usuarioLogueado = obtenerUsuarioLogueado(auth)
		val pageable = PageRequest.of(filters.page, filters.size, Sort.by(Sort.Direction.DESC, "fecha"))

		val spec = ActividadSpecification.conUsuarioId(usuarioLogueado.id!!)
			.and(ActividadSpecification.conTipo(filters.tipo))
			.and(ActividadSpecification.desdeFecha(filters.desde))
			.and(ActividadSpecification.hastaFecha(filters.hasta))
			.and(ActividadSpecification.conSearch(filters.search))

		val pagina = actividadRepository.findAll(spec, pageable)

		return pagina.toPageResponse(ActividadMapper::toDTO)
	}

	fun registrarActividad(
		usuario: Usuario,
		tipo: TipoActividad,
		descripcion: String? = null,
		metadata: Map<String, Any?> = emptyMap(),
	): Actividad {
		val metadataJson = if (metadata.isEmpty()) {
			null
		} else {
			runCatching { objectMapper.writeValueAsString(metadata) }
				.getOrElse { throw BusinessException(ErrorStrings.INFORMACION_INVALIDA) }
		}

		return actividadRepository.save(
			Actividad(
				usuario = usuario,
				tipo = tipo,
				descripcion = descripcion,
				metadata = metadataJson,
			)
		)
	}

	fun registrarIngresoGrupo(
		usuario: Usuario,
		grupo: Grupo,
		actor: Usuario = usuario,
	): Actividad {
		val metadata = linkedMapOf<String, Any?>(
			"grupoId" to grupo.id,
			"grupoNombre" to grupo.nombre,
			"usuarioId" to usuario.id,
			"usuarioNombre" to usuario.nombre,
			"actorId" to actor.id,
			"actorNombre" to actor.nombre,
		)

		return registrarActividad(
			usuario = usuario,
			tipo = TipoActividad.INGRESO_GRUPO,
			descripcion = "Te uniste al grupo ${grupo.nombre}.",
			metadata = metadata,
		)
	}

	fun registrarPago(
		deudor: Usuario,
		acreedor: Usuario,
		monto: Double,
		grupoId: Long? = null,
		grupoNombre: String? = null,
	): Actividad {
		val metadata = linkedMapOf<String, Any?>(
			"acreedorId" to acreedor.id,
			"acreedorNombre" to acreedor.nombre,
			"monto" to monto,
		)

		if (grupoId != null) {
			metadata["grupoId"] = grupoId
		}
		if (grupoNombre != null) {
			metadata["grupoNombre"] = grupoNombre
		}

		return registrarActividad(
			usuario = deudor,
			tipo = TipoActividad.PAGO_REALIZADO,
			descripcion = "Pagaste ${String.format("%.2f", monto)} a ${acreedor.nombre}." + (grupoNombre?.let { " en $it" } ?: ""),
			metadata = metadata,
		)
	}

	fun registrarAmistadAceptada(
		usuarioQuienAcepta: Usuario,
		usuarioEmisor: Usuario,
	): Actividad {
		val metadata = linkedMapOf<String, Any?>(
			"amigoId" to usuarioEmisor.id,
			"amigoNombre" to usuarioEmisor.nombre,
		)

		return registrarActividad(
			usuario = usuarioQuienAcepta,
			tipo = TipoActividad.AMISTAD_ACEPTADA,
			descripcion = "Aceptaste la solicitud de amistad de ${usuarioEmisor.nombre}.",
			metadata = metadata,
		)
	}

	@Transactional(readOnly = true)
	fun obtenerActividadesDeGastoPorGrupo(
		grupoId: Long,
		page: Int = 0,
		size: Int = 10,
	): PageResponse<ActividadDTO> {
		if (page < 0 || size <= 0 || size > MAX_PAGE_SIZE_ACTIVIDADES) {
			throw BusinessException(ErrorStrings.INFORMACION_INVALIDA)
		}

		val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fecha"))
		val spec = ActividadSpecification.conGrupoId(grupoId)
		val pagina = actividadRepository.findAll(spec, pageable)

		return pagina.toPageResponse(ActividadMapper::toDTO)
	}

	private fun obtenerUsuarioLogueado(auth: Authentication): Usuario {
		return usuarioRepository.findByEmail(auth.name)
			.orElseThrow { BusinessException(ErrorStrings.USUARIO_NO_ENCONTRADO) }
	}

	@Transactional(readOnly = true)
	fun obtenerAnalytics(desde: LocalDate, hasta: LocalDate): ActividadAnalyticsResponse {
		return try {
			// Construir rangos de fecha (desde 00:00 hasta 23:59:59)
			val desdeDateTime = desde.atStartOfDay()
			val hastaDateTime = hasta.atTime(23,59,59)

			val spec = ActividadSpecification.desdeFecha(desdeDateTime)
				.and(ActividadSpecification.hastaFecha(hastaDateTime))

			val actividades = actividadRepository.findAll(spec)

			val total = actividades.size.toLong()
			val dias = ChronoUnit.DAYS.between(desde, hasta).let { if (it <= 0) 1 else it + 1 }
			val promedioPorDia = if (dias > 0) total.toDouble() / dias.toDouble() else 0.0

			val porTipo = actividades.groupingBy { it.tipo.name }.eachCount().map { ActividadPorTipoDTO(it.key, it.value.toLong()) }

			val porDia = actividades.groupBy { it.fecha.toLocalDate() }
				.map { ActividadPorDiaDTO(it.key.toString(), it.value.size.toLong()) }
				.sortedBy { it.fecha }

			// porGrupo: extraer grupoId y grupoNombre desde metadata JSON si existe
			val porGrupoMap = mutableMapOf<Long, Pair<String, Long>>() // id -> (nombre, count)
			actividades.forEach { act ->
				val meta = act.metadata
				if (meta != null) {
					runCatching {
						val node: JsonNode = objectMapper.readTree(meta)
						val gid = node.path("grupoId").takeIf { !it.isMissingNode && it.isNumber }?.asLong()
						val gname = node.path("grupoNombre").takeIf { !it.isMissingNode && it.isTextual }?.asText()
						if (gid != null && gname != null) {
							val existing = porGrupoMap[gid]
							if (existing == null) porGrupoMap[gid] = Pair(gname, 1)
							else porGrupoMap[gid] = Pair(existing.first, existing.second + 1)
						}
					}
				}
			}

			val porGrupo = porGrupoMap.map { ActividadPorGrupoDTO(it.key, it.value.first, it.value.second) }

			val actividadesPorDiaRedondeado = Math.round(promedioPorDia * 100) / 100.0

			val resumen = mapOf(
				"totalActividades" to total,
				"actividadesPorDia" to actividadesPorDiaRedondeado
			)

			ActividadAnalyticsResponse(
				rango = RangoDTO(desde, hasta),
				resumen = resumen,
				porTipo = porTipo,
				porDia = porDia,
				porGrupo = porGrupo.sortedByDescending { it.cantidad }
			)
		} catch (ex: BusinessException) {
			throw ex
		} catch (ex: Exception) {
			throw AnalyticsProcessingException(ErrorStrings.ERROR_ANALYTICS_PROCESAMIENTO, ex)
		}
	}
}
