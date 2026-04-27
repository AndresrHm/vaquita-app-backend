package ar.edu.unsam.proyectosoftware.service

import ar.edu.unsam.proyectosoftware.BusinessException
import ar.edu.unsam.proyectosoftware.domain.Gasto
import ar.edu.unsam.proyectosoftware.domain.Grupo
import ar.edu.unsam.proyectosoftware.domain.Usuario
import ar.edu.unsam.proyectosoftware.dto.BalanceDTO
import ar.edu.unsam.proyectosoftware.dto.CreacionGastoDTO
import ar.edu.unsam.proyectosoftware.dto.FilterGastosCustom
import ar.edu.unsam.proyectosoftware.dto.OrdenGasto
import ar.edu.unsam.proyectosoftware.dto.GastoDetalleDTO
import ar.edu.unsam.proyectosoftware.dto.PageResponse
import ar.edu.unsam.proyectosoftware.dto.PagoDTO
import org.springframework.security.core.Authentication
import ar.edu.unsam.proyectosoftware.dto.toPageResponse
import ar.edu.unsam.proyectosoftware.errors.ErrorStrings
import ar.edu.unsam.proyectosoftware.mapper.GastoMapper
import ar.edu.unsam.proyectosoftware.repository.FraccionGastoRepository
import ar.edu.unsam.proyectosoftware.repository.GastoRepository
import ar.edu.unsam.proyectosoftware.repository.GrupoRepository
import ar.edu.unsam.proyectosoftware.repository.IntegranteGrupoRepository
import ar.edu.unsam.proyectosoftware.repository.UsuarioRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ar.edu.unsam.proyectosoftware.dto.TipoRelacionVista

const val MAX_CARACTERES_DESCRIPCION = 50
const val MAX_CARACTERES_NOMBRE_PRODUCTO = 20

@Service
class GastoService(
    var fraccionGastoRepository: FraccionGastoRepository,
    var gastoRepository: GastoRepository,
    var integranteGrupoRepository: IntegranteGrupoRepository,
    var grupoRepository: GrupoRepository,
    var usuarioRepository: UsuarioRepository,
    private val usuarioService: UsuarioService
) {

    @Transactional
    fun crearGasto(
        auth: Authentication,
        gastoDTO: CreacionGastoDTO
    ): Gasto {
        val usuarioLogueado = usuarioService.obtenerUsuarioLogueado(auth)

        val grupo = obtenerGrupoOLanzarExcepcion(gastoDTO.grupoID)
        val pagador = obtenerUsuarioOLanzarExcepcion(gastoDTO.pagadorID)
        val participantes = integranteGrupoRepository.findUsuariosByGrupoAndIds(grupo.id!!, gastoDTO.participantesIDs)

        if (participantes.size != gastoDTO.participantesIDs.size) {
            throw BusinessException(ErrorStrings.NINGUN_USUARIO_ENCONTRADO)
        }

        val gasto = GastoMapper.fromCreacionDTO(gastoDTO, grupo, pagador, participantes)

        validarDatosBasicos(gasto)

        if (!existeIntegranteYGrupo(grupo.id, gasto)) {
            throw BusinessException(ErrorStrings.PAGADOR_NO_PERTENECE_AL_GRUPO)
        }

        if (!integranteGrupoRepository.existsByGrupoIdAndUsuarioId(grupo.id!!, usuarioLogueado.id!!)) {
            throw BusinessException(ErrorStrings.USUARIO_SIN_PERMISO_GASTO)
        }

        if (!integranteGrupoRepository.existsByGrupoIdAndUsuarioId(grupo.id!!, gasto.pagador.id!!)) {
            throw BusinessException(ErrorStrings.PAGADOR_NO_PERTENECE_AL_GRUPO)
        }

        if (!todosLosParticipantesPertenecenAlGrupo(gasto)) {
            throw BusinessException(ErrorStrings.PARTICIPANTES_NO_PERTENECEN_AL_GRUPO)
        }

        if (!puedeCrearGasto(usuarioLogueado, gasto.grupo)) {
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

    @Transactional(readOnly = true) // Si no está el transactional, no reconoce los objetos
    fun getGastosByFilters(
        auth: Authentication,
        filters: FilterGastosCustom,
    ): PageResponse<GastoDetalleDTO> {
        val usuarioLogueado = usuarioService.obtenerUsuarioLogueado(auth)
        val userId = usuarioLogueado.id!!

        obtenerUsuarioOLanzarExcepcion(userId)

        if (filters.fechaInicio != null && filters.fechaFin != null && filters.fechaInicio > filters.fechaFin) {
            throw BusinessException(ErrorStrings.INFORMACION_INVALIDA)
        }

        if (filters.rangoIni != null && filters.rangoFin != null && filters.rangoIni > filters.rangoFin) {
            throw BusinessException(ErrorStrings.INFORMACION_INVALIDA)
        }

        if (filters.page < 0 || filters.size <= 0) {
            throw BusinessException(ErrorStrings.INFORMACION_INVALIDA)
        }

        val searchPattern = filters.search
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.lowercase()
            ?.let { "%$it%" }
            ?: ""

        val categorias = filters.categoria
            ?.map { it.name }
            ?.distinct()
            ?: emptyList()

        val filtrarCategorias = categorias.isNotEmpty()

        val vistaFlags = resolverVistaFlags(filters.vista)

        val sort = construirSort(filters.orden)


        val pageable = PageRequest.of(
            filters.page,
            filters.size,
            sort
        )

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
            vistaTodos = vistaFlags.todos,
            vistaDeuda = vistaFlags.deuda,
            vistaAmigos = vistaFlags.amigos,
            pageable = pageable,
        )

        return gastosPage.toPageResponse { GastoMapper.toViewDTO(it, userId) }
    }

    @Transactional(readOnly = true) // Si no está el transactional, no reconoce los objetos
    fun obtenerBalancesSimplificados(grupoId: Long): List<BalanceDTO> {
        val integrantes = integranteGrupoRepository.findAllByGrupoId(grupoId)

        // 1 - Calcular el neto de cada uno en este grupo
        val balancesNetos = obtenerBalancesNetos(integrantes, grupoId)

        val deudores = balancesNetos.filter { it.value < 0 }.toList().sortedBy { it.second }.toMutableList()
        val acreedores = balancesNetos.filter { it.value > 0 }.toList().sortedByDescending { it.second }.toMutableList()

        // 2 - Algoritmo de liquidación
        return simplificarDeudas(deudores, acreedores)
    }

    @Transactional
    fun realizarPagoPorGasto(auth: Authentication, pagoDTO: PagoDTO) {
        val usuarioLogueado = usuarioService.obtenerUsuarioLogueado(auth)
        val gastoId = pagoDTO.gastoId
        val fraccion = fraccionGastoRepository.findByDeudorIdAndGastoAsociadoId(usuarioLogueado.id!!, gastoId)
            ?: throw BusinessException(ErrorStrings.FRACCION_NO_ENCONTRADA)

        fraccion.pagar(pagoDTO.monto)

        fraccionGastoRepository.save(fraccion)
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

    private fun obtenerBalancesNetos(
        integrantes: List<Usuario>,
        grupoId: Long
    ): Map<Usuario, Double> {
        // AssociateWith crea un map con los valores de la
        // primera lista como keys y los de la segunda como values
        return integrantes.associateWith { integrante ->
            val loQueLeDeben =
                fraccionGastoRepository.findByAcreedorIdAndGastoAsociadoGrupoId(integrante.id!!, grupoId)
                    .sumOf { it.saldoPendiente() }
            val loQueDebe =
                fraccionGastoRepository.findByDeudorIdAndGastoAsociadoGrupoId(integrante.id!!, grupoId)
                    .sumOf { it.saldoPendiente() }
            Math.round((loQueLeDeben - loQueDebe) * 100) / 100.0
        }.filter { Math.abs(it.value) > 0.01 }.toMutableMap()
    }

    private fun simplificarDeudas(
        deudores: MutableList<Pair<Usuario, Double>>,
        acreedores: MutableList<Pair<Usuario, Double>>
    ): List<BalanceDTO> {
        val resultados = mutableListOf<BalanceDTO>()

        var dIdx = 0
        var aIdx = 0

        while (dIdx < deudores.size && aIdx < acreedores.size) {
            val (deudor, deudaTotal) = deudores[dIdx]
            val (acreedor, creditoTotal) = acreedores[aIdx]

            val montoATransferir = minOf(-deudaTotal, creditoTotal)

            resultados.add(BalanceDTO(deudor.id!!, deudor.nombre, acreedor.id!!, acreedor.nombre, montoATransferir))

            // Restamos lo transferido de los saldos temporales
            deudores[dIdx] = deudor to (deudores[dIdx].second + montoATransferir)
            acreedores[aIdx] = acreedor to (acreedores[aIdx].second - montoATransferir)

            // Si el deudor ya no debe nada (o casi nada por redondeo), pasamos al siguiente
            if (Math.abs(deudores[dIdx].second) < 0.01) dIdx++
            // Si al acreedor ya no le deben nada, pasamos al siguiente
            if (Math.abs(acreedores[aIdx].second) < 0.01) aIdx++
        }

        return resultados
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
        val grupoId = gasto.grupo.id!!
        val ids = gasto.participantes.mapNotNull { it.id }.toSet()

        val pertenecen = integranteGrupoRepository
            .findIdsQuePertenecen(grupoId, ids.toList())
            .toSet()

        val faltantes = ids - pertenecen

        if (faltantes.isNotEmpty()) {
            throw BusinessException("Usuarios no pertenecen al grupo: $faltantes")
        }

        return true
    }

    @Suppress("UNUSED_PARAMETER")
    private fun puedeCrearGasto(usuario: Usuario, grupo: Grupo): Boolean {
        return true
    }

    private fun resolverVistaFlags(vista: TipoRelacionVista?): VistaFlags = when (vista ?: TipoRelacionVista.TODOS) {
        TipoRelacionVista.TODOS -> VistaFlags(todos = true, deuda = false, amigos = false)
        TipoRelacionVista.DEUDA -> VistaFlags(todos = false, deuda = true, amigos = false)
        TipoRelacionVista.AMIGOS -> VistaFlags(todos = false, deuda = false, amigos = true)
    }

    private fun construirSort(orden: OrdenGasto?): Sort {
        return when (orden) {

            OrdenGasto.FECHA_ASC ->
                Sort.by(Sort.Direction.ASC, "fecha")

            OrdenGasto.FECHA_DESC ->
                Sort.by(Sort.Direction.DESC, "fecha")

            OrdenGasto.NOMBRE_ASC ->
                Sort.by(Sort.Direction.ASC, "descripcion")

            OrdenGasto.NOMBRE_DESC ->
                Sort.by(Sort.Direction.DESC, "descripcion")

            OrdenGasto.CREADOR_ASC ->
                Sort.by(Sort.Direction.ASC, "pagador.nombre")

            OrdenGasto.CREADOR_DESC ->
                Sort.by(Sort.Direction.DESC, "pagador.nombre")

            null ->
                Sort.by(Sort.Direction.DESC, "fecha") // default
        }
    }

    private fun existeIntegranteYGrupo(id: Long?, gasto: Gasto): Boolean =
        integranteGrupoRepository.existsByGrupoIdAndUsuarioId(id!!, gasto.pagador.id!!)

    private fun obtenerGrupoOLanzarExcepcion(grupoId: Long): Grupo =
        grupoRepository.findById(grupoId)
            .orElseThrow { BusinessException(ErrorStrings.GRUPO_NO_ENCONTRADO) }

    private fun obtenerUsuarioOLanzarExcepcion(usuarioId: Long): Usuario =
        usuarioRepository.findById(usuarioId)
            .orElseThrow { BusinessException(ErrorStrings.USUARIO_NO_ENCONTRADO) }

    private data class VistaFlags(
        val todos: Boolean,
        val deuda: Boolean,
        val amigos: Boolean,
    )
}
