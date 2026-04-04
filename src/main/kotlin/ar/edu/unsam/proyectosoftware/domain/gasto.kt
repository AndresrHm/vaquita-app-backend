package ar.edu.unsam.proyectosoftware.domain

import ar.edu.unsam.proyectosoftware.dto.GastoDTO
import jakarta.persistence.Id
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import java.time.LocalDate

@Entity
class Gasto(
    var descripcion: String,
    var categoria: String, // ¿class Categoria?
    var monto: Double,
    val fecha: LocalDate,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grupo_id")
    val grupo: Grupo, // Un gasto no tiene sentido si no pertenece a un grupo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pagador_id")
    val pagador: Usuario
    //,val archivo: String
) {
    @Id
    @GeneratedValue
    var id: Long? = null

    // Quizás habría que tener entidades "splits" para llevar el control de pagos de cada usuario

    companion object {
        fun fromGastoDTO(dto: GastoDTO, grupo: Grupo, pagador: Usuario) = Gasto(
            descripcion = dto.descripcion,
            categoria = dto.categoria,
            monto = dto.monto,
            fecha = dto.fecha,
            grupo = grupo,
            pagador = pagador
        )
    }

}