package ar.edu.unsam.proyectosoftware.domain

import ar.edu.unsam.proyectosoftware.dto.GrupoNombreIdDTO
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
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
    var activado = true

    // TODO: Implementar estas features
    var moneda: String = "ARS"
    var simplificarDeudas: Boolean = false
    var notificaciones: Boolean = false
    var modoRata: Boolean = false

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "grupo_integrantes",
        joinColumns = [JoinColumn(name = "grupo_id")],
        inverseJoinColumns = [JoinColumn(name = "usuario_id")]
    )
    val integrantes: MutableList<Usuario> = mutableListOf()

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "grupo_admins",
        joinColumns = [JoinColumn(name = "grupo_id")],
        inverseJoinColumns = [JoinColumn(name = "usuario_id")]
    )
    val admins: MutableList<Usuario> = mutableListOf()


    fun agregarIntegrante(nuevo: Usuario) {
        integrantes.add(nuevo)
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