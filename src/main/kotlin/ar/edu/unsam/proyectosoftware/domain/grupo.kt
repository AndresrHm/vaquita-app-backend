package ar.edu.unsam.proyectosoftware.domain

import ar.edu.unsam.proyectosoftware.dto.GrupoNombreIdDTO
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
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
    var activado = true

    // TODO: Implementar estas features
    var moneda: String = "ARS"
    var simplificarDeudas: Boolean = false
    var notificaciones: Boolean = false
    var modoRata: Boolean = false

    // Esta config permite que al agregar o eliminar un integrante esto persista en la relación integranteGrupo también.
    @OneToMany(
        mappedBy = "grupo",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    val integrantes: MutableList<IntegranteGrupo> = mutableListOf()

    fun agregarIntegrante(usuario: Usuario, rol: RolIntegrante) {
        val yaExiste = integrantes.any { it.usuario.id == usuario.id }
        if (yaExiste) return

        val integrante = IntegranteGrupo(
            grupo = this,
            usuario = usuario,
            rol = rol
        )

        integrantes.add(integrante)
    }

    fun obtenerAdmins(): List<Usuario> {
        return integrantes
            .filter { it.rol == RolIntegrante.ADMIN }
            .map { it.usuario }
    }

    fun obtenerMiembros(): List<Usuario> {
        return integrantes
            .filter { it.rol == RolIntegrante.MIEMBRO }
            .map { it.usuario }
    }

    fun usuarios(): List<Usuario> {
        return integrantes.map { it.usuario }
    }

    fun usuarioEsMiembro(usuario: Usuario) = usuarios().contains(usuario)

    companion object {
        fun toGrupoNombreIdDTO(grupo: Grupo): GrupoNombreIdDTO {
            return GrupoNombreIdDTO(
                id = grupo.id!!,
                nombre = grupo.nombre
            )
        }
    }
}