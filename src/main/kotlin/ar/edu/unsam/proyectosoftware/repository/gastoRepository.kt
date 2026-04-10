package ar.edu.unsam.proyectosoftware.repository

import ar.edu.unsam.proyectosoftware.domain.Gasto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface GastoRepository : CrudRepository<Gasto, Long> {
    @EntityGraph(attributePaths = ["pagador", "participantes"])
    @Query(
        """
    SELECT g FROM Gasto g
    WHERE g.grupo.id = :grupoId
"""
    )
    fun findByGrupoId(grupoId: Long, pageable: Pageable): Page<Gasto>
}