package ar.edu.unsam.proyectosoftware.service

import ar.edu.unsam.proyectosoftware.BusinessException
import ar.edu.unsam.proyectosoftware.domain.Gasto
import ar.edu.unsam.proyectosoftware.domain.FraccionGasto
import ar.edu.unsam.proyectosoftware.domain.Grupo
import ar.edu.unsam.proyectosoftware.domain.Usuario
import ar.edu.unsam.proyectosoftware.dto.BalanceDTO
import ar.edu.unsam.proyectosoftware.dto.BalanceConGrupoDTO
import ar.edu.unsam.proyectosoftware.dto.PagoDTO
import ar.edu.unsam.proyectosoftware.dto.TipoActividad
import ar.edu.unsam.proyectosoftware.dto.CreacionGastoDTO
import ar.edu.unsam.proyectosoftware.dto.FilterGastosCustom
import ar.edu.unsam.proyectosoftware.dto.OrdenGasto
import ar.edu.unsam.proyectosoftware.dto.GastoDetalleDTO
import ar.edu.unsam.proyectosoftware.dto.GastoDetalleModalDTO
import ar.edu.unsam.proyectosoftware.dto.PageResponse
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
    var usuarioService: UsuarioService,
    var actividadService: ActividadService,
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

    @Transactional(readOnly = true)
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

        return gastosPage.toPageResponse { GastoMapper.toViewDTO(it) }
    }

    @Transactional(readOnly = true)
    fun obtenerListaItems(auth: Authentication, gastoId: Long): GastoDetalleModalDTO {
        val usuarioLogueado = usuarioService.obtenerUsuarioLogueado(auth)

        val gasto = gastoRepository.findDetalleById(gastoId)
            ?: throw BusinessException(ErrorStrings.GASTO_NO_ENCONTRADO)

        if (!integranteGrupoRepository.esMiembro(gasto.grupo.id!!, usuarioLogueado.id!!)) {
            throw BusinessException(ErrorStrings.USUARIO_NO_PERTENECE_AL_GRUPO)
        }

        return GastoMapper.toGastoDetalleItemDTO(gasto)
    }

    @Transactional(readOnly = true)
    fun obtenerBalancesEntreUsuarios(usuarioId: Long, otroUsuarioId: Long): List<BalanceConGrupoDTO> {
        val fracciones = (
            fraccionGastoRepository.findGastosPendientesBetween(usuarioId, otroUsuarioId) +
            fraccionGastoRepository.findGastosPendientesBetween(otroUsuarioId, usuarioId)
        ).filter { it.saldoPendiente() > 0.01 }

        val resultados = mutableListOf<BalanceConGrupoDTO>()

        fracciones.groupBy { it.gastoAsociado.grupo.id!! }.forEach { (grupoId, fraccionesGrupo) ->
            val grupoNombre = fraccionesGrupo.first().gastoAsociado.grupo.nombre
            val balancesCruzados = mutableMapOf<Pair<Long, Long>, Double>()
            val nombres = mutableMapOf<Long, String>()

            fraccionesGrupo.forEach { fracc ->
                val clave = fracc.deudor.id!! to fracc.acreedor.id!!
                balancesCruzados[clave] = balancesCruzados.getOrDefault(clave, 0.0) + fracc.saldoPendiente()
                nombres[fracc.deudor.id!!] = fracc.deudor.nombre
                nombres[fracc.acreedor.id!!] = fracc.acreedor.nombre
            }

            val procesados = mutableSetOf<Pair<Long, Long>>()

            balancesCruzados.keys.forEach { (u1, u2) ->
                if (Pair(u1, u2) in procesados || Pair(u2, u1) in procesados) return@forEach

                val deuda1a2 = balancesCruzados.getOrDefault(u1 to u2, 0.0)
                val deuda2a1 = balancesCruzados.getOrDefault(u2 to u1, 0.0)

                if (deuda1a2 > deuda2a1) {
                    resultados.add(BalanceConGrupoDTO(u1, nombres[u1]!!, u2, nombres[u2]!!, Math.round((deuda1a2 - deuda2a1) * 100) / 100.0, grupoId, grupoNombre))
                } else if (deuda2a1 > deuda1a2) {
                    resultados.add(BalanceConGrupoDTO(u2, nombres[u2]!!, u1, nombres[u1]!!, Math.round((deuda2a1 - deuda1a2) * 100) / 100.0, grupoId, grupoNombre))
                }

                procesados.add(u1 to u2)
            }
        }

        return resultados.filter { it.monto > 0.01 }
    }

    @Transactional(readOnly = true)
    fun obtenerBalancesGrupo(grupoId: Long): List<BalanceDTO> {
        val fracciones = fraccionGastoRepository.findAllByGastoAsociadoGrupoId(grupoId)
            .filter { it.saldoPendiente() > 0.01 }

        // Agrupamos por (Deudor, Acreedor) para tener el total bruto entre cada par
        val balancesCruzados = mutableMapOf<Pair<Long, Long>, Double>()
        val nombres = mutableMapOf<Long, String>()

        fracciones.forEach { fracc ->
            val clave = fracc.deudor.id!! to fracc.acreedor.id!!
            balancesCruzados[clave] = balancesCruzados.getOrDefault(clave, 0.0) + fracc.saldoPendiente()
            nombres[fracc.deudor.id!!] = fracc.deudor.nombre
            nombres[fracc.acreedor.id!!] = fracc.acreedor.nombre
        }

        val resultados = mutableListOf<BalanceDTO>()
        val procesados = mutableSetOf<Pair<Long, Long>>()

        // Simplificamos deudas bilaterales: Si A debe a B y B debe a A, restamos.
        balancesCruzados.keys.forEach { (u1, u2) ->
            if (Pair(u1, u2) in procesados || Pair(u2, u1) in procesados) return@forEach

            val deuda1a2 = balancesCruzados.getOrDefault(u1 to u2, 0.0)
            val deuda2a1 = balancesCruzados.getOrDefault(u2 to u1, 0.0)

            if (deuda1a2 > deuda2a1) {
                resultados.add(BalanceDTO(u1, nombres[u1]!!, u2, nombres[u2]!!, Math.round((deuda1a2 - deuda2a1) * 100) / 100.0))
            } else if (deuda2a1 > deuda1a2) {
                resultados.add(BalanceDTO(u2, nombres[u2]!!, u1, nombres[u1]!!, Math.round((deuda2a1 - deuda1a2) * 100) / 100.0))
            }

            procesados.add(u1 to u2)
        }

        return resultados.filter { it.monto > 0.01 }
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

    @Transactional
    fun realizarPagoGlobalAUsuario(auth: Authentication, acreedorId: Long, montoEnviado: Double) {
        procesarPagoSimplificado(auth, null, acreedorId, montoEnviado)
    }

    @Transactional
    fun realizarPagoGrupoAUsuario(auth: Authentication, grupoId: Long, acreedorId: Long, montoEnviado: Double) {
        procesarPagoSimplificado(auth, grupoId, acreedorId, montoEnviado)
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

    private fun procesarPagoSimplificado(auth: Authentication, grupoId: Long?, acreedorId: Long, montoEnviado: Double) {
        val deudorId = usuarioService.obtenerUsuarioLogueado(auth).id!!

        // 1. Buscamos deudas en ambas direcciones (Filtrado por grupo opcionalmente)
        val deudasConmigo = fraccionGastoRepository.findFraccionesPendientesEntre(acreedorId, deudorId, grupoId)
        val misDeudasConEl = fraccionGastoRepository.findFraccionesPendientesEntre(deudorId, acreedorId, grupoId)

        // 2. Compensación: Saldamos lo que él me debía (crédito contable)
        val creditoCompensacion = deudasConmigo.sumOf { it.saldoPendiente() }
        deudasConmigo.forEach { it.pagar(it.saldoPendiente()) }

        // 3. Pago Real: Aplicamos (Crédito + Efectivo) a mis deudas con él
        aplicarPagoAFracciones(misDeudasConEl, creditoCompensacion + montoEnviado)

        // 4. Persistencia
        fraccionGastoRepository.saveAll(deudasConmigo + misDeudasConEl)
    }

    private fun aplicarPagoAFracciones(fracciones: List<FraccionGasto>, monto: Double): Double {
        var restante = monto
        for (f in fracciones) {
            if (restante <= 0.0) break
            val saldo = f.saldoPendiente()
            val pago = minOf(restante, saldo)
            f.pagar(pago)
            restante -= pago
        }
        return restante
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

    private fun registrarActividadesDeGasto(gasto: Gasto, fracciones: List<ar.edu.unsam.proyectosoftware.domain.FraccionGasto>) {
        actividadService.registrarActividad(
            usuario = gasto.pagador,
            tipo = TipoActividad.DEUDA_GENERADA,
            descripcion = "Registraste un gasto en ${gasto.grupo.nombre}.",
            metadata = mapOf(
                "gastoId" to gasto.id,
                "grupoId" to gasto.grupo.id,
                "grupoNombre" to gasto.grupo.nombre,
                "montoTotal" to gasto.monto,
                "deudores" to fracciones.map { it.deudor.id },
            ),
        )

        fracciones.forEach { fraccion ->
            actividadService.registrarActividad(
                usuario = fraccion.deudor,
                tipo = TipoActividad.GASTO_DEUDA,
                descripcion = "Se registro una deuda en ${gasto.grupo.nombre}.",
                metadata = mapOf(
                    "gastoId" to gasto.id,
                    "grupoId" to gasto.grupo.id,
                    "grupoNombre" to gasto.grupo.nombre,
                    "acreedorId" to fraccion.acreedor.id,
                    "acreedorNombre" to fraccion.acreedor.nombre,
                    "montoAdeudado" to fraccion.montoFraccion,
                ),
            )
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
