package ar.edu.unsam.proyectosoftware.repository

import ar.edu.unsam.proyectosoftware.domain.Grupo
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface GrupoRepository : CrudRepository<Grupo, Long> {
    @Query(
        """
    SELECT g FROM Grupo g
    JOIN g.integrantes i
    WHERE i.id = :usuarioId
      AND g.activado = true
"""
    )
    fun findByUsuario(
        usuarioId: Long,
        pageable: Pageable
    ): Page<Grupo>

    @Query(
        """
    SELECT g FROM Grupo g
    JOIN g.integrantes i1
    JOIN g.integrantes i2
    WHERE i1.usuario.id = :usuarioId1
      AND i2.usuario.id = :usuarioId2
      AND g.activado = true
"""
    )
    fun findGruposCompartidos(usuarioId1: Long, usuarioId2: Long): List<Grupo>
}