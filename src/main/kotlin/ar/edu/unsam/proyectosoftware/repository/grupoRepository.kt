package ar.edu.unsam.proyectosoftware.repository

import ar.edu.unsam.proyectosoftware.domain.Grupo
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
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

    @Query("""
    SELECT COUNT(u) FROM Grupo g
    JOIN g.integrantes u
    WHERE g.id = :grupoId
""")
    fun countIntegrantes(grupoId: Long): Long

    fun findByIntegrantes_Id(usuarioId: Long): List<Grupo>

    @Query("""
        SELECT COUNT(g) > 0
        FROM Grupo g
        JOIN g.integrantes i
        WHERE g.id = :grupoId AND i.id = :usuarioId
    """)
    fun esMiembro(grupoId: Long, usuarioId: Long): Boolean

    @Query("""
        SELECT COUNT(g) > 0
        FROM Grupo g
        JOIN g.admins a
        WHERE g.id = :grupoId AND a.id = :usuarioId
    """)
    fun esAdmin(grupoId: Long, usuarioId: Long): Boolean

    @Query("""
    SELECT new ar.edu.unsam.proyectosoftware.repository.MiembroProjection(
        u.id,
        u.nombre,
        u.imagenUrl,
        CASE WHEN a.id IS NOT NULL THEN true ELSE false END
    )
    FROM Grupo g
    JOIN g.integrantes u
    LEFT JOIN g.admins a ON a.id = u.id
    WHERE g.id = :grupoId
""")
    fun obtenerMiembrosConRol(@Param("grupoId") grupoId: Long): List<MiembroProjection>
}

data class MiembroProjection(
    val id: Long,
    val nombre: String,
    val imagenUrl: String,
    val esAdmin: Boolean
)