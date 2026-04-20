package ar.edu.unsam.proyectosoftware.repository

import ar.edu.unsam.proyectosoftware.domain.Grupo
import ar.edu.unsam.proyectosoftware.domain.IntegranteGrupo
import ar.edu.unsam.proyectosoftware.domain.RolIntegrante
import ar.edu.unsam.proyectosoftware.domain.Usuario
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface IntegranteGrupoRepository : CrudRepository<IntegranteGrupo, Long> {
    @Query(
        """
    SELECT COUNT(ig) > 0
    FROM IntegranteGrupo ig
    WHERE ig.grupo.id = :grupoId
      AND ig.usuario.id = :usuarioId
"""
    )
    fun esMiembro(grupoId: Long, usuarioId: Long): Boolean

    @Query(
        """
        SELECT ig.usuario FROM IntegranteGrupo ig WHERE ig.grupo.id = :grupoId
"""
    )
    fun findAllByGrupoId(grupoId: Long): List<Usuario>

    @Query(
        """
    SELECT ig.usuario 
    FROM IntegranteGrupo ig 
    WHERE ig.grupo.id = :grupoId 
    AND ig.usuario.id IN :usuarioIds
"""
    )
    fun findUsuariosByGrupoAndIds(
        @Param("grupoId") grupoId: Long,
        @Param("usuarioIds") usuarioIds: List<Long>
    ): List<Usuario>

    @Query(
        """
    SELECT COUNT(ig) > 0
    FROM IntegranteGrupo ig
    WHERE ig.grupo.id = :grupoId
      AND ig.usuario.id = :usuarioId
      AND ig.rol = ar.edu.unsam.proyectosoftware.domain.RolIntegrante.ADMIN
"""
    )
    fun esAdmin(grupoId: Long, usuarioId: Long): Boolean

    fun existsByGrupoIdAndUsuarioIdAndRol(
        grupoId: Long,
        usuarioId: Long,
        rol: RolIntegrante
    ): Boolean

    fun existsByGrupoIdAndUsuarioId(grupoId: Long, usuarioId: Long): Boolean

    @Query(
        """
    SELECT new ar.edu.unsam.proyectosoftware.repository.MiembroProjection(
        u.id,
        u.nombre,
        u.imagenUrl,
        (ig.rol = ar.edu.unsam.proyectosoftware.domain.RolIntegrante.ADMIN)
    )
    FROM IntegranteGrupo ig
    JOIN ig.usuario u
    WHERE ig.grupo.id = :grupoId
"""
    )
    fun obtenerMiembrosConRol(@Param("grupoId") grupoId: Long): List<MiembroProjection>

    @Query(
        """
    SELECT g FROM Grupo g
    JOIN g.integrantes ig
    WHERE ig.usuario.id = :usuarioId
      AND g.activado = true
"""
    )
    fun findByIntegrantesIdAndActivadoTrue(usuarioId: Long): List<Grupo>

    @Query(
        """
    SELECT COUNT(ig)
    FROM IntegranteGrupo ig
    WHERE ig.grupo.id = :grupoId
"""
    )
    fun countByGrupoId(grupoId: Long): Long

    fun deleteByGrupoIdAndUsuarioId(grupoId: Long, usuarioId: Long)

    @Query(
        """
    SELECT ig.usuario.id
    FROM IntegranteGrupo ig
    WHERE ig.grupo.id = :grupoId
      AND ig.usuario.id IN :usuarioIds
"""
    )
    fun findIdsQuePertenecen(
        @Param("grupoId") grupoId: Long,
        @Param("usuarioIds") usuarioIds: List<Long>
    ): List<Long>

    @Query(
        """
    SELECT g
    FROM Grupo g
    JOIN g.integrantes ig
    WHERE ig.usuario.id = :usuarioId
"""
    )
    fun findGruposByUsuario(
        @Param("usuarioId") usuarioId: Long,
        pageable: Pageable
    ): Page<Grupo>
}

data class MiembroProjection(
    val id: Long,
    val nombre: String,
    val imagenUrl: String,
    val esAdmin: Boolean
)