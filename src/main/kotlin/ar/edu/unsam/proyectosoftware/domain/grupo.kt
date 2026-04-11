package ar.edu.unsam.proyectosoftware.domain

import ar.edu.unsam.proyectosoftware.dto.GrupoNombreIdDTO
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.LocalDate
import kotlin.collections.mutableListOf

@Entity
@Table(name = "grupos")
class Grupo(
    var nombre: String = "Grupo",
    var descripcion: String = "",
    var imagenUrl: String = "",
    val fechaCreacion: LocalDate,
) {
    @Id
    @GeneratedValue
    var id: Long? = null

    @ManyToMany(fetch = FetchType.LAZY)
    val integrantes = mutableListOf<Usuario>()

    @ManyToMany(fetch = FetchType.LAZY)
    val admins = mutableListOf<Usuario>()

    var modoRata: Boolean = false // TODO: Implementar feature

    fun agregarIntegrante(nuevo: Usuario) {
        integrantes.add(nuevo)
    }

    fun registrarGasto(gasto: Gasto) {
        // TODO()
    }

    fun esMiembro(usuario: Usuario): Boolean {
        return integrantes.contains(usuario)
    }

    fun esAdmin(usuario: Usuario): Boolean {
        return admins.contains(usuario)
    }

    companion object {
        fun toGrupoNombreIdDTO(grupo: Grupo): GrupoNombreIdDTO {
            return GrupoNombreIdDTO(
                id = grupo.id!!,
                nombre = grupo.nombre
            )
        }
    }
}