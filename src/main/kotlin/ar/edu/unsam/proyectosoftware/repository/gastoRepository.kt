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
                    (:vistaPropia = false AND :vistaGrupo = false)
                    OR (:vistaPropia = true AND g.pagador.id = :userId)
                    OR (:vistaGrupo = true AND g.pagador.id <> :userId)
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
        @Param("vistaPropia") vistaPropia: Boolean,
        @Param("vistaGrupo") vistaGrupo: Boolean,
        pageable: Pageable,
    ): Page<Gasto>
}