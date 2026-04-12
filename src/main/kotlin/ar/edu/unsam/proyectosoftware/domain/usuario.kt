package ar.edu.unsam.proyectosoftware.domain

import ar.edu.unsam.proyectosoftware.dto.UsuarioNombreIdDTO
import com.fasterxml.jackson.annotation.JsonIgnore
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

    @JsonIgnore
    @ManyToMany(mappedBy = "integrantes")
    var grupos: MutableList<Grupo> = mutableListOf()
    @ManyToMany
    var amigos: MutableList<Usuario> = mutableListOf()

    fun perteneceA(grupo: Grupo): Boolean{
        return true // Mock
    }

    companion object {
        fun toUsuarioNombreIdDTO(usuario: Usuario): UsuarioNombreIdDTO {
            return UsuarioNombreIdDTO(
                nombre = usuario.nombre,
                id = usuario.id!!
            )
        }
    }
}