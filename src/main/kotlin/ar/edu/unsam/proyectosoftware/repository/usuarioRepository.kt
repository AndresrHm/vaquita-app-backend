package ar.edu.unsam.proyectosoftware.repository

import ar.edu.unsam.proyectosoftware.domain.Usuario
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface UsuarioRepository : CrudRepository<Usuario, Long> {
    fun findByEmail(email: String): Optional<Usuario>
    fun findByGrupos_Id(grupoId: Long): List<Usuario>

    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.amigos WHERE u.id = :id")
    fun findByIdWithAmigos(id: Long): Optional<Usuario>
}