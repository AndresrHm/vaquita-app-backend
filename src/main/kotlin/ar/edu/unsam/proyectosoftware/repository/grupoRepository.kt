package ar.edu.unsam.proyectosoftware.repository

import ar.edu.unsam.proyectosoftware.domain.Grupo
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface GrupoRepository : CrudRepository<Grupo, Long> {}