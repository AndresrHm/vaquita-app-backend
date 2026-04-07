package ar.edu.unsam.proyectosoftware.domain

import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.ManyToMany

@Entity
class Grupo(
    var nombre: String = "Grupo",
    var descripcion: String = "Descripción del grupo",
    var imagen: String = "Imagen del grupo"
) {
    @Id
    @GeneratedValue
    var id: Long? = null

    @ManyToMany(fetch = FetchType.LAZY)
    val integrantes = mutableListOf<Usuario>()

    fun registrarGasto(gasto: Gasto) {
        // TODO()
    }
}