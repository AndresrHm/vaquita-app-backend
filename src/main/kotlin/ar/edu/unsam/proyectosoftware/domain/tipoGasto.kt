package ar.edu.unsam.proyectosoftware.domain

enum class TipoGasto {
    PARTES_IGUALES {
        override fun repartirGasto(gasto: Gasto): List<FraccionGasto> {
            val fracciones = mutableListOf<FraccionGasto>()
            gasto.participantes.forEach {
                if (it.id != gasto.pagador.id) {
                    fracciones.add(
                        FraccionGasto(
                            deudor = it,
                            acreedor = gasto.pagador,
                            gastoAsociado = gasto,
                            montoFraccion = calcularMontoPorParticipante(gasto),
                            pagoParcial = 0.0
                        )
                    )
                }
            }
            return fracciones
        }

        fun calcularMontoPorParticipante(gasto: Gasto): Double {
            return gasto.monto / gasto.cantidadParticipantes()
        }
    },
    PARTES_EXACTAS {
        override fun repartirGasto(gasto: Gasto): List<FraccionGasto> {
            val fracciones = gasto.asignacionesExactas
                .filter { (usuario, _) -> usuario.id != gasto.pagador.id }
                .map { (usuario, montoEspecifico) ->
                    FraccionGasto(
                        deudor = usuario,
                        acreedor = gasto.pagador,
                        gastoAsociado = gasto,
                        montoFraccion = montoEspecifico,
                        pagoParcial = 0.0
                    )
                }
            return fracciones
        }
    };

    abstract fun repartirGasto(gasto: Gasto): List<FraccionGasto>
}