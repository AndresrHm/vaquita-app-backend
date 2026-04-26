package ar.edu.unsam.proyectosoftware.repository

import ar.edu.unsam.proyectosoftware.domain.FraccionGasto
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
    fun findByAcreedorIdAndGastoAsociadoGrupoId(acreedorId: Long, grupoId: Long): List<SaldoPendiente>
    fun findByDeudorIdAndGastoAsociadoGrupoId(deudorId: Long, grupoId: Long): List<SaldoPendiente>
    fun findByDeudorId(id: Long): List<SaldoPendiente>
}