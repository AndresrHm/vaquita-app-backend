package ar.edu.unsam.proyectosoftware.service

import ar.edu.unsam.proyectosoftware.BusinessException
import ar.edu.unsam.proyectosoftware.domain.Gasto
import ar.edu.unsam.proyectosoftware.domain.Grupo
import ar.edu.unsam.proyectosoftware.domain.Usuario
import ar.edu.unsam.proyectosoftware.dto.GastoItemDTO
import ar.edu.unsam.proyectosoftware.dto.GastosPorAnioDTO
import ar.edu.unsam.proyectosoftware.dto.GastosPorMesDTO
import ar.edu.unsam.proyectosoftware.dto.GrupoCreacionDTO
import ar.edu.unsam.proyectosoftware.dto.GrupoDetalleDTO
import ar.edu.unsam.proyectosoftware.errors.ErrorStrings
import ar.edu.unsam.proyectosoftware.repository.GastoRepository
import ar.edu.unsam.proyectosoftware.repository.GrupoRepository
import ar.edu.unsam.proyectosoftware.repository.UsuarioRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.forEach

@Service
class GrupoService {
    @Autowired
    lateinit var grupoRepository: GrupoRepository

    @Autowired
    lateinit var usuarioRepository: UsuarioRepository

    @Autowired
    lateinit var gastoRepository: GastoRepository

    @Transactional(readOnly = true)
    fun obtenerGrupos(
        usuarioId: Long,
        page: Int,
        size: Int,
    ): Page<Grupo> {

        if (usuarioRepository.findByIdOrNull(usuarioId) == null) throw BusinessException(ErrorStrings.USUARIO_NO_ENCONTRADO)

        val pageable = PageRequest.of(
            page,
            size,
            Sort.by(Sort.Direction.DESC, "fechaCreacion")
        )

        return grupoRepository.findByUsuario(usuarioId, pageable)
    }

    @Transactional
    fun crearGrupo(
        usuarioId: Long,
        dto: GrupoCreacionDTO
    ): Grupo {
        val usuario = buscarUsuario(usuarioId)

        val grupo = Grupo(
            nombre = dto.nombre,
            descripcion = dto.descripcion,
            imagen = dto.imagen,
            fechaCreacion = LocalDate.now()
        )

        grupo.agregarIntegrante(usuario)

        return grupoRepository.save(grupo)
    }

    fun obtenerDetalleGrupo(grupoId: Long, userId: Long): GrupoDetalleDTO {
        val grupo = grupoRepository.findById(grupoId)
            .orElseThrow { RuntimeException("Grupo no encontrado") }

        val gastos = gastoRepository.findByGrupoId(grupoId)

        val gastosPorAnio = gastos
            .sortedByDescending { it.fecha }
            .groupBy { it.fecha.year }
            .map { (anio, gastosDelAnio) ->
                GastosPorAnioDTO(
                    year = anio,
                    months = gastosDelAnio
                        .groupBy { it.fecha.month }
                        .map { (mes, gastosDelMes) ->
                            GastosPorMesDTO(
                                month = mes.name, // después lo podemos traducir
                                expenses = gastosDelMes.map {
                                    toGastoItemDTO(it, userId)
                                }
                            )
                        }
                )
            }

        return GrupoDetalleDTO(
            id = grupo.id!!,
            name = grupo.nombre,
            status = "Pendiente de pago", // hardcodeado por ahora
            outstanding = calcularBalance(gastos, userId),
            currency = "$",
            imageUrl = grupo.imagen,
            expensesByYear = gastosPorAnio
        )
    }

    private fun toGastoItemDTO(gasto: Gasto, userId: Long): GastoItemDTO {
        return GastoItemDTO(
            id = gasto.id!!,
            day = gasto.fecha.dayOfMonth,
            title = gasto.descripcion,
            amount = gasto.monto,
            currency = "$",
            paymentType = calcularPaymentType(gasto, userId),
            imageUrl = "https://cdn-icons-png.flaticon.com/512/891/891462.png"
        )
    }

    private fun calcularPaymentType(gasto: Gasto, userId: Long): String {
        val esPagador = gasto.pagador.id == userId
        val participa = gasto.participantes.any { it.id == userId }

        return when {
            esPagador && gasto.participantes.size > 1 -> "split_equal"
            esPagador -> "paid_by_you"
            participa -> "you_owe"
            else -> "paid_by_other"
        }
    }

    private fun calcularBalance(gastos: List<Gasto>, userId: Long): Double {
        var balance = 0.0

        gastos.forEach { gasto ->
            val esPagador = gasto.pagador.id == userId
            val participa = gasto.participantes.any { it.id == userId }

            val parte = gasto.monto / gasto.participantes.size

            when {
                esPagador -> balance += (gasto.monto - parte)
                participa -> balance -= parte
            }
        }

        return balance
    }


    fun obtenerGruposDeUsuario(usuarioId: Long): List<Grupo> {
        buscarUsuario(usuarioId)

        return grupoRepository.findByIntegrantes_Id(usuarioId)
    }

    private fun buscarUsuario(usuarioId: Long): Usuario {
        return usuarioRepository.findByIdOrNull(usuarioId)
            ?: throw BusinessException(ErrorStrings.USUARIO_NO_ENCONTRADO)
    }
}