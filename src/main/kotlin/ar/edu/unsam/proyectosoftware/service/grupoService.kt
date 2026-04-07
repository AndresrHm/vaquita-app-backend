package ar.edu.unsam.proyectosoftware.service

import ar.edu.unsam.proyectosoftware.BusinessException
import ar.edu.unsam.proyectosoftware.domain.Grupo
import ar.edu.unsam.proyectosoftware.dto.GrupoCreacionDTO
import ar.edu.unsam.proyectosoftware.errors.ErrorStrings
import ar.edu.unsam.proyectosoftware.repository.GrupoRepository
import ar.edu.unsam.proyectosoftware.repository.UsuarioRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class GrupoService {
    @Autowired
    lateinit var grupoRepository: GrupoRepository

    @Autowired
    lateinit var usuarioRepository: UsuarioRepository

    @Transactional(readOnly = true)
    fun obtenerGrupos(
        usuarioId: Long,
        page: Int,
        size: Int,
    ): Page<Grupo> {

        if (usuarioRepository.findByIdOrNull(usuarioId) == null) throw BusinessException(ErrorStrings.USUARIO_NO_ENCONTRADO)

        val pageable = PageRequest.of(
            page,
            size,
            Sort.by(Sort.Direction.DESC, "fechaCreacion")
        )

        return grupoRepository.findByUsuario(usuarioId, pageable)
    }

    @Transactional
    fun crearGrupo(
        usuarioId: Long,
        dto: GrupoCreacionDTO
    ): Grupo {
        val usuario = usuarioRepository.findByIdOrNull(usuarioId)
            ?: throw BusinessException(ErrorStrings.USUARIO_NO_ENCONTRADO)

        val grupo = Grupo(
            nombre = dto.nombre,
            descripcion = dto.descripcion,
            imagen = dto.imagen,
            fechaCreacion = LocalDate.now()
        )

        grupo.agregarIntegrante(usuario)

        return grupoRepository.save(grupo)
    }
}