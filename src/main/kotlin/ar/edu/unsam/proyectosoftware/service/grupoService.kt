package ar.edu.unsam.proyectosoftware.service

import ar.edu.unsam.proyectosoftware.BusinessException
import ar.edu.unsam.proyectosoftware.domain.Gasto
import ar.edu.unsam.proyectosoftware.domain.Grupo
import ar.edu.unsam.proyectosoftware.domain.Usuario
import ar.edu.unsam.proyectosoftware.dto.GrupoContextoUsuarioDTO
import ar.edu.unsam.proyectosoftware.dto.GrupoCreacionDTO
import ar.edu.unsam.proyectosoftware.dto.GrupoNombreIdDTO
import ar.edu.unsam.proyectosoftware.dto.GrupoSettingsDTO
import ar.edu.unsam.proyectosoftware.dto.MiembroDTO
import ar.edu.unsam.proyectosoftware.errors.ErrorStrings
import ar.edu.unsam.proyectosoftware.repository.GastoRepository
import ar.edu.unsam.proyectosoftware.repository.GrupoRepository
import ar.edu.unsam.proyectosoftware.repository.UsuarioRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class GrupoService(
    var grupoRepository: GrupoRepository,
    var usuarioRepository: UsuarioRepository,
    var gastoRepository: GastoRepository
) {
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

    fun obtenerDetalleGrupo(
        grupoId: Long
    ): Grupo {
        val grupo = grupoRepository.findByIdOrNull(grupoId)
            ?: throw BusinessException(ErrorStrings.GRUPO_NO_ENCONTRADO)

        return grupo
    }

    fun obtenerCantIntegrantes(
        grupoId: Long
    ): Long {
        if (grupoRepository.findByIdOrNull(grupoId) == null) throw BusinessException(ErrorStrings.GRUPO_NO_ENCONTRADO)

        return grupoRepository.countIntegrantes(grupoId)
    }

    fun obtenerGastosGrupo(
        grupoId: Long,
        page: Int,
        size: Int
    ): Page<Gasto> {
        val pageable = PageRequest.of(page, size, Sort.by("fecha").descending())
        return gastoRepository.findByGrupoId(grupoId, pageable)
    }

    fun obtenerGruposDeUsuario(usuarioId: Long): List<Grupo> {
        buscarUsuario(usuarioId)

        return grupoRepository.findByIntegrantes_Id(usuarioId)
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
            imagenUrl = dto.imagenUrl,
            fechaCreacion = LocalDate.now()
        )

        grupo.agregarIntegrante(usuario)

        return grupoRepository.save(grupo)
    }

    fun obtenerGrupoNombrePorId(id: Long): GrupoNombreIdDTO {
        val grupo = grupoRepository.findByIdOrNull(id)
            ?: throw BusinessException(ErrorStrings.GRUPO_NO_ENCONTRADO)

        return GrupoNombreIdDTO(grupo.id!!, grupo.nombre)
    }

    // --- Services para GroupSettings ---
    fun obtenerSettings(grupoId: Long): Grupo {
        val grupo = grupoRepository.findByIdOrNull(grupoId)
            ?: throw BusinessException(ErrorStrings.GRUPO_NO_ENCONTRADO)

        return grupo
    }

    fun obtenerContextoUsuario(grupoId: Long, usuarioId: Long): GrupoContextoUsuarioDTO {
        val usuario = usuarioRepository.findByIdOrNull(usuarioId)
            ?: throw BusinessException(ErrorStrings.USUARIO_NO_ENCONTRADO)

        val grupo = grupoRepository.findByIdOrNull(grupoId)
            ?: throw BusinessException(ErrorStrings.GRUPO_NO_ENCONTRADO)

        return GrupoContextoUsuarioDTO(
            esAdmin = grupo.esAdmin(usuario),
            tieneDeudas = true // TODO: Hardcodeado
        )
    }

//    fun obtenerMiembros(grupoId: Long): List<MiembroDTO> {
//        val usuarios = grupoRepository.findBy
//
//        return usuarios.map { usuario ->
//            MiembroDTO(
//                id = usuario.id!!,
//                nombre = usuario.nombre,
//                rol = if (esAdminDelGrupo(usuario.id!!, grupoId)) "admin" else "member",
//                imagenUrl = usuario.imagenUrl
//            )
//        }
//    }

    // --- Private functions
    private fun buscarUsuario(usuarioId: Long): Usuario {
        return usuarioRepository.findByIdOrNull(usuarioId)
            ?: throw BusinessException(ErrorStrings.USUARIO_NO_ENCONTRADO)
    }
}
