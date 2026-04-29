package ar.edu.unsam.proyectosoftware.service

import ar.edu.unsam.proyectosoftware.BusinessException
import ar.edu.unsam.proyectosoftware.domain.Actividad
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
		auth: Authentication,
		filters: FilterActividadDTO,
	): PageResponse<ActividadDTO> {
		if (filters.page < 0 || filters.size <= 0 || filters.size > MAX_PAGE_SIZE_ACTIVIDADES) {
			throw BusinessException(ErrorStrings.INFORMACION_INVALIDA)
		}

		if (filters.desde != null && filters.hasta != null && filters.desde > filters.hasta) {
			throw BusinessException(ErrorStrings.INFORMACION_INVALIDA)
		}

		val usuarioLogueado = obtenerUsuarioLogueado(auth)
		val pageable = PageRequest.of(filters.page, filters.size, Sort.by(Sort.Direction.DESC, "fecha"))

		val spec = ActividadSpecification.conUsuarioId(usuarioLogueado.id!!)
			.and(ActividadSpecification.conTipo(filters.tipo))
			.and(ActividadSpecification.desdeFecha(filters.desde))
			.and(ActividadSpecification.hastaFecha(filters.hasta))

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

	private fun obtenerUsuarioLogueado(auth: Authentication): Usuario {
		return usuarioRepository.findByEmail(auth.name)
			.orElseThrow { BusinessException(ErrorStrings.USUARIO_NO_ENCONTRADO) }
	}
}

