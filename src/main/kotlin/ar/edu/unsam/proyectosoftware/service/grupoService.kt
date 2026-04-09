package ar.edu.unsam.proyectosoftware.service

import ar.edu.unsam.proyectosoftware.BusinessException
import ar.edu.unsam.proyectosoftware.domain.Gasto
import ar.edu.unsam.proyectosoftware.domain.Grupo
import ar.edu.unsam.proyectosoftware.domain.Usuario
import ar.edu.unsam.proyectosoftware.dto.GrupoCreacionDTO
import ar.edu.unsam.proyectosoftware.errors.ErrorStrings
import ar.edu.unsam.proyectosoftware.repository.GastoRepository
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

    @Autowired
    lateinit var gastoRepository: GastoRepository

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

    @Transactional(readOnly = true)
    fun obtenerDetalleGrupo(
        grupoId: Long
    ): Grupo {
        val grupo = grupoRepository.findByIdOrNull(grupoId)
            ?: throw BusinessException(ErrorStrings.GRUPO_NO_ENCONTRADO)

        return grupo
    }

    @Transactional(readOnly = true)
    fun obtenerCantIntegrantes(
        grupoId: Long
    ): Long {
        if (grupoRepository.findByIdOrNull(grupoId) == null) throw BusinessException(ErrorStrings.GRUPO_NO_ENCONTRADO)

        return grupoRepository.countIntegrantes(grupoId)
    }

    @Transactional(readOnly = true)
    fun obtenerGastosGrupo(
        grupoId: Long,
        page: Int,
        size: Int
    ): Page<Gasto> {
        val pageable = PageRequest.of(page, size, Sort.by("fecha").descending())
        return gastoRepository.findByGrupoId(grupoId, pageable)
    }


    @Transactional(readOnly = true)
    fun obtenerGruposDeUsuario(usuarioId: Long): List<Grupo> {
        buscarUsuario(usuarioId)

        return grupoRepository.findByIntegrantes_Id(usuarioId)
    }

    @Transactional(readOnly = true)
    private fun buscarUsuario(usuarioId: Long): Usuario {
        return usuarioRepository.findByIdOrNull(usuarioId)
            ?: throw BusinessException(ErrorStrings.USUARIO_NO_ENCONTRADO)
    }

    @Transactional
    fun crearGrupo(
        usuarioId: Long,
        dto: GrupoCreacionDTO
    ): Grupo {
        val usuario = buscarUsuario(usuarioId)

        val grupo = Grupo(
            nombre = dto.nombre,
            descripcion = dto.descripcion,
            imagenUrl = dto.imagen,
            fechaCreacion = LocalDate.now()
        )

        grupo.agregarIntegrante(usuario)

        return grupoRepository.save(grupo)
    }
}
