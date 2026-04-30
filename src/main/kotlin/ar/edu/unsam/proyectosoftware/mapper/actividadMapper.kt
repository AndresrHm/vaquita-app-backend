package ar.edu.unsam.proyectosoftware.mapper

import ar.edu.unsam.proyectosoftware.domain.Actividad
import ar.edu.unsam.proyectosoftware.dto.ActividadDTO

object ActividadMapper {
	fun toDTO(actividad: Actividad): ActividadDTO {
		return ActividadDTO(
			id = actividad.id!!,
			tipo = actividad.tipo,
			fecha = actividad.fecha,
			descripcion = actividad.descripcion,
			metadata = actividad.metadata,
		)
	}
}

