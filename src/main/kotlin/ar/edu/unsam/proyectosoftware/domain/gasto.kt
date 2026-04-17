package ar.edu.unsam.proyectosoftware.domain

import ar.edu.unsam.proyectosoftware.BusinessException
import ar.edu.unsam.proyectosoftware.dto.CreacionGastoDTO
import ar.edu.unsam.proyectosoftware.errors.ErrorStrings
import jakarta.persistence.CascadeType
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorColumn
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.ElementCollection
import jakarta.persistence.Id
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.MapKeyJoinColumn
import jakarta.persistence.OneToMany
import java.time.LocalDate

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_gasto_id")
abstract class Gasto(
    var descripcion: String,
    var categoria: String, // ¿class Categoria?
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

    fun totalItems(): Double {
        return items.sumOf { it.total() }
    }

    fun cantidadParticipantes(): Int =
        participantes.size

    abstract fun repartirGasto(): List<FraccionGasto>
    abstract fun validar()
}

@Entity
@DiscriminatorValue("EXACT")
class PartesExactas(
    descripcion: String,
    categoria: String,
    monto: Double,
    fecha: LocalDate,
    grupo: Grupo,
    pagador: Usuario,
    participantes: List<Usuario>,
    @ElementCollection //Para guardar usuario / gasto
    @CollectionTable(name = "gasto_asignaciones", joinColumns = [JoinColumn(name = "gasto_id")])
    @MapKeyJoinColumn(name = "usuario_id")
    @Column(name = "monto_asignado")
    val asignacionesExactas: MutableMap<Usuario, Double> = mutableMapOf()
) : Gasto(
    descripcion,
    categoria,
    monto,
    fecha,
    grupo,
    pagador,
    participantes
) {
    override fun repartirGasto(): List<FraccionGasto> {
        val fracciones = asignacionesExactas
            .filter { (usuario, _) -> usuario.id != pagador.id }
            .map { (usuario, montoEspecifico) ->
                FraccionGasto(
                    deudor = usuario,
                    acreedor = pagador,
                    gastoAsociado = this,
                    montoFraccion = montoEspecifico,
                    pagoParcial = 0.0
                )
            }
        return fracciones
    }

    override fun validar() {
        if (asignacionesExactas.values.any { it <= 0 }) {
            throw BusinessException(ErrorStrings.ASIGNACIONES_DEBEN_TENER_MONTO_POSITIVO)
        }

        val sumaAsignada = asignacionesExactas.values.sum()
        if (Math.abs(sumaAsignada - monto) > 0.01) {
            throw BusinessException(ErrorStrings.FRACCIONES_NO_COINCIDEN_CON_TOTAL_DE_GASTO)
        }
    }
}

@Entity
@DiscriminatorValue("EQUAL")
class PartesIguales(
    descripcion: String,
    categoria: String,
    monto: Double,
    fecha: LocalDate,
    grupo: Grupo,
    pagador: Usuario,
    participantes: List<Usuario>,
) : Gasto(
    descripcion,
    categoria,
    monto,
    fecha,
    grupo,
    pagador,
    participantes
) {
    override fun repartirGasto(): List<FraccionGasto> {
        val fracciones = mutableListOf<FraccionGasto>()
        participantes.forEach {
            if (it.id != pagador.id) {
                fracciones.add(
                    FraccionGasto(
                        deudor = it,
                        acreedor = pagador,
                        gastoAsociado = this,
                        montoFraccion = calcularMontoPorParticipante(),
                        pagoParcial = 0.0
                    )
                )
            }
        }
        return fracciones
    }

    override fun validar() {}

    private fun calcularMontoPorParticipante(): Double {
        return monto / cantidadParticipantes()
    }
}