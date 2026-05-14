package ar.edu.unsam.proyectosoftware.repository

import ar.edu.unsam.proyectosoftware.domain.Gasto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

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

    @EntityGraph(attributePaths = ["items", "grupo", "pagador"])
    @Query(
        """
        SELECT g
        FROM Gasto g
        WHERE g.id = :gastoId
        """
    )
    fun findDetalleById(@Param("gastoId") gastoId: Long): Gasto?

    /**
     * Devuelve true si el usuario tiene al menos una fracción con saldo pendiente (> 0)
     * dentro de cualquier gasto del grupo indicado.
     */
    @Query(
        """
        SELECT CASE WHEN COUNT(fg) > 0 THEN true ELSE false END
        FROM FraccionGasto fg
        WHERE fg.gastoAsociado.grupo.id = :grupoId
          AND fg.deudor.id = :usuarioId
          AND (fg.montoFraccion - fg.pagoParcial) > 0
        """
    )
    fun existeDeudaPendienteEnGrupo(grupoId: Long, usuarioId: Long): Boolean

    /**
     * Devuelve true si otros usuarios le deben dinero al usuario en el grupo indicado.
     */
    @Query(
        """
        SELECT CASE WHEN COUNT(fg) > 0 THEN true ELSE false END
        FROM FraccionGasto fg
        WHERE fg.gastoAsociado.grupo.id = :grupoId
          AND fg.deudor.id != :usuarioId
          AND (fg.montoFraccion - fg.pagoParcial) > 0
          AND fg.gastoAsociado.pagador.id = :usuarioId
        """
    )
    fun existeDeudaPendienteHaciaUsuario(grupoId: Long, usuarioId: Long): Boolean

    @EntityGraph(attributePaths = ["pagador", "participantes"])
    @Query(
        value = """
            SELECT DISTINCT g
            FROM Gasto g
            LEFT JOIN g.participantes p
            WHERE (:searchPattern = '' OR LOWER(g.descripcion) LIKE :searchPattern)
              AND (CAST(:fechaInicio AS date) IS NULL OR g.fecha >= :fechaInicio)
              AND (CAST(:fechaFin AS date) IS NULL OR g.fecha <= :fechaFin)
              AND (CAST(:rangoIni AS double) IS NULL OR g.monto >= :rangoIni)
              AND (CAST(:rangoFin AS double) IS NULL OR g.monto <= :rangoFin)
              AND (:filtrarCategorias = false OR g.categoria IN :categorias)
              AND p.id = :userId
              AND (CAST(:amigoId AS long) IS NULL OR g.pagador.id = :amigoId)
              AND (
                    (:vistaTodos = true)
                    OR (
                        :vistaDeuda = true
                        AND EXISTS (
                            SELECT 1
                            FROM FraccionGasto fg
                            WHERE fg.gastoAsociado = g
                              AND fg.deudor.id = :userId
                              AND (fg.montoFraccion - fg.pagoParcial) > 0
                        )
                    )
                    OR (:vistaAmigos = true AND g.pagador.id = :userId)
                  )
        """
    )
    fun buscarPorFiltros(
        @Param("searchPattern") searchPattern: String,
        @Param("fechaInicio") fechaInicio: LocalDate?,
        @Param("fechaFin") fechaFin: LocalDate?,
        @Param("rangoIni") rangoIni: Double?,
        @Param("rangoFin") rangoFin: Double?,
        @Param("filtrarCategorias") filtrarCategorias: Boolean,
        @Param("categorias") categorias: List<String>,
        @Param("userId") userId: Long,
        @Param("amigoId") amigoId: Long?,
        @Param("vistaTodos") vistaTodos: Boolean,
        @Param("vistaDeuda") vistaDeuda: Boolean,
        @Param("vistaAmigos") vistaAmigos: Boolean,
        pageable: Pageable,
    ): Page<Gasto>
}