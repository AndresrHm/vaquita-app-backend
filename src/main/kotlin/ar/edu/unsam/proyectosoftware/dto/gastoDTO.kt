package ar.edu.unsam.proyectosoftware.dto

import java.time.LocalDate

data class GastoDTO(
    val id: Long?,
    val descripcion: String,
    val categoria: String,
    val monto: Double,
    val fecha: LocalDate,
    val grupoID: Long,
    val pagadorID: Long,
){

}