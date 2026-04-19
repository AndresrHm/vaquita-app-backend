package ar.edu.unsam.proyectosoftware.service

import ar.edu.unsam.proyectosoftware.BusinessException
import ar.edu.unsam.proyectosoftware.domain.Gasto
import ar.edu.unsam.proyectosoftware.domain.Grupo
import ar.edu.unsam.proyectosoftware.domain.Usuario
import ar.edu.unsam.proyectosoftware.dto.CreacionGastoDTO
import ar.edu.unsam.proyectosoftware.repository.GastoRepository
import ar.edu.unsam.proyectosoftware.errors.ErrorStrings
import ar.edu.unsam.proyectosoftware.repository.FraccionGastoRepository
import ar.edu.unsam.proyectosoftware.repository.GrupoRepository
import ar.edu.unsam.proyectosoftware.repository.UsuarioRepository
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

val MAX_CARACTERES_DESCRIPCION = 50
val MAX_CARACTERES_NOMBRE_PRODUCTO = 20

@Service
class GastoService(
    var fraccionGastoRepository: FraccionGastoRepository,
    var gastoRepository: GastoRepository,
    var grupoRepository: GrupoRepository,
    var usuarioRepository: UsuarioRepository,
    private val usuarioService: UsuarioService // Agregado para el refactor
) {

    @Transactional(readOnly = false, rollbackFor = [Exception::class])
    fun crearGasto(
        auth: Authentication,
        gastoDTO: CreacionGastoDTO
    ): Gasto {
        // 1. Identificamos QUIÉN está usando la app (el creador)
        val usuarioQueCargaElGasto = usuarioService.obtenerUsuarioLogueado(auth)

        val grupo = obtenerGrupoOLanzarExcepcion(gastoDTO.grupoID)

        // 2. Buscamos al pagador que viene en el DTO (puede ser el mismo u otro)
        val pagador = obtenerUsuarioOLanzarExcepcion(gastoDTO.pagadorID)

        val participantes = usuarioRepository.findAllById(gastoDTO.participantesIDs).toList()

        if (participantes.size != gastoDTO.participantesIDs.size) {
            throw BusinessException(ErrorStrings.NINGUN_USUARIO_ENCONTRADO)
        }

        val gasto = Gasto.fromGastoDTO(gastoDTO, grupo, pagador, participantes)

        validarDatosBasicos(gasto)

        // --- VALIDACIONES DE SEGURIDAD ---

        // Validamos que el que está cargando el gasto PERTENECE al grupo
        if (!usuarioQueCargaElGasto.perteneceA(gasto.grupo)) {
            throw BusinessException(ErrorStrings.USUARIO_SIN_PERMISO_GASTO)
        }

        // Validamos que el pagador elegido PERTENECE al grupo
        if (!gasto.pagador.perteneceA(gasto.grupo)) {
            throw BusinessException(ErrorStrings.PAGADOR_NO_PERTENECE_AL_GRUPO)
        }

        if (!todosLosParticipantesPertenecenAlGrupo(gasto)) {
            throw BusinessException(ErrorStrings.PARTICIPANTES_NO_PERTENECEN_AL_GRUPO)
        }

        // Aquí podrías agregar: if (!usuarioQueCargaElGasto.esAdmin) { ... } si solo admins cargan por otros
        if (!puedeCrearGasto(usuarioQueCargaElGasto, gasto.grupo)) {
            throw BusinessException(ErrorStrings.USUARIO_SIN_PERMISO_GASTO)
        }

        if (!participantesContienePagador(participantes, pagador)) {
            throw BusinessException(ErrorStrings.USUARIO_SIN_PERMISO_GASTO)
        }

        val fracciones = gasto.repartirGasto()

        gastoRepository.save(gasto)
        fraccionGastoRepository.saveAll(fracciones)

        return gasto
    }

    fun obtenerDeuda(auth: Authentication): Double {
        val usuarioLogueado = usuarioService.obtenerUsuarioLogueado(auth)
        val userId = usuarioLogueado.id!!

        obtenerUsuarioOLanzarExcepcion(userId)

        return fraccionGastoRepository.findByDeudorId(userId)
            .sumOf { it.saldoPendiente() }
    }

    private fun validarDatosBasicos(gasto: Gasto) {
        if (gasto.monto <= 0) {
            throw BusinessException(ErrorStrings.MONTO_NEGATIVO)
        }
        if (gasto.monto != gasto.totalItems()) {
            throw BusinessException(ErrorStrings.MONTO_TOTAL_NO_COINCIDE_CON_TOTAL_ITEMS)
        }
        if (gasto.descripcion.isBlank()) {
            throw BusinessException(ErrorStrings.DESCRIPCION_VACIA)
        }
        if (gasto.descripcion.length > MAX_CARACTERES_DESCRIPCION) {
            throw BusinessException(ErrorStrings.DESCRIPCION_LARGA(MAX_CARACTERES_DESCRIPCION))
        }
        if (gasto.items.any { it.nombre.length > MAX_CARACTERES_NOMBRE_PRODUCTO }) {
            throw BusinessException(ErrorStrings.NOMBRE_PRODUCTO_LARGO(MAX_CARACTERES_NOMBRE_PRODUCTO))
        }
    }

    // Métodos auxiliares
    private fun participantesContienePagador(
        participantes: List<Usuario>,
        pagador: Usuario
    ): Boolean {
        return participantes.contains(pagador)
    }

    private fun todosLosParticipantesPertenecenAlGrupo(
        gasto: Gasto
    ): Boolean {
        val participantes = gasto.participantes
        val grupo = gasto.grupo
        return participantes.all { it.perteneceA(grupo) }
    }

    private fun puedeCrearGasto(usuario: Usuario, grupo: Grupo): Boolean {
        // Por ahora devolvemos true, pero acá iría la lógica de roles (administrador)
        return true
    }

    private fun obtenerGrupoOLanzarExcepcion(grupoId: Long): Grupo =
        grupoRepository.findById(grupoId)
            .orElseThrow { BusinessException(ErrorStrings.GRUPO_NO_ENCONTRADO) }

    private fun obtenerUsuarioOLanzarExcepcion(usuarioId: Long): Usuario =
        usuarioRepository.findById(usuarioId)
            .orElseThrow { BusinessException(ErrorStrings.USUARIO_NO_ENCONTRADO) }
}

