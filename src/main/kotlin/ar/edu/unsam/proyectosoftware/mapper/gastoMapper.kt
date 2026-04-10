package ar.edu.unsam.proyectosoftware.mapper

import ar.edu.unsam.proyectosoftware.domain.Gasto
import ar.edu.unsam.proyectosoftware.dto.GastoItemDTO

object GastoMapper {

    fun toGastoItemDTO(gasto: Gasto, userId: Long): GastoItemDTO {
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
}