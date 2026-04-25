package ar.edu.unsam.proyectosoftware.domain

import ar.edu.unsam.proyectosoftware.dto.UsuarioNombreIdDTO
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Lob
import jakarta.persistence.ManyToMany
import jakarta.persistence.OneToMany

@Entity
class Usuario(
    var nombre: String = "",
    var email: String = "",
    var contrasena: String = "",

    @Lob
    @Column(length = 10485760)
    var imagenUrl: String = ""
) {
    @Id
    @GeneratedValue
    var id: Long? = null

    @JsonIgnore
    @OneToMany(mappedBy = "usuario")
    var grupos: MutableList<IntegranteGrupo> = mutableListOf()

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY)
    var amigos: MutableList<Usuario> = mutableListOf()

    fun agregarAmigo(amigo: Usuario) : Boolean {
        if (!amigos.contains(amigo)){
            return amigos.add(amigo)
        }
        return false
    }

    fun eliminarAmigo(amigo: Usuario) {
        if (amigos.contains(amigo)){
            amigos.remove(amigo)
        }
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