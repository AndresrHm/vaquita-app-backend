package ar.edu.unsam.proyectosoftware.domain

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.ManyToMany

@Entity
class Usuario(
    var nombre: String = "",
    var email: String = "",
    var contrasena: String = ""
) {
    @Id
    @GeneratedValue
    var id: Long? = null

    @ManyToMany(mappedBy = "integrantes")
    var grupos: MutableList<Grupo> = mutableListOf()

    fun perteneceA(grupo: Grupo): Boolean{
        return true // Mock
    }
}