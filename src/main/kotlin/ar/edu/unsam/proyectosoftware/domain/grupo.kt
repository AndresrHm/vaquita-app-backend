package ar.edu.unsam.proyectosoftware.domain

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id

@Entity
class Grupo(
    var nombre: String,
) {
    @Id
    @GeneratedValue
    var id: Long? = null

    fun registrarGasto(gasto: Gasto) {
        // TODO()
    }
}