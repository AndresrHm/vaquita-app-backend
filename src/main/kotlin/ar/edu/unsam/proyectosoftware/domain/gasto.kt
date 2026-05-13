package ar.edu.unsam.proyectosoftware.domain

import ar.edu.unsam.proyectosoftware.domain.enums.CategoriaGasto
import ar.edu.unsam.proyectosoftware.BusinessException
import ar.edu.unsam.proyectosoftware.domain.enums.TipoGasto
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
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
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
    //val archivo: String,
) {
    @Id
    @GeneratedValue
    var id: Long? = null

    @OneToMany(mappedBy = "gasto", cascade = [CascadeType.ALL], orphanRemoval = true)
    var items: MutableList<ItemGasto> = mutableListOf()

    // Tickets almacenados
    @OneToMany(mappedBy = "expense")
    val attachments: MutableList<ExpenseAttachment> = mutableListOf()

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
    abstract fun tipoGasto(): TipoGasto

    protected fun redondear(valor: Double): Double {
        return Math.round(valor * 100.0) / 100.0
    }
}

@Entity
@DiscriminatorValue("EXACT")
class PartesExactas(
    descripcion: String,
    categoria: CategoriaGasto,
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

    override fun tipoGasto() = TipoGasto.PARTES_EXACTAS
}

@Entity
@DiscriminatorValue("EQUAL")
class PartesIguales(
    descripcion: String,
    categoria: CategoriaGasto,
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

    override fun tipoGasto() = TipoGasto.PARTES_IGUALES

    private fun calcularMontoPorParticipante(): Double {
        return Math.round(monto / cantidadParticipantes() * 100) / 100.0
    }
}

@Entity
@DiscriminatorValue("PERCENTAGE")
class Porcentual(
    descripcion: String,
    categoria: CategoriaGasto,
    monto: Double,
    fecha: LocalDate,
    grupo: Grupo,
    pagador: Usuario,
    participantes: List<Usuario>,
    @ElementCollection
    @CollectionTable(name = "gasto_porcentajes", joinColumns = [JoinColumn(name = "gasto_id")])
    @MapKeyJoinColumn(name = "usuario_id")
    @Column(name = "porcentaje_asignado")
    val porcentajes: MutableMap<Usuario, Double> = mutableMapOf()
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
        return porcentajes
            .filter { (usuario, _) -> usuario.id != pagador.id }
            .map { (usuario, porcentaje) ->
                FraccionGasto(
                    deudor = usuario,
                    acreedor = pagador,
                    gastoAsociado = this,
                    montoFraccion = calcularMontoSegunPorcentaje(porcentaje),
                    pagoParcial = 0.0
                )
            }
    }

    override fun validar() {
        val sumaPorcentajes = porcentajes.values.sum()
        // Margen de error por redondeo
        if (Math.abs(sumaPorcentajes - 100.0) > 0.001) {
            throw BusinessException(ErrorStrings.PORCENTAJE_NO_SUMA_100)
        }

        if (porcentajes.values.any { it < 0 }) {
            throw BusinessException("Los porcentajes no pueden ser negativos")
        }
    }

    override fun tipoGasto() = TipoGasto.PORCENTUAL

    private fun calcularMontoSegunPorcentaje(porcentaje: Double): Double {
        val resultadoBruto = (monto * porcentaje) / 100.0
        return Math.round(resultadoBruto * 100) / 100.0
    }
}

@Entity
@DiscriminatorValue("RANDOM")
class Aleatorio(
    descripcion: String,
    categoria: CategoriaGasto,
    monto: Double,
    fecha: LocalDate,
    grupo: Grupo,
    pagador: Usuario,
    participantes: List<Usuario>
) : Gasto(descripcion, categoria, monto, fecha, grupo, pagador, participantes) {

    override fun repartirGasto(): List<FraccionGasto> {
        val n = participantes.size

        // Generamos N-1 puntos de corte aleatorios entre 0 y el monto total
        val cortes = List(n - 1) { (0..monto.toInt()).random().toDouble() }.sorted()

        // Calculamos las diferencias entre cortes para obtener las partes
        val montosAsignados = mutableListOf<Double>()
        var anterior = 0.0
        for (corte in cortes) {
            montosAsignados.add(redondear(corte - anterior))
            anterior = corte
        }
        montosAsignados.add(redondear(monto - anterior))

        // Mezclamos los montos para que no siempre el primero reciba lo mismo
        val montosMezclados = montosAsignados.shuffled()

        // Mapeamos a los participantes (saltando al pagador)
        return participantes.zip(montosMezclados)
            .filter { (usuario, _) -> usuario.id != pagador.id }
            .map { (usuario, montoFraccion) ->
                FraccionGasto(
                    deudor = usuario,
                    acreedor = pagador,
                    gastoAsociado = this,
                    montoFraccion = montoFraccion,
                    pagoParcial = 0.0
                )
            }
    }

    override fun validar(){}

    override fun tipoGasto() = TipoGasto.ALEATORIO
}