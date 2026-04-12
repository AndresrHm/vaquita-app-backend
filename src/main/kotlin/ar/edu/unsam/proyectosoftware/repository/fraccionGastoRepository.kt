package ar.edu.unsam.proyectosoftware.repository

import ar.edu.unsam.proyectosoftware.domain.FraccionGasto
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

interface SaldoPendiente {
    val montoFraccion: Double
    val pagoParcial: Double

    fun saldoPendiente() = montoFraccion - pagoParcial
}

@Repository
interface FraccionGastoRepository : CrudRepository<FraccionGasto, Long> {
    fun findByDeudorId(id: Long): List<SaldoPendiente>
}