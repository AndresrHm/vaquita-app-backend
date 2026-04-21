package ar.edu.unsam.proyectosoftware.domain
import jakarta.persistence.*

enum class RolIntegrante {
    ADMIN,
    MIEMBRO
}

@Entity
@Table(name = "integrantes_grupo")
class IntegranteGrupo(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grupo_id", nullable = false)
    var grupo: Grupo,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    var usuario: Usuario,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var rol: RolIntegrante = RolIntegrante.MIEMBRO

) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}