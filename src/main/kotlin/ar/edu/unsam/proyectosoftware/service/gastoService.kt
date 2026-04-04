package ar.edu.unsam.proyectosoftware

import ar.edu.unsam.proyectosoftware.domain.Gasto
import ar.edu.unsam.proyectosoftware.repository.GastoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GastoService(
    @Autowired
    val gastoRepository: GastoRepository,
) {
    @Transactional(readOnly = false, rollbackFor = [Exception::class])
    fun crearGasto(
        gasto: Gasto
    ): Gasto {
        gastoRepository.save(gasto)

        return gasto
    }

}