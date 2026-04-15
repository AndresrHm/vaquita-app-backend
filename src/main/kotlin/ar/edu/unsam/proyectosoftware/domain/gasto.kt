package ar.edu.unsam.proyectosoftware.domain

import ar.edu.unsam.proyectosoftware.domain.CategoriaGasto
import ar.edu.unsam.proyectosoftware.dto.CreacionGastoDTO
import jakarta.persistence.CascadeType
import jakarta.persistence.Id
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import java.time.LocalDate

@Entity
class Gasto(
    var descripcion: String,
    @Enumerated(EnumType.STRING)
    var categoria: CategoriaGasto,
    var monto: Double,
    val fecha: LocalDate,
    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.MERGE])
    @JoinColumn(name = "grupo_id")
    val grupo: Grupo, // Un gasto no tiene sentido si no pertenece a un grupo
    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.MERGE])
    @JoinColumn(name = "pagador_id")
    val pagador: Usuario,
    @ManyToMany(fetch = FetchType.LAZY, cascade = [CascadeType.MERGE])
    @JoinTable(
        name = "gasto_participantes",
        joinColumns = [JoinColumn(name = "gasto_id")],
        inverseJoinColumns = [JoinColumn(name = "usuario_id")]
    )
    val participantes: List<Usuario>,
    @Enumerated(EnumType.STRING)
    val tipoGasto: TipoGasto
    //val archivo: String,
) {
    @Id
    @GeneratedValue
    var id: Long? = null

    @OneToMany(mappedBy = "gasto", cascade = [CascadeType.ALL], orphanRemoval = true)
    var items: MutableList<ItemGasto> = mutableListOf()

    fun agregarItem(nombre: String, cantidad: Double, precio: Double) {
        items.add(ItemGasto(nombre, cantidad, precio, this))
    }

    fun cantidadParticipantes(): Int =
        participantes.size

    fun repartirGasto(): List<FraccionGasto> = tipoGasto.repartirGasto(this)

    companion object {
        fun fromGastoDTO(dto: CreacionGastoDTO, grupo: Grupo, pagador: Usuario, participantes: List<Usuario>): Gasto {
            val gasto = Gasto(
                descripcion = dto.descripcion,
                categoria = dto.categoria,
                monto = dto.monto,
                fecha = dto.fecha,
                grupo = grupo,
                pagador = pagador,
                participantes = participantes,
                tipoGasto = dto.tipoGasto
            )
            if (dto.items.isEmpty()) {
                gasto.agregarItem("Pago único", 1.0, dto.monto)
            } else {
                dto.items.forEach {
                    gasto.agregarItem(it.nombre, it.cantidad, it.precioUnitario)
                }
            }

            return gasto
        }
    }

}