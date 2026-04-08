package ar.edu.unsam.proyectosoftware.repository

import ar.edu.unsam.proyectosoftware.domain.Grupo
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

// En caso de tener problemas con la paginación probar con jpa repository
@Repository
interface GrupoRepository : CrudRepository<Grupo, Long> {
    @Query(
        """
    SELECT g FROM Grupo g
    JOIN g.integrantes i
    WHERE i.id = :usuarioId
"""
    )
    fun findByUsuario(
        usuarioId: Long,
        pageable: Pageable
    ): Page<Grupo>
}