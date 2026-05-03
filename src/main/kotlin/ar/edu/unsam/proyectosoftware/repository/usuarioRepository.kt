package ar.edu.unsam.proyectosoftware.repository

import ar.edu.unsam.proyectosoftware.domain.Usuario
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface UsuarioRepository : CrudRepository<Usuario, Long> {
    fun findByEmail(email: String): Optional<Usuario>
    fun findByGrupos_Id(grupoId: Long): List<Usuario>

    fun existsByIdAndAmigosId(id: Long, amigoId: Long): Boolean
}