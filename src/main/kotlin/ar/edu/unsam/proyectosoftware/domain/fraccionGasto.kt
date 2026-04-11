package ar.edu.unsam.proyectosoftware.domain

import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Entity
class FraccionGasto(
    @ManyToOne(cascade = [(CascadeType.MERGE)])
    @JoinColumn(name = "deudor_id")
    val deudor: Usuario,

    @ManyToOne(cascade = [(CascadeType.MERGE)])
    @JoinColumn(name = "acreedor_id")
    val acreedor: Usuario,

    val montoFraccion: Double,
    var pagoParcial: Double,
) {
    @Id
    @GeneratedValue
    var id: Long? = null

    fun saldoPendiente() = montoFraccion - pagoParcial

    fun estaSaldado() = saldoPendiente() <= 0.0

    fun pagar(pago: Double) {
        pagoParcial += pago
    }
}
