package ar.edu.unsam.proyectosoftware.service

import ar.edu.unsam.proyectosoftware.BusinessException
import ar.edu.unsam.proyectosoftware.domain.Gasto
import ar.edu.unsam.proyectosoftware.domain.Grupo
import ar.edu.unsam.proyectosoftware.domain.Usuario
import ar.edu.unsam.proyectosoftware.dto.GrupoContextoUsuarioDTO
import ar.edu.unsam.proyectosoftware.dto.GrupoCreacionDTO
import ar.edu.unsam.proyectosoftware.dto.GrupoNombreIdDTO
import ar.edu.unsam.proyectosoftware.dto.GrupoSettingsDTO
import ar.edu.unsam.proyectosoftware.errors.ErrorStrings
import ar.edu.unsam.proyectosoftware.repository.GastoRepository
import ar.edu.unsam.proyectosoftware.repository.GrupoRepository
import ar.edu.unsam.proyectosoftware.repository.MiembroProjection
import ar.edu.unsam.proyectosoftware.repository.UsuarioRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class GrupoService(
    var grupoRepository: GrupoRepository,
    var usuarioRepository: UsuarioRepository,
    var gastoRepository: GastoRepository,
    private val usuarioService: UsuarioService // Necesario para obtener al usuario del token
) {
    fun obtenerGrupos(
        auth: Authentication,
        page: Int,
        size: Int,
    ): Page<Grupo> {
        val usuarioLogueado = usuarioService.obtenerUsuarioLogueado(auth)
        val usuarioId = usuarioLogueado.id!!

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

    fun obtenerGruposDeUsuario(auth: Authentication): List<Grupo> {
        val usuarioLogueado = usuarioService.obtenerUsuarioLogueado(auth)
        val usuarioId = usuarioLogueado.id!!

        buscarUsuario(usuarioId)

        return grupoRepository.findByIntegrantes_Id(usuarioId)
    }

    @Transactional
    fun crearGrupo(
        auth: Authentication,
        dto: GrupoCreacionDTO
    ): Grupo {
        val usuarioLogueado = usuarioService.obtenerUsuarioLogueado(auth)
        val usuarioId = usuarioLogueado.id!!

        val usuario = buscarUsuario(usuarioId)

        val grupo = Grupo(
            nombre = dto.nombre,
            descripcion = dto.descripcion,
            imagenUrl = dto.imagenUrl,
            fechaCreacion = LocalDate.now()
        )

        grupo.agregarIntegrante(usuario)
        grupo.admins.add(usuario)

        return grupoRepository.save(grupo)
    }

    @Transactional
    fun agregarMiembros(
        grupoId: Long,
        auth: Authentication,
        usuariosAgregarIds: List<Long>
    ): Grupo {
        val usuarioLogueado = usuarioService.obtenerUsuarioLogueado(auth)
        val currentUserId = usuarioLogueado.id!!

        val grupo = grupoRepository.findByIdOrNull(grupoId)
            ?: throw BusinessException(ErrorStrings.GRUPO_NO_ENCONTRADO)

        // Validar que el usuario actual es admin
        val esAdmin = grupoRepository.esAdmin(grupoId, currentUserId)
        if (!esAdmin) {
            throw BusinessException(ErrorStrings.USUARIO_NO_ES_ADMIN)
        }

        // Agregar cada usuario
        usuariosAgregarIds.forEach { usuarioId ->
            val usuario = buscarUsuario(usuarioId)

            // Validar que no está ya en el grupo
            if (grupo.integrantes.any { it.id == usuarioId }) {
                throw BusinessException(ErrorStrings.USUARIO_YA_PERTENECE_AL_GRUPO)
            }

            grupo.agregarIntegrante(usuario)
        }

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

    fun obtenerContextoUsuario(grupoId: Long, auth: Authentication): GrupoContextoUsuarioDTO {
        val usuarioLogueado = usuarioService.obtenerUsuarioLogueado(auth)
        val usuarioId = usuarioLogueado.id!!

        if (!grupoRepository.existsById(grupoId)) {
            throw BusinessException(ErrorStrings.GRUPO_NO_ENCONTRADO)
        }

        val esMiembro = grupoRepository.esMiembro(grupoId, usuarioId)
        if (!esMiembro) {
            throw BusinessException(ErrorStrings.USUARIO_NO_PERTENECE_AL_GRUPO)
        }

        val esAdmin = grupoRepository.esAdmin(grupoId, usuarioId)

        val tieneDeudas = tieneDeudas(grupoId, usuarioId) // TODO: Implementar

        return GrupoContextoUsuarioDTO(
            usuarioId = usuarioId,
            esAdmin = esAdmin,
            tieneDeudas = tieneDeudas,
        )
    }

    fun obtenerMiembrosConRol(grupoId: Long): List<MiembroProjection> {
        if (!grupoRepository.existsById(grupoId)) {
            throw BusinessException(ErrorStrings.GRUPO_NO_ENCONTRADO)
        }

        return grupoRepository.obtenerMiembrosConRol(grupoId)
    }

    fun actualizarGrupo(grupoId: Long, grupoSettingsDTO: GrupoSettingsDTO): Grupo {
        if (grupoId != grupoSettingsDTO.id) throw BusinessException(ErrorStrings.INFORMACION_INVALIDA)

        val grupo = grupoRepository.findByIdOrNull(grupoId)
            ?: throw BusinessException(ErrorStrings.GRUPO_NO_ENCONTRADO)

        if (grupoSettingsDTO.nombre.isBlank()) {
            throw BusinessException(ErrorStrings.NOMBRE_OBLIGATORIO)
        }

        grupo.nombre = grupoSettingsDTO.nombre
        grupo.descripcion = grupoSettingsDTO.descripcion
        grupo.imagenUrl = grupoSettingsDTO.imagenUrl
        grupo.moneda = grupoSettingsDTO.moneda
        grupo.simplificarDeudas = grupoSettingsDTO.simplificarDeudas
        grupo.notificaciones = grupoSettingsDTO.notificaciones
        grupo.modoRata = grupoSettingsDTO.modoRata

        val guardado = grupoRepository.save(grupo)

        return guardado
    }

    @Transactional
    fun eliminarMiembro(
        grupoId: Long,
        auth: Authentication,
        usuarioAEliminarId: Long
    ) {
        val usuarioLogueado = usuarioService.obtenerUsuarioLogueado(auth)
        val currentUserId = usuarioLogueado.id!!

        val grupo = grupoRepository.findByIdOrNull(grupoId)
            ?: throw BusinessException(ErrorStrings.GRUPO_NO_ENCONTRADO)

        val integrantes = grupoRepository.obtenerMiembrosConRol(grupoId)

        // 🔍 Usuario a eliminar
        val miembroAEliminar = integrantes.find { it.id == usuarioAEliminarId }
            ?: throw BusinessException(ErrorStrings.USUARIO_NO_PERTENECE_AL_GRUPO)

        // 🔍 Usuario actual (quien ejecuta)
        val currentUser = integrantes.find { it.id == currentUserId }
            ?: throw BusinessException(ErrorStrings.USUARIO_NO_PERTENECE_AL_GRUPO)

        // 🔒 Permisos
        if (!currentUser.esAdmin) {
            throw BusinessException(ErrorStrings.USUARIO_NO_ES_ADMIN)
        }

        // ❗ No puede eliminarse a sí mismo
        if (currentUserId == usuarioAEliminarId) {
            throw BusinessException(ErrorStrings.NO_PODES_ELIMINARTE)
        }

        // ❗ Si el target es admin → validar que no sea el último
        if (miembroAEliminar.esAdmin) {
            val cantidadAdmins = integrantes.count { it.esAdmin }

            if (cantidadAdmins <= 1) {
                throw BusinessException(ErrorStrings.NO_SE_PUEDE_ELIMINAR_ULTIMO_ADMIN)
            }
        }

        // 🧹 Eliminación en entidades
        grupo.integrantes.removeIf { it.id == usuarioAEliminarId }
        grupo.admins.removeIf { it.id == usuarioAEliminarId }

        grupoRepository.save(grupo)
    }

    // --- Private functions
    private fun buscarUsuario(usuarioId: Long): Usuario {
        return usuarioRepository.findByIdOrNull(usuarioId)
            ?: throw BusinessException(ErrorStrings.USUARIO_NO_ENCONTRADO)
    }

    private fun tieneDeudas(grupoId: Long, usuarioId: Long): Boolean {
        return true // TODO: Implementar
    }
}