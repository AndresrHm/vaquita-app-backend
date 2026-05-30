package ar.edu.unsam.proyectosoftware.controller

import ar.edu.unsam.proyectosoftware.BusinessException
import ar.edu.unsam.proyectosoftware.domain.Grupo
import ar.edu.unsam.proyectosoftware.dto.GrupoNombreIdDTO
import ar.edu.unsam.proyectosoftware.dto.UnirseGrupoDTO
import ar.edu.unsam.proyectosoftware.service.InvitacionGrupoService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/invitaciones")
class InvitacionGrupoController(
    private val invitacionGrupoService: InvitacionGrupoService
) {

    @PostMapping("/unirse")
    fun unirsePorInvitacion(
        @RequestBody dto: UnirseGrupoDTO,
        auth: Authentication?
    ): ResponseEntity<GrupoNombreIdDTO> {

        // 1. Validamos que el usuario esté autenticado (token JWT válido)
        val authentication = auth ?: throw BusinessException("No autorizado: Token no encontrado")

        // 2. Llamamos al servicio para validar el link, chequear vencimiento y agregar al usuario
        val grupo = invitacionGrupoService.unirsePorToken(dto.token, authentication)

        // 3. Convertimos la entidad Grupo a DTO para no exponer gastos, saldos ni información extra
        val respuesta = Grupo.toGrupoNombreIdDTO(grupo)

        // 4. Devolvemos un HTTP 200 OK con los datos básicos del grupo
        return ResponseEntity.ok(respuesta)
    }
}