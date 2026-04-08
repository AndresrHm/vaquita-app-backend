package ar.edu.unsam.proyectosoftware

import ar.edu.unsam.proyectosoftware.domain.Gasto
import ar.edu.unsam.proyectosoftware.domain.Grupo
import ar.edu.unsam.proyectosoftware.domain.Usuario
import ar.edu.unsam.proyectosoftware.dto.GastoDTO
import ar.edu.unsam.proyectosoftware.repository.GastoRepository
import ar.edu.unsam.proyectosoftware.errors.ErrorStrings
import ar.edu.unsam.proyectosoftware.repository.GrupoRepository
import ar.edu.unsam.proyectosoftware.repository.UsuarioRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GastoService(
    @Autowired
    val gastoRepository: GastoRepository,
    @Autowired
    val grupoRepository: GrupoRepository,
    @Autowired
    val usuarioRepository: UsuarioRepository,
) {
    @Transactional(readOnly = false, rollbackFor = [Exception::class])
    fun crearGasto(
        gastoDTO: GastoDTO
    ): Gasto {
        val grupo = grupoRepository.findById(gastoDTO.grupoID)
            .orElseThrow { BusinessException(ErrorStrings.GRUPO_NO_ENCONTRADO) }
        val pagador = usuarioRepository.findById(gastoDTO.pagadorID)
            .orElseThrow { BusinessException(ErrorStrings.USUARIO_NO_ENCONTRADO) }
        val participantes = usuarioRepository.findAllById(gastoDTO.participantesIDs).toList()
        if (participantes.size != gastoDTO.participantesIDs.size) {
            throw BusinessException(ErrorStrings.NINGUN_USUARIO_ENCONTRADO)
        }

        val gasto = Gasto.fromGastoDTO(gastoDTO, grupo, pagador, participantes)

        validarDatosBasicos(gasto)

        if (!gasto.pagador.perteneceA(gasto.grupo)) {
            throw BusinessException(ErrorStrings.PAGADOR_NO_PERTENECE_AL_GRUPO)
        }

        if (!puedeCrearGasto(gasto.pagador, gasto.grupo)) {
            throw BusinessException(ErrorStrings.USUARIO_SIN_PERMISO_GASTO)
        }

        if (!participantesContienePagador(participantes, pagador)) {
            throw BusinessException(ErrorStrings.USUARIO_SIN_PERMISO_GASTO)
        }
/* Próximamente
        if (!participantesPertenecenAlGrupo(participantes, grupo)) {
            throw BusinessException(ErrorStrings.PARTICIPANTES_NO_PERTENECEN_AL_GRUPO)
        }
*/
        //gasto.grupo.registrarGasto(gasto) // Agrega saldo en la db para x grupo

        gastoRepository.save(gasto)

        return gasto
    }

    private fun participantesContienePagador(
        participantes: List<Usuario>,
        pagador: Usuario
    ): Boolean {
        return participantes.contains(pagador)
    }
/*
    private fun participantesPertenecenAlGrupo(
        participantes: List<Usuario>,
        grupo: Grupo
    ): Boolean {
        return participantes.all{ participante -> participante.grupo == grupo}
    }
*/
    private fun validarDatosBasicos(gasto: Gasto) {
        if (gasto.monto <= 0) {
            throw BusinessException(ErrorStrings.MONTO_NEGATIVO)
        }
        if (gasto.descripcion.isBlank()) {
            throw BusinessException(ErrorStrings.DESCRIPCION_VACIA)
        }
    }

    private fun puedeCrearGasto(usuario: Usuario, grupo: Grupo): Boolean {
        // Por ahora devolvemos true, pero acá iría la lógica de roles (administrador)
        return true
    }
}

