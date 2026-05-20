package ar.edu.unsam.proyectosoftware.repository

import ar.edu.unsam.proyectosoftware.domain.FraccionGasto
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

interface SaldoPendiente {
    val montoFraccion: Double
    val pagoParcial: Double

    fun saldoPendiente(): Double {
        return Math.round((montoFraccion - pagoParcial) * 100) / 100.0
    }
}

@Repository
interface FraccionGastoRepository : CrudRepository<FraccionGasto, Long> {
    fun findByDeudorIdAndGastoAsociadoId(deudorId: Long, gastoId: Long): FraccionGasto?
    fun findByDeudorId(id: Long): List<SaldoPendiente>
    @Query("""
    SELECT f FROM FraccionGasto f 
    WHERE f.deudor.id = :deudorId 
      AND f.acreedor.id = :acreedorId 
      AND (:grupoId IS NULL OR f.gastoAsociado.grupo.id = :grupoId)
""")
    fun findFraccionesPendientesEntre(
        deudorId: Long,
        acreedorId: Long,
        grupoId: Long?
    ): List<FraccionGasto>
    fun findAllByGastoAsociadoGrupoId(grupoId: Long): List<FraccionGasto>

    @Query("SELECT f FROM FraccionGasto f WHERE f.deudor.id = :deudorId AND f.montoFraccion > f.pagoParcial")
    fun findGastosPendientesByDeudorId(deudorId: Long): List<FraccionGasto>

    @Query("SELECT DISTINCT f FROM FraccionGasto f JOIN FETCH f.gastoAsociado g JOIN FETCH g.participantes WHERE f.deudor.id = :deudorId AND f.acreedor.id = :acreedorId AND f.montoFraccion > f.pagoParcial")
    fun findGastosPendientesBetween(deudorId: Long, acreedorId: Long): List<FraccionGasto>
}