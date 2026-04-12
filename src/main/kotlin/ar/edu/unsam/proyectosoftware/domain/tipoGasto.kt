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
                            montoFraccion = calcularMontoPorParticipante(gasto),
                            pagoParcial = 0.0
                        )
                    )
                }
            }
            return fracciones
        }

        override fun calcularMontoPorParticipante(gasto: Gasto): Double {
            return gasto.monto / gasto.cantidadParticipantes()
        }
    };

    abstract fun repartirGasto(gasto: Gasto): List<FraccionGasto>
    abstract fun calcularMontoPorParticipante(gasto: Gasto): Double
}