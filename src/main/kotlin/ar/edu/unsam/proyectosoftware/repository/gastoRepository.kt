package ar.edu.unsam.proyectosoftware.repository

import ar.edu.unsam.proyectosoftware.domain.Gasto
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface GastoRepository : CrudRepository<Gasto, Long> {
    @Query(
        """
        SELECT g FROM Gasto g
        JOIN FETCH g.pagador
        JOIN FETCH g.participantes
        WHERE g.grupo.id = :grupoId
    """
    )
    fun findByGrupoId(grupoId: Long): List<Gasto>

}