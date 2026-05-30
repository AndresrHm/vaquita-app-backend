package ar.edu.unsam.proyectosoftware.repository

import ar.edu.unsam.proyectosoftware.domain.InvitacionGrupo
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface InvitacionGrupoRepository : JpaRepository<InvitacionGrupo, Long> {
    fun findByToken(token: String): Optional<InvitacionGrupo>
}