package ar.edu.unsam.proyectosoftware.domain

import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import kotlin.math.roundToInt

@Entity
class ItemGasto(
    var nombre: String,
    var cantidad: Double,
    var precioUnitario: Double,
    @ManyToOne
    @JoinColumn(name = "gasto_id")
    val gasto: Gasto
) {
    @Id
    @GeneratedValue
    var id: Long? = null

    fun total(): Double {

        val precioEnCentavos =
            (precioUnitario * 100).roundToInt()

        return (precioEnCentavos * cantidad) / 100.0
    }
}