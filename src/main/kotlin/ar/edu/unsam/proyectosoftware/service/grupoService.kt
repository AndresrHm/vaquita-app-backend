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
import ar.edu.unsam.proyectosoftware.repository.FraccionGastoRepository
import ar.edu.unsam.proyectosoftware.repository.GastoRepository
import ar.edu.unsam.proyectosoftware.repository.GrupoRepository
import ar.edu.unsam.proyectosoftware.repository.IntegranteGrupoRepository
import ar.edu.unsam.proyectosoftware.repository.InvitacionGrupoRepository
import ar.edu.unsam.proyectosoftware.repository.MiembroProjection
import ar.edu.unsam.proyectosoftware.repository.UsuarioRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
enum class TipoFiltroGrupo {
    TODOS_LOS_GRUPOS,
    SALDOS_PENDIENTES,
    GRUPOS_A_LOS_QUE_DEBES,
    GRUPOS_QUE_TE_DEBEN
}

fun String?.asTipoFiltroGrupo(): TipoFiltroGrupo {
    return when (this?.lowercase()) {
        "all" -> TipoFiltroGrupo.TODOS_LOS_GRUPOS
        "pending" -> TipoFiltroGrupo.SALDOS_PENDIENTES
        "you-owe" -> TipoFiltroGrupo.GRUPOS_A_LOS_QUE_DEBES
        "owe-you" -> TipoFiltroGrupo.GRUPOS_QUE_TE_DEBEN
        else -> TipoFiltroGrupo.TODOS_LOS_GRUPOS
    }
}

@Service
@Transactional
class GrupoService(
    val grupoRepository: GrupoRepository,
    val integranteGrupoRepository: IntegranteGrupoRepository,
    val usuarioRepository: UsuarioRepository,
    val gastoRepository: GastoRepository,
    private val fraccionGastoRepository: FraccionGastoRepository,
    private val usuarioService: UsuarioService,
    private val actividadService: ActividadService,
) {
    fun obtenerGrupos(auth: Authentication, page: Int, size: Int, search: String? = null, filter: String? = null): Page<Grupo> {
        val usuarioLogueado = usuarioService.obtenerUsuarioLogueado(auth)
        val tipoFiltro = filter.asTipoFiltroGrupo()

        // Blindaje contra el size = 0
        val safeSize = if (size <= 0) 8 else size
        val pageable = PageRequest.of(page, safeSize, Sort.by("fechaCreacion"))

        // Obtener todos los grupos del usuario
        val grupos = if (search?.isNotBlank() == true) {
            integranteGrupoRepository.findGruposByUsuarioWithSearch(usuarioLogueado.id!!, search.trim(), PageRequest.of(0, Int.MAX_VALUE, Sort.by("fechaCreacion"))).content
        } else {
            integranteGrupoRepository.findGruposByUsuario(usuarioLogueado.id!!, PageRequest.of(0, Int.MAX_VALUE, Sort.by("fechaCreacion"))).content
        }

        // Aplicar filtro
        val gruposFiltrados = grupos.filter { grupo ->
            when (tipoFiltro) {
                TipoFiltroGrupo.TODOS_LOS_GRUPOS -> true
                TipoFiltroGrupo.SALDOS_PENDIENTES -> {
                    // Grupos que tienen saldos pendientes (el usuario debe o le deben)
                    gastoRepository.existeDeudaPendienteEnGrupo(grupo.id!!, usuarioLogueado.id!!) ||
                    gastoRepository.existeDeudaPendienteHaciaUsuario(grupo.id!!, usuarioLogueado.id!!)
                }
                TipoFiltroGrupo.GRUPOS_A_LOS_QUE_DEBES -> {
                    // Grupos donde el usuario le debe dinero a otros
                    gastoRepository.existeDeudaPendienteEnGrupo(grupo.id!!, usuarioLogueado.id!!)
                }
                TipoFiltroGrupo.GRUPOS_QUE_TE_DEBEN -> {
                    // Grupos donde otros le deben dinero al usuario
                    gastoRepository.existeDeudaPendienteHaciaUsuario(grupo.id!!, usuarioLogueado.id!!)
                }
            }
        }

        // Paginar el resultado filtrado
        val start = page * safeSize
        val end = minOf(start + safeSize, gruposFiltrados.size)
        val content = if (start < gruposFiltrados.size) gruposFiltrados.subList(start, end) else emptyList()

            return PageImpl(
            content,
            pageable,
            gruposFiltrados.size.toLong()
        )
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

    @Transactional(readOnly = true)
    fun obtenerGruposCompartidos(usuarioId1: Long, usuarioId2: Long): List<GrupoNombreIdDTO> {
        return grupoRepository.findGruposCompartidos(usuarioId1, usuarioId2)
            .map { Grupo.toGrupoNombreIdDTO(it) }
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

            actividadService.registrarIngresoGrupo(usuarioLogueado, grupoGuardado)

        miembrosAgregados.forEach { miembro ->
              actividadService.registrarIngresoGrupo(miembro, grupoGuardado, usuarioLogueado)
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
              actividadService.registrarIngresoGrupo(miembro, grupoGuardado, usuarioLogueado)
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

        val miembros = integranteGrupoRepository.obtenerMiembrosConRol(grupoId)
        val deudaPorUsuario = fraccionGastoRepository
            .findAllByGastoAsociadoGrupoId(grupoId)
            .asSequence()
            .map { it.deudor.id!! to it.saldoPendiente() }
            .filter { (_, deuda) -> deuda > 0.0 }
            .groupBy({ it.first }, { it.second })
            .mapValues { (_, deudas) -> deudas.sum() }

        val usuarioMasDeudorId = deudaPorUsuario.maxByOrNull { it.value }?.key

        return miembros.map { miembro ->
            miembro.copy(esRata = miembro.id == usuarioMasDeudorId)
        }
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