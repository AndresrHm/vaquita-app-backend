package ar.edu.unsam.proyectosoftware.service

import ar.edu.unsam.proyectosoftware.BusinessException // <-- IMPORTANTE
import ar.edu.unsam.proyectosoftware.domain.Grupo
import ar.edu.unsam.proyectosoftware.domain.InvitacionGrupo
import ar.edu.unsam.proyectosoftware.domain.RolIntegrante
import ar.edu.unsam.proyectosoftware.repository.GrupoRepository
import ar.edu.unsam.proyectosoftware.repository.InvitacionGrupoRepository
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class InvitacionGrupoService(
    private val invitacionRepository: InvitacionGrupoRepository,
    private val usuarioService: UsuarioService,
    private val grupoRepository: GrupoRepository,
    private val actividadService: ActividadService // <-- Inyectamos esto para el historial
) {
    @Transactional
    fun crearTokenInvitacion(grupoId: Long, authentication: Authentication): String {
        val grupo = grupoRepository.findById(grupoId)
            .orElseThrow { BusinessException("Grupo no encontrado") }

        val invitacion = InvitacionGrupo(
            grupo = grupo,
            fechaExpiracion = LocalDateTime.now().plusHours(24) // Dura exactamente 1 día, perfecto.
        )

        invitacionRepository.save(invitacion)
        return invitacion.token
    }

    @Transactional
    fun unirsePorToken(token: String, authentication: Authentication): Grupo {
        val invitacion = invitacionRepository.findByToken(token)
            // Cambiamos Exception por BusinessException
            .orElseThrow { BusinessException("Invitación inválida o inexistente") }

        if (!invitacion.esValida()) {
            throw BusinessException("El código de invitación ha expirado.")
        }

        val usuarioLogueado = usuarioService.obtenerUsuarioLogueado(authentication)
        val grupo = invitacion.grupo

        // Validamos que no intente unirse si ya forma parte
        if (grupo.usuarioEsMiembro(usuarioLogueado)) {
            // Sería ideal usar tu ErrorStrings acá si lo tienen: ErrorStrings.USUARIO_YA_PERTENECE_AL_GRUPO
            throw BusinessException("Ya sos miembro de este grupo")
        }

        grupo.agregarIntegrante(usuarioLogueado, RolIntegrante.MIEMBRO)
        val grupoGuardado = grupoRepository.save(grupo)

        // 👇 REGISTRAMOS LA ACTIVIDAD PARA EL FRONTEND 👇
        actividadService.registrarIngresoGrupo(usuarioLogueado, grupoGuardado)

        return grupoGuardado
    }
}