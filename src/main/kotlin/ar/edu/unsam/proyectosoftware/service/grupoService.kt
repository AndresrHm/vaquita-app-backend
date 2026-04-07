package ar.edu.unsam.proyectosoftware.service

import ar.edu.unsam.proyectosoftware.domain.Grupo
import org.springframework.stereotype.Service

@Service
class GrupoService(
) {
    fun obtenerGrupo(grupoID: Long): Grupo {
        return Grupo() //Mock
    }
}