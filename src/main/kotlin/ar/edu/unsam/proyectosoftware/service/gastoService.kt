package ar.edu.unsam.proyectosoftware.service

import ar.edu.unsam.proyectosoftware.BusinessException
import ar.edu.unsam.proyectosoftware.domain.Gasto
import ar.edu.unsam.proyectosoftware.domain.Grupo
import ar.edu.unsam.proyectosoftware.domain.Usuario
import ar.edu.unsam.proyectosoftware.dto.CreacionGastoDTO
import ar.edu.unsam.proyectosoftware.dto.FilterGastosCustom
import ar.edu.unsam.proyectosoftware.dto.GastoDetalleDTO
import ar.edu.unsam.proyectosoftware.dto.PageResponse
import ar.edu.unsam.proyectosoftware.dto.TipoRelacionVista
import ar.edu.unsam.proyectosoftware.dto.toPageResponse
import ar.edu.unsam.proyectosoftware.errors.ErrorStrings
import ar.edu.unsam.proyectosoftware.mapper.GastoMapper
import ar.edu.unsam.proyectosoftware.repository.FraccionGastoRepository
import ar.edu.unsam.proyectosoftware.repository.GastoRepository
import ar.edu.unsam.proyectosoftware.repository.GrupoRepository
import ar.edu.unsam.proyectosoftware.repository.UsuarioRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

val MAX_CARACTERES_DESCRIPCION = 50
val MAX_CARACTERES_NOMBRE_PRODUCTO = 20

@Service
class GastoService(
    var fraccionGastoRepository: FraccionGastoRepository,
    var gastoRepository: GastoRepository,
    var grupoRepository: GrupoRepository,
    var usuarioRepository: UsuarioRepository
) {

    @Transactional
    fun crearGasto(
        gastoDTO: CreacionGastoDTO
    ): Gasto {
        val grupo = obtenerGrupoOLanzarExcepcion(gastoDTO.grupoID)

        val pagador = obtenerUsuarioOLanzarExcepcion(gastoDTO.pagadorID)

        val participantes = usuarioRepository.findAllById(gastoDTO.participantesIDs).toList()

        if (participantes.size != gastoDTO.participantesIDs.size) {
            throw BusinessException(ErrorStrings.NINGUN_USUARIO_ENCONTRADO)
        }

        val gasto = GastoMapper.fromCreacionDTO(gastoDTO, grupo, pagador, participantes)

        validarDatosBasicos(gasto)

        if (!gasto.pagador.perteneceA(gasto.grupo)) {
            throw BusinessException(ErrorStrings.PAGADOR_NO_PERTENECE_AL_GRUPO)
        }

        if (!todosLosParticipantesPertenecenAlGrupo(gasto)) {
            throw BusinessException(ErrorStrings.PARTICIPANTES_NO_PERTENECEN_AL_GRUPO)
        }

        if (!puedeCrearGasto(gasto.pagador, gasto.grupo)) {
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

    fun obtenerDeuda(userId: Long): Double {
        obtenerUsuarioOLanzarExcepcion(userId)

        return fraccionGastoRepository.findByDeudorId(userId)
            .sumOf { it.saldoPendiente() }
    }

    @Transactional(readOnly = true)
    fun getGastosByFilters(
        userId: Long,
        filters: FilterGastosCustom,
    ): PageResponse<GastoDetalleDTO> {
        obtenerUsuarioOLanzarExcepcion(userId)

        // Validar rango de fechas
        if (filters.fechaInicio != null && filters.fechaFin != null && filters.fechaInicio > filters.fechaFin) {
            throw BusinessException(ErrorStrings.INFORMACION_INVALIDA)
        }

        // Validar rango de montos
        if (filters.rangoIni != null && filters.rangoFin != null && filters.rangoIni > filters.rangoFin) {
            throw BusinessException(ErrorStrings.INFORMACION_INVALIDA)
        }

        // Validar paginación
        if (filters.page < 0 || filters.size <= 0) {
            throw BusinessException(ErrorStrings.INFORMACION_INVALIDA)
        }

        // Procesar búsqueda: usar patrón LIKE solo si hay texto
        val searchPattern = filters.search
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.lowercase()
            ?.let { "%$it%" }
            ?: ""

        // Procesar categorías
        val categorias = filters.categoria
            ?.map { it.name }
            ?.distinct()
            ?: emptyList()

        val filtrarCategorias = categorias.isNotEmpty()

        // Procesar vistas (por defecto sin filtro de vista si no se especifica)
        val vistaPropia = filters.vista == TipoRelacionVista.PROPIA
        val vistaGrupo = filters.vista == TipoRelacionVista.GRUPO

        // Configurar paginación
        val pageable = PageRequest.of(
            filters.page,
            filters.size,
            Sort.by(Sort.Order.desc("fecha"), Sort.Order.desc("id"))
        )

        // Ejecutar consulta con todos los filtros normalizados
        val gastosPage = gastoRepository.buscarPorFiltros(
            searchPattern = searchPattern,
            fechaInicio = filters.fechaInicio,
            fechaFin = filters.fechaFin,
            rangoIni = filters.rangoIni,
            rangoFin = filters.rangoFin,
            filtrarCategorias = filtrarCategorias,
            categorias = categorias,
            userId = userId,
            amigoId = filters.amigoID,
            vistaPropia = vistaPropia,
            vistaGrupo = vistaGrupo,
            pageable = pageable,
        )

        return gastosPage.toPageResponse { GastoMapper.toViewDTO(it, userId) }
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
