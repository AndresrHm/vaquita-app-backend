package ar.edu.unsam.proyectosoftware.domain

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "invitaciones_grupo")
class InvitacionGrupo(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grupo_id", nullable = false)
    val grupo: Grupo,

    @Column(nullable = false, unique = true)
    val token: String = UUID.randomUUID().toString(),

    @Column(name = "fecha_expiracion", nullable = false)
    val fechaExpiracion: LocalDateTime,

    @Column(nullable = false)
    var activa: Boolean = true
) {
    fun estaExpirada(): Boolean {
        return LocalDateTime.now().isAfter(fechaExpiracion)
    }

    fun esValida(): Boolean {
        return activa && !estaExpirada()
    }
}