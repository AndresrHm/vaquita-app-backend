package ar.edu.unsam.proyectosoftware.repository

import ar.edu.unsam.proyectosoftware.domain.SolicitudAmistad
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface SolicitudAmistadRepository : CrudRepository<SolicitudAmistad, Long> {
    fun findByIdReceptor(idReceptor: Long): List<SolicitudAmistad>
    fun findByIdEmisor(idEmisor: Long): List<SolicitudAmistad>
    fun existsByIdEmisorAndIdReceptor(idEmisor: Long, idReceptor: Long): Boolean
    fun deleteByIdEmisorAndIdReceptor(idEmisor: Long, idReceptor: Long)
}