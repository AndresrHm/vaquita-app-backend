package ar.edu.unsam.proyectosoftware.repository

import ar.edu.unsam.proyectosoftware.domain.FraccionGasto
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface FraccionGastoRepository : CrudRepository<FraccionGasto, Long> {

}