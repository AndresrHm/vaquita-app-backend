package ar.edu.unsam.proyectosoftware.domain

import ar.edu.unsam.proyectosoftware.dto.GrupoNombreIdDTO
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
    var imagenUrl: String = "",
    val fechaCreacion: LocalDate,
    var modoRata: Boolean = false, // TODO: Implementar feature
) {
    @Id
    @GeneratedValue
    var id: Long? = null

    @ManyToMany(fetch = FetchType.LAZY)
    var integrantes = mutableListOf<Usuario>()

    fun agregarIntegrante(nuevo: Usuario) {
        integrantes.add(nuevo)
    }

    fun registrarGasto(gasto: Gasto) {
        // TODO()
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