package ar.edu.unsam.proyectosoftware.repository

import ar.edu.unsam.proyectosoftware.domain.Actividad
import ar.edu.unsam.proyectosoftware.dto.TipoActividad
import org.springframework.data.jpa.domain.Specification
import java.time.LocalDateTime

class ActividadSpecification {
	companion object {
		fun conUsuarioId(usuarioId: Long): Specification<Actividad> = Specification { root, _, cb ->
			cb.equal(root.get<Long>("usuario").get<Long>("id"), usuarioId)
		}

		fun conTipo(tipo: TipoActividad?): Specification<Actividad> = Specification { root, _, cb ->
			if (tipo == null) {
				cb.conjunction()
			} else {
				cb.equal(root.get<TipoActividad>("tipo"), tipo)
			}
		}

		fun desdeFecha(desde: LocalDateTime?): Specification<Actividad> = Specification { root, _, cb ->
			if (desde == null) {
				cb.conjunction()
			} else {
				cb.greaterThanOrEqualTo(root.get<LocalDateTime>("fecha"), desde)
			}
		}

		fun hastaFecha(hasta: LocalDateTime?): Specification<Actividad> = Specification { root, _, cb ->
			if (hasta == null) {
				cb.conjunction()
			} else {
				cb.lessThanOrEqualTo(root.get<LocalDateTime>("fecha"), hasta)
			}
		}

		fun conSearch(search: String?): Specification<Actividad> = Specification { root, _, cb ->
			if (search == null || search.isBlank()) {
				cb.conjunction()
			} else {
				val pattern = "%${search.lowercase()}%"
				val descripcionPath = root.get<String>("descripcion")
				val metadataPath = root.get<String>("metadata")
				val tipoAsString = root.get<Any>("tipo").`as`(String::class.java)
				cb.or(
					cb.like(cb.lower(descripcionPath), pattern),
					cb.like(cb.lower(cb.coalesce(metadataPath, cb.literal(""))), pattern),
					cb.like(cb.lower(tipoAsString), pattern),
				)
			}
		}

		fun conGrupoId(grupoId: Long): Specification<Actividad> = Specification { root, _, cb ->
			val metadataPath = root.get<String>("metadata")
			val metadataCoalesced = cb.coalesce(metadataPath, cb.literal(""))

			// Jackson serializa sin espacios por defecto, pero dejamos tolerancia a variantes comunes.
			val patrones = listOf(
				"%\"grupoId\":$grupoId,%",
				"%\"grupoId\": $grupoId,%",
				"%\"grupoId\":$grupoId}%",
				"%\"grupoId\": $grupoId}%",
			)

			cb.or(*patrones.map { cb.like(cb.lower(metadataCoalesced), it.lowercase()) }.toTypedArray())
		}
	}
}

