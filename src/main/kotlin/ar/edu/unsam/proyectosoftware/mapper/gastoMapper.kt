package ar.edu.unsam.proyectosoftware.mapper

import ar.edu.unsam.proyectosoftware.BusinessException
import ar.edu.unsam.proyectosoftware.domain.Gasto
import ar.edu.unsam.proyectosoftware.dto.GastoDetalleDTO
import ar.edu.unsam.proyectosoftware.domain.Grupo
import ar.edu.unsam.proyectosoftware.domain.PartesExactas
import ar.edu.unsam.proyectosoftware.domain.PartesIguales
import ar.edu.unsam.proyectosoftware.domain.Porcentual
import ar.edu.unsam.proyectosoftware.domain.enums.TipoGasto
import ar.edu.unsam.proyectosoftware.domain.Usuario
import ar.edu.unsam.proyectosoftware.dto.CreacionGastoDTO
import ar.edu.unsam.proyectosoftware.dto.GastoItemDTO
import ar.edu.unsam.proyectosoftware.errors.ErrorStrings

object GastoMapper {

    fun fromCreacionDTO(
        dto: CreacionGastoDTO,
        grupo: Grupo,
        pagador: Usuario,
        participantes: List<Usuario>
    ): Gasto {
        // 1. Instanciamos la subclase específica (Pattern Factory)
        val gasto = when (dto.tipoGasto) {
            TipoGasto.PARTES_IGUALES -> PartesIguales(
                descripcion = dto.descripcion,
                categoria = dto.categoria,
                monto = dto.monto,
                fecha = dto.fecha,
                grupo = grupo,
                pagador = pagador,
                participantes = participantes
            )

            TipoGasto.PARTES_EXACTAS -> {
                val asignacionesDTO = dto.asignacionesExactas
                    ?: throw BusinessException(ErrorStrings.FALTAN_ASIGNACIONES)

                // Mapeamos IDs a objetos Usuario reales
                val mapaAsignaciones = asignacionesDTO.mapKeys { (id, _) ->
                    participantes.find { it.id == id }
                        ?: throw BusinessException(ErrorStrings.USUARIO_NO_PERTENECE_A_PARTICIPANTES(id))
                }.toMutableMap()

                PartesExactas(
                    descripcion = dto.descripcion,
                    categoria = dto.categoria,
                    monto = dto.monto,
                    fecha = dto.fecha,
                    grupo = grupo,
                    pagador = pagador,
                    participantes = participantes,
                    asignacionesExactas = mapaAsignaciones
                )
            }

            TipoGasto.PORCENTUAL -> {
                val mapaPorcentajes = participantes.associateWith { usuario ->
                    dto.asignacionesExactas?.get(usuario.id) ?: 0.0
                }.toMutableMap()

                Porcentual(
                    descripcion = dto.descripcion,
                    categoria = dto.categoria,
                    monto = dto.monto,
                    fecha = dto.fecha,
                    grupo = grupo,
                    pagador = pagador,
                    participantes = participantes,
                    porcentajes = mapaPorcentajes
                )
            }
            else -> throw BusinessException(ErrorStrings.TIPO_GASTO_INEXISTENTE)
        }

        // 2. Cargamos los items (común a todos)
        if (dto.items.isEmpty()) {
            gasto.agregarItem("Pago único", 1.0, dto.monto)
        } else {
            dto.items.forEach {
                gasto.agregarItem(it.nombre, it.cantidad, it.precioUnitario)
            }
        }

        // 3. Validamos (polimorfismo: cada clase usa su propia regla)
        gasto.validar()

        return gasto
    }

    fun toItemDTO(gasto: Gasto, userId: Long): GastoItemDTO {
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

    fun toViewDTO(gasto: Gasto, userId: Long): GastoDetalleDTO {
        return GastoDetalleDTO(
            id = gasto.id!!,
            descripcion = gasto.descripcion,
            categoria = gasto.categoria,
            monto = gasto.monto,
            fecha = gasto.fecha,
            pagador = gasto.pagador.nombre,
            participantes = gasto.participantes,
            tipoGasto = gasto.tipoGasto()
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