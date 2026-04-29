package ar.edu.unsam.proyectosoftware.domain

import ar.edu.unsam.proyectosoftware.dto.TipoActividad
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(
    name = "actividades",
    indexes = [
        Index(name = "idx_actividad_usuario_fecha", columnList = "usuario_id, fecha"),
    ]
)
class Actividad (
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    val usuario: Usuario,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val tipo: TipoActividad,
    @Column(nullable = false)
    val fecha: LocalDateTime = LocalDateTime.now(),
    @Column(columnDefinition = "TEXT")
    val descripcion: String? = null,
    // JSON serializado con detalles específicos
    @Column(columnDefinition = "TEXT")
    val metadata: String? = null
){
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}