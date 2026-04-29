package ar.edu.unsam.proyectosoftware.repository

import ar.edu.unsam.proyectosoftware.domain.Actividad
import ar.edu.unsam.proyectosoftware.dto.TipoActividad
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.data.jpa.domain.Specification
import java.time.LocalDateTime

class ActividadSpecification {
	companion object {
		fun conUsuarioId(usuarioId: Long): Specification<Actividad> = Specification { root, query, cb ->
			cb.equal(root.get<Long>("usuario").get<Long>("id"), usuarioId)
		}

		fun conTipo(tipo: TipoActividad?): Specification<Actividad> = Specification { root, query, cb ->
			if (tipo == null) {
				cb.conjunction()
			} else {
				cb.equal(root.get<TipoActividad>("tipo"), tipo)
			}
		}

		fun desdeFecha(desde: LocalDateTime?): Specification<Actividad> = Specification { root, query, cb ->
			if (desde == null) {
				cb.conjunction()
			} else {
				cb.greaterThanOrEqualTo(root.get<LocalDateTime>("fecha"), desde)
			}
		}

		fun hastaFecha(hasta: LocalDateTime?): Specification<Actividad> = Specification { root, query, cb ->
			if (hasta == null) {
				cb.conjunction()
			} else {
				cb.lessThanOrEqualTo(root.get<LocalDateTime>("fecha"), hasta)
			}
		}
	}
}

