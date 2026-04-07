package ar.edu.unsam.proyectosoftware.domain

import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
@Table(name = "grupos")
class Grupo(
    var nombre: String = "Grupo",
    var descripcion: String = "",
    var imagen: String = "",
    val fechaCreacion: LocalDate
) {
    @Id
    @GeneratedValue
    var id: Long? = null

    @ManyToMany(fetch = FetchType.LAZY)
    val integrantes = mutableListOf<Usuario>()

    fun agregarIntegrante(nuevo: Usuario) {
        integrantes.add(nuevo)
    }

    fun registrarGasto(gasto: Gasto) {
        // TODO()
    }
}