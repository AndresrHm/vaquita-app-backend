package ar.edu.unsam.proyectosoftware.domain

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id

@Entity
class SolicitudAmistad(
    val idEmisor: Long,
    val idReceptor: Long){
    @Id
    @GeneratedValue
    var id: Long? = null
}

data class SolicitudAmistadFront(
    val id: Long,
    val nombre: String,
    val imagenUrl: String
)
