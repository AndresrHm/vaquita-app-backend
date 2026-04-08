package ar.edu.unsam.proyectosoftware.domain

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id

@Entity
class Usuario(
    var nombre: String = "",
    var email: String = "",
    var contrasena: String = ""
) {
    @Id
    @GeneratedValue
    var id: Long? = null

    fun perteneceA(grupo: Grupo): Boolean{
        return true // Mock
    }
}