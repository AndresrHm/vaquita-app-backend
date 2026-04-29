/*
package ar.edu.unsam.proyectosoftware.service

import ar.edu.unsam.proyectosoftware.BusinessException
import ar.edu.unsam.proyectosoftware.domain.Gasto
import ar.edu.unsam.proyectosoftware.domain.Grupo
import ar.edu.unsam.proyectosoftware.domain.RolIntegrante
import ar.edu.unsam.proyectosoftware.domain.Usuario
import ar.edu.unsam.proyectosoftware.dto.GrupoContextoUsuarioDTO
import ar.edu.unsam.proyectosoftware.dto.GrupoCreacionDTO
import ar.edu.unsam.proyectosoftware.dto.GrupoNombreIdDTO
import ar.edu.unsam.proyectosoftware.dto.GrupoSettingsDTO
import ar.edu.unsam.proyectosoftware.dto.TipoActividad
import ar.edu.unsam.proyectosoftware.errors.ErrorStrings
import ar.edu.unsam.proyectosoftware.repository.GastoRepository
import ar.edu.unsam.proyectosoftware.repository.GrupoRepository
import ar.edu.unsam.proyectosoftware.repository.IntegranteGrupoRepository
import ar.edu.unsam.proyectosoftware.repository.MiembroProjection
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
    val grupoRepository: GrupoRepository,
    val integranteGrupoRepository: IntegranteGrupoRepository,
    val usuarioRepository: UsuarioRepository,
    val gastoRepository: GastoRepository,
) {
    fun obtenerGrupos(
        usuarioId: Long,
        page: Int,
        size: Int,
    ): Page<Grupo> {

        if (usuarioRepository.findByIdOrNull(usuarioId) == null)
            throw BusinessException(ErrorStrings.USUARIO_NO_ENCONTRADO)

        val pageable = PageRequest.of(
            page,
            size,
            Sort.by("fechaCreacion")
        )

        return integranteGrupoRepository.findGruposByUsuario(usuarioId, pageable)
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

        return integranteGrupoRepository.countByGrupoId(grupoId)
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

        return integranteGrupoRepository.findByIntegrantesIdAndActivadoTrue(usuarioId)
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

        grupo.agregarIntegrante(usuario, RolIntegrante.ADMIN)
        agregarMiembrosAlGrupo(grupo, dto.integrantesIds.filterNot { it == usuario.id })

        return grupoRepository.save(grupo)
    }

    @Transactional
    fun agregarMiembros(
        grupoId: Long,
        usuarioId: Long,
        usuariosAgregarIds: List<Long>
    ): Grupo {
        val grupo = grupoRepository.findByIdOrNull(grupoId)
            ?: throw BusinessException(ErrorStrings.GRUPO_NO_ENCONTRADO)

        // Validar que el usuario actual es admin
        if (!integranteGrupoRepository.esAdmin(
                grupoId,
                usuarioId
            )
        ) throw BusinessException(ErrorStrings.USUARIO_NO_ES_ADMIN)

        agregarMiembrosAlGrupo(grupo, usuariosAgregarIds)

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

        if (!grupoRepository.existsById(grupoId))
            throw BusinessException(ErrorStrings.GRUPO_NO_ENCONTRADO)

        if (!integranteGrupoRepository.esMiembro(grupoId, usuarioId))
            throw BusinessException(ErrorStrings.USUARIO_NO_PERTENECE_AL_GRUPO)

        return GrupoContextoUsuarioDTO(
            usuarioId = usuarioId,
            esAdmin = integranteGrupoRepository.esAdmin(grupoId, usuarioId),
            tieneDeudas = tieneDeudas(grupoId, usuarioId),
        )
    }

    fun obtenerMiembrosConRol(grupoId: Long): List<MiembroProjection> {
        if (!grupoRepository.existsById(grupoId)) {
            throw BusinessException(ErrorStrings.GRUPO_NO_ENCONTRADO)
        }

        return integranteGrupoRepository.obtenerMiembrosConRol(grupoId)
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
        currentUserId: Long,
        usuarioAEliminarId: Long
    ) {

        val grupo = grupoRepository.findByIdOrNull(grupoId)
            ?: throw BusinessException(ErrorStrings.GRUPO_NO_ENCONTRADO)

        val integrantes = integranteGrupoRepository.obtenerMiembrosConRol(grupoId)

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
        integranteGrupoRepository.deleteByGrupoIdAndUsuarioId(grupoId, usuarioAEliminarId)

        grupoRepository.save(grupo)
    }

    @Transactional
    fun eliminarGrupo(grupoId: Long, currentUserId: Long) {
        // Valida que el usuario que solicita la baja exista en el sistema.
        buscarUsuario(currentUserId)

        val grupo = grupoRepository.findByIdOrNull(grupoId)
            ?: throw BusinessException(ErrorStrings.GRUPO_NO_ENCONTRADO)

        // Si ya estaba desactivado, se considera no disponible para operar.
        if (!grupo.activado)
            throw BusinessException(ErrorStrings.GRUPO_NO_ENCONTRADO)

        // Solo un administrador del grupo puede desactivarlo.
        if (!integranteGrupoRepository.esAdmin(grupoId, currentUserId))
            throw BusinessException(ErrorStrings.USUARIO_NO_ES_ADMIN)

        // Baja lógica: preserva histórico, pero oculta el grupo como activo.
        grupo.activado = false
        grupoRepository.save(grupo)
    }

    // --- Private functions
    private fun buscarUsuario(usuarioId: Long): Usuario {
        return usuarioRepository.findByIdOrNull(usuarioId)
            ?: throw BusinessException(ErrorStrings.USUARIO_NO_ENCONTRADO)
    }

    private fun agregarMiembrosAlGrupo(grupo: Grupo, usuariosAgregarIds: List<Long>) {
        usuariosAgregarIds.distinct().forEach { id ->
            val usuario = buscarUsuario(id)

            if (grupo.integrantes.any { it.usuario.id == id }) {
                throw BusinessException(ErrorStrings.USUARIO_YA_PERTENECE_AL_GRUPO)
            }

            grupo.agregarIntegrante(usuario, RolIntegrante.MIEMBRO)
        }
    }

    */
/**
     * Consulta en el repositorio de gastos si el usuario figura como deudor
     * con saldo pendiente en alguna fracción de gastos del grupo.
     *//*

    private fun tieneDeudas(grupoId: Long, usuarioId: Long): Boolean {
        return gastoRepository.existeDeudaPendienteEnGrupo(grupoId, usuarioId)
    }
}
*/

package ar.edu.unsam.proyectosoftware.service

import ar.edu.unsam.proyectosoftware.BusinessException
import ar.edu.unsam.proyectosoftware.domain.Gasto
import ar.edu.unsam.proyectosoftware.domain.Grupo
import ar.edu.unsam.proyectosoftware.domain.RolIntegrante
import ar.edu.unsam.proyectosoftware.domain.Usuario
import ar.edu.unsam.proyectosoftware.dto.GrupoContextoUsuarioDTO
import ar.edu.unsam.proyectosoftware.dto.GrupoCreacionDTO
import ar.edu.unsam.proyectosoftware.dto.GrupoNombreIdDTO
import ar.edu.unsam.proyectosoftware.dto.GrupoSettingsDTO
import ar.edu.unsam.proyectosoftware.dto.TipoActividad
import ar.edu.unsam.proyectosoftware.errors.ErrorStrings
import ar.edu.unsam.proyectosoftware.repository.GastoRepository
import ar.edu.unsam.proyectosoftware.repository.GrupoRepository
import ar.edu.unsam.proyectosoftware.repository.IntegranteGrupoRepository
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
@Transactional
class GrupoService(
    val grupoRepository: GrupoRepository,
    val integranteGrupoRepository: IntegranteGrupoRepository,
    val usuarioRepository: UsuarioRepository,
    val gastoRepository: GastoRepository,
    private val usuarioService: UsuarioService,
    private val actividadService: ActividadService,
) {
    fun obtenerGrupos(auth: Authentication, page: Int, size: Int): Page<Grupo> {
        val usuarioLogueado = usuarioService.obtenerUsuarioLogueado(auth)
        // Blindaje contra el size = 0
        val safeSize = if (size <= 0) 8 else size
        val pageable = PageRequest.of(page, safeSize, Sort.by("fechaCreacion"))
        return integranteGrupoRepository.findGruposByUsuario(usuarioLogueado.id!!, pageable)
    }

    fun obtenerDetalleGrupo(grupoId: Long, auth: Authentication): Grupo {
        val usuarioLogueado = usuarioService.obtenerUsuarioLogueado(auth)
        if (!integranteGrupoRepository.esMiembro(grupoId, usuarioLogueado.id!!)) {
            throw BusinessException(ErrorStrings.USUARIO_NO_PERTENECE_AL_GRUPO)
        }
        return grupoRepository.findByIdOrNull(grupoId)
            ?: throw BusinessException(ErrorStrings.GRUPO_NO_ENCONTRADO)
    }

    fun obtenerCantIntegrantes(grupoId: Long): Long {
        if (!grupoRepository.existsById(grupoId)) throw BusinessException(ErrorStrings.GRUPO_NO_ENCONTRADO)
        return integranteGrupoRepository.countByGrupoId(grupoId)
    }

    fun obtenerGastosGrupo(grupoId: Long, auth: Authentication, page: Int, size: Int): Page<Gasto> {
        val usuarioLogueado = usuarioService.obtenerUsuarioLogueado(auth)
        if (!integranteGrupoRepository.esMiembro(grupoId, usuarioLogueado.id!!)) {
            throw BusinessException(ErrorStrings.USUARIO_NO_PERTENECE_AL_GRUPO)
        }
        val safeSize = if (size <= 0) 10 else size
        val pageable = PageRequest.of(page, safeSize, Sort.by("fecha").descending())
        return gastoRepository.findByGrupoId(grupoId, pageable)
    }

    fun obtenerGruposDeUsuario(auth: Authentication): List<Grupo> {
        val usuarioLogueado = usuarioService.obtenerUsuarioLogueado(auth)
        return integranteGrupoRepository.findByIntegrantesIdAndActivadoTrue(usuarioLogueado.id!!)
    }

    @Transactional
    fun crearGrupo(auth: Authentication, dto: GrupoCreacionDTO): Grupo {
        val usuarioLogueado = usuarioService.obtenerUsuarioLogueado(auth)

        // Validación estricta para evitar crashes silenciosos
        if (dto.nombre.isBlank()) {
            throw BusinessException("El nombre del grupo es obligatorio")
        }

        val grupo = Grupo(
            nombre = dto.nombre.trim(),
            descripcion = dto.descripcion ?: "",
            imagenUrl = dto.imagenUrl ?: "",
            fechaCreacion = LocalDate.now()
        )

        grupo.agregarIntegrante(usuarioLogueado, RolIntegrante.ADMIN)

        // Aseguramos que la lista no sea nula y filtramos al creador por si el front lo mandó
        val miembrosIds = dto.integrantesIds
        val miembrosAgregados = agregarMiembrosAlGrupo(grupo, miembrosIds.filter { it != usuarioLogueado.id })

        val grupoGuardado = grupoRepository.save(grupo)

        actividadService.registrarActividad(
            usuario = usuarioLogueado,
            tipo = TipoActividad.INGRESO_GRUPO,
            descripcion = "Creaste el grupo ${grupoGuardado.nombre}.",
            metadata = mapOf(
                "grupoId" to grupoGuardado.id,
                "grupoNombre" to grupoGuardado.nombre,
            ),
        )

        miembrosAgregados.forEach { miembro ->
            actividadService.registrarActividad(
                usuario = miembro,
                tipo = TipoActividad.INGRESO_GRUPO,
                descripcion = "Te agregaron al grupo ${grupoGuardado.nombre}.",
                metadata = mapOf(
                    "grupoId" to grupoGuardado.id,
                    "grupoNombre" to grupoGuardado.nombre,
                    "actorId" to usuarioLogueado.id,
                    "actorNombre" to usuarioLogueado.nombre,
                ),
            )
        }

        return grupoGuardado
    }

    @Transactional
    fun agregarMiembros(grupoId: Long, auth: Authentication, usuariosAgregarIds: List<Long>): Grupo {
        val usuarioLogueado = usuarioService.obtenerUsuarioLogueado(auth)
        val grupo = grupoRepository.findByIdOrNull(grupoId)
            ?: throw BusinessException(ErrorStrings.GRUPO_NO_ENCONTRADO)

        if (!integranteGrupoRepository.esAdmin(grupoId, usuarioLogueado.id!!)) {
            throw BusinessException(ErrorStrings.USUARIO_NO_ES_ADMIN)
        }

        val miembrosAgregados = agregarMiembrosAlGrupo(grupo, usuariosAgregarIds)
        val grupoGuardado = grupoRepository.save(grupo)

        miembrosAgregados.forEach { miembro ->
            actividadService.registrarActividad(
                usuario = miembro,
                tipo = TipoActividad.INGRESO_GRUPO,
                descripcion = "Te agregaron al grupo ${grupoGuardado.nombre}.",
                metadata = mapOf(
                    "grupoId" to grupoGuardado.id,
                    "grupoNombre" to grupoGuardado.nombre,
                    "actorId" to usuarioLogueado.id,
                    "actorNombre" to usuarioLogueado.nombre,
                ),
            )
        }

        return grupoGuardado
    }

    fun obtenerGrupoNombrePorId(id: Long): GrupoNombreIdDTO {
        val grupo = grupoRepository.findByIdOrNull(id)
            ?: throw BusinessException(ErrorStrings.GRUPO_NO_ENCONTRADO)
        return GrupoNombreIdDTO(grupo.id!!, grupo.nombre)
    }

    fun obtenerSettings(grupoId: Long, auth: Authentication): Grupo {
        val usuarioLogueado = usuarioService.obtenerUsuarioLogueado(auth)
        if (!integranteGrupoRepository.esMiembro(grupoId, usuarioLogueado.id!!)) {
            throw BusinessException(ErrorStrings.USUARIO_NO_PERTENECE_AL_GRUPO)
        }
        return grupoRepository.findByIdOrNull(grupoId)
            ?: throw BusinessException(ErrorStrings.GRUPO_NO_ENCONTRADO)
    }

    fun obtenerContextoUsuario(grupoId: Long, auth: Authentication): GrupoContextoUsuarioDTO {
        val usuarioLogueado = usuarioService.obtenerUsuarioLogueado(auth)
        val usuarioId = usuarioLogueado.id!!

        if (!grupoRepository.existsById(grupoId))
            throw BusinessException(ErrorStrings.GRUPO_NO_ENCONTRADO)

        if (!integranteGrupoRepository.esMiembro(grupoId, usuarioId))
            throw BusinessException(ErrorStrings.USUARIO_NO_PERTENECE_AL_GRUPO)

        return GrupoContextoUsuarioDTO(
            usuarioId = usuarioId,
            esAdmin = integranteGrupoRepository.esAdmin(grupoId, usuarioId),
            tieneDeudas = tieneDeudas(grupoId, usuarioId),
        )
    }

    fun obtenerMiembrosConRol(grupoId: Long): List<MiembroProjection> {
        if (!grupoRepository.existsById(grupoId)) {
            throw BusinessException(ErrorStrings.GRUPO_NO_ENCONTRADO)
        }
        return integranteGrupoRepository.obtenerMiembrosConRol(grupoId)
    }

    @Transactional
    fun actualizarGrupo(grupoId: Long, auth: Authentication, grupoSettingsDTO: GrupoSettingsDTO): Grupo {
        val usuarioLogueado = usuarioService.obtenerUsuarioLogueado(auth)

        if (!integranteGrupoRepository.esAdmin(grupoId, usuarioLogueado.id!!)) {
            throw BusinessException(ErrorStrings.USUARIO_NO_ES_ADMIN)
        }
        if (grupoId != grupoSettingsDTO.id) throw BusinessException(ErrorStrings.INFORMACION_INVALIDA)

        val grupo = grupoRepository.findByIdOrNull(grupoId)
            ?: throw BusinessException(ErrorStrings.GRUPO_NO_ENCONTRADO)

        if (grupoSettingsDTO.nombre.isBlank()) throw BusinessException(ErrorStrings.NOMBRE_OBLIGATORIO)

        grupo.nombre = grupoSettingsDTO.nombre
        grupo.descripcion = grupoSettingsDTO.descripcion
        grupo.imagenUrl = grupoSettingsDTO.imagenUrl
        grupo.moneda = grupoSettingsDTO.moneda
        grupo.simplificarDeudas = grupoSettingsDTO.simplificarDeudas
        grupo.notificaciones = grupoSettingsDTO.notificaciones
        grupo.modoRata = grupoSettingsDTO.modoRata

        return grupoRepository.save(grupo)
    }

    @Transactional
    fun eliminarMiembro(grupoId: Long, auth: Authentication, usuarioAEliminarId: Long) {
        val usuarioLogueado = usuarioService.obtenerUsuarioLogueado(auth)
        val currentUserId = usuarioLogueado.id!!

        val grupo = grupoRepository.findByIdOrNull(grupoId)
            ?: throw BusinessException(ErrorStrings.GRUPO_NO_ENCONTRADO)

        val integrantes = integranteGrupoRepository.obtenerMiembrosConRol(grupoId)
        val miembroAEliminar = integrantes.find { it.id == usuarioAEliminarId }
            ?: throw BusinessException(ErrorStrings.USUARIO_NO_PERTENECE_AL_GRUPO)
        val currentUser = integrantes.find { it.id == currentUserId }
            ?: throw BusinessException(ErrorStrings.USUARIO_NO_PERTENECE_AL_GRUPO)

        if (!currentUser.esAdmin) throw BusinessException(ErrorStrings.USUARIO_NO_ES_ADMIN)
        if (currentUserId == usuarioAEliminarId) throw BusinessException(ErrorStrings.NO_PODES_ELIMINARTE)

        if (miembroAEliminar.esAdmin) {
            if (integrantes.count { it.esAdmin } <= 1) {
                throw BusinessException(ErrorStrings.NO_SE_PUEDE_ELIMINAR_ULTIMO_ADMIN)
            }
        }

        integranteGrupoRepository.deleteByGrupoIdAndUsuarioId(grupoId, usuarioAEliminarId)
        grupoRepository.save(grupo)

        actividadService.registrarActividad(
            usuario = buscarUsuario(usuarioAEliminarId),
            tipo = TipoActividad.EGRESO_GRUPO,
            descripcion = "Fuiste removido del grupo ${grupo.nombre}.",
            metadata = mapOf(
                "grupoId" to grupo.id,
                "grupoNombre" to grupo.nombre,
                "actorId" to currentUserId,
            ),
        )
    }

    @Transactional
    fun eliminarGrupo(grupoId: Long, auth: Authentication) {
        val usuarioLogueado = usuarioService.obtenerUsuarioLogueado(auth)
        val currentUserId = usuarioLogueado.id!!

        val grupo = grupoRepository.findByIdOrNull(grupoId)
            ?: throw BusinessException(ErrorStrings.GRUPO_NO_ENCONTRADO)

        if (!grupo.activado) throw BusinessException(ErrorStrings.GRUPO_NO_ENCONTRADO)

        if (!integranteGrupoRepository.esAdmin(grupoId, currentUserId)) {
            throw BusinessException(ErrorStrings.USUARIO_NO_ES_ADMIN)
        }

        grupo.activado = false
        grupoRepository.save(grupo)
    }

    // --- Private functions
    private fun buscarUsuario(usuarioId: Long): Usuario {
        return usuarioRepository.findByIdOrNull(usuarioId)
            ?: throw BusinessException(ErrorStrings.USUARIO_NO_ENCONTRADO)
    }

    private fun agregarMiembrosAlGrupo(grupo: Grupo, usuariosAgregarIds: List<Long>): List<Usuario> {
        val usuariosAgregados = mutableListOf<Usuario>()

        usuariosAgregarIds.distinct().forEach { id ->
            val usuario = buscarUsuario(id)
            if (grupo.integrantes.any { it.usuario.id == id }) {
                throw BusinessException(ErrorStrings.USUARIO_YA_PERTENECE_AL_GRUPO)
            }
            grupo.agregarIntegrante(usuario, RolIntegrante.MIEMBRO)
            usuariosAgregados.add(usuario)
        }

        return usuariosAgregados
    }

    private fun tieneDeudas(grupoId: Long, usuarioId: Long): Boolean {
        return gastoRepository.existeDeudaPendienteEnGrupo(grupoId, usuarioId)
    }
}