package ar.edu.unsam.proyectosoftware.controller

import ar.edu.unsam.proyectosoftware.domain.SolicitudAmistadFront
import ar.edu.unsam.proyectosoftware.domain.Usuario
import ar.edu.unsam.proyectosoftware.dto.UsuarioInfoDTO
import ar.edu.unsam.proyectosoftware.dto.UsuarioLoginDTO
import ar.edu.unsam.proyectosoftware.dto.UsuarioLogueadoDTO
import ar.edu.unsam.proyectosoftware.dto.UsuarioNombreIdDTO
import ar.edu.unsam.proyectosoftware.dto.UsuarioRegistradoDTO
import ar.edu.unsam.proyectosoftware.service.UsuarioService
import ar.edu.unsam.proyectosoftware.mapper.UsuarioMapper
import ar.edu.unsam.proyectosoftware.service.GrupoService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = ["http://localhost:5173"])
class UsuarioController(
    private val usuarioService: UsuarioService,
    private val grupoService: GrupoService
) {

    @GetMapping("/{id}")
    fun obtenerUsuario(@PathVariable id: Long): ResponseEntity<Usuario> {
        val usuario = usuarioService.obtenerUsuario(id)
        return ResponseEntity.ok(usuario)
    }

    @GetMapping("/{id}/info/{idBuscado}")
    fun obtenerUsuarioInfo(@PathVariable id: Long, @PathVariable idBuscado: Long): ResponseEntity<UsuarioInfoDTO> {
        val usuario = usuarioService.obtenerUsuario(id)
        val usuarioBuscado = usuarioService.obtenerUsuario(idBuscado)
        val gruposCompartidos = grupoService.obtenerGruposCompartidos(usuario.id!!, usuarioBuscado.id!!)
        val usuarioBuscadoInfo = UsuarioMapper.toInfoDTO(usuarioBuscado, usuario, gruposCompartidos)
        return ResponseEntity.ok(usuarioBuscadoInfo)
    }

    @GetMapping
    fun obtenerTodosLosUsuarios(): ResponseEntity<Iterable<Usuario>> {
        val usuarios = usuarioService.obtenerTodosLosUsuarios()
        return ResponseEntity.ok(usuarios)
    }

    @GetMapping("/grupo/{grupoId}")
    fun obtenerUsuariosPorGrupo(@PathVariable grupoId: Long): ResponseEntity<Iterable<UsuarioNombreIdDTO>> {
        val usuarios = usuarioService.obtenerUsuariosPorGrupo(grupoId)
        val response = usuarios.map { Usuario.toUsuarioNombreIdDTO(it) }
        return ResponseEntity.ok(response)
    }

    @GetMapping("/amigos/{id}")
    fun obtenerUsuarioAmigos(@PathVariable id: Long): ResponseEntity<Iterable<UsuarioNombreIdDTO>> {
        val amigos = usuarioService.obtenerAmigos(id)
        return ResponseEntity.ok(amigos)
    }


    @PostMapping("/register")
    fun registrarUsuario(@RequestBody dto: UsuarioRegistradoDTO): ResponseEntity<Usuario> {
        val usuarioCreado = usuarioService.registrarUsuario(dto)
        return ResponseEntity.ok(usuarioCreado) // Devolvemos el objeto entero, no un texto
    }

    @PostMapping("/login")
    fun loginUser(@RequestBody dataUser: UsuarioLoginDTO): ResponseEntity<UsuarioLogueadoDTO> {
        val respuesta = usuarioService.login(dataUser)
        return ResponseEntity.ok(respuesta)
    }

    @GetMapping("/{idUsuario}/solicitudAmistad")
    fun obtenerSolicitudesPendientes(@PathVariable idUsuario: Long): ResponseEntity<List<SolicitudAmistadFront>> {
        val solicitudes = usuarioService.obtenerSolicitudesPendientes(idUsuario)
        return ResponseEntity.ok(solicitudes)
    }

    @PostMapping("/{idEmisor}/solicitudAmistad/{idReceptor}")
    fun enviarSolicitudAmistad(
        @PathVariable idEmisor: Long,
        @PathVariable idReceptor: Long
    ): ResponseEntity<Long> {
        val response = usuarioService.enviarSolicitudAmistad(idEmisor,idReceptor)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{idUsuario}/amigos/{idAmigo}")
    fun eliminarAmigo(
        @PathVariable idUsuario: Long,
        @PathVariable idAmigo: Long
    ): ResponseEntity<Void> {
        usuarioService.eliminarAmigo(idUsuario, idAmigo)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/{idUsuario}/solicitudAmistad/{idSolicitud}")
    fun responderSolicitudAmistad(
        @PathVariable idUsuario: Long,
        @PathVariable idSolicitud: Long,
        @RequestParam aceptar: Boolean
    ): ResponseEntity<Long> {
        val result = usuarioService.responderSolicitudAmistad(idUsuario, idSolicitud, aceptar)
        return ResponseEntity.ok(result)
    }
}