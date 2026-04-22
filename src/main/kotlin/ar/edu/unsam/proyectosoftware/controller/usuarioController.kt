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
import org.springframework.security.core.Authentication
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
@RequestMapping("/usuarios")
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
    
      @GetMapping("/me")
    fun obtenerMiPerfil(auth: Authentication?): ResponseEntity<UsuarioNombreIdDTO> {
        val authentication = auth ?: throw Exception("No se encontró el token o el usuario no está autenticado")
        val usuario = usuarioService.obtenerUsuarioLogueado(authentication)
        val dto = Usuario.toUsuarioNombreIdDTO(usuario)
        return ResponseEntity.ok(dto)
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

    @GetMapping("/me/amigos")
    fun obtenerMisAmigos(auth: Authentication?): ResponseEntity<List<UsuarioNombreIdDTO>> {
        val authentication = auth ?: throw Exception("No se encontró el token o el usuario no está autenticado")
        val amigos = usuarioService.obtenerAmigos(authentication)
        return ResponseEntity.ok(amigos)
    }

    @PostMapping("/register")
    fun registrarUsuario(@RequestBody dto: UsuarioRegistradoDTO): ResponseEntity<Usuario> {
        val usuarioCreado = usuarioService.registrarUsuario(dto)
        return ResponseEntity.ok(usuarioCreado)
    }

    @PostMapping("/login")
    fun loginUser(@RequestBody dataUser: UsuarioLoginDTO): ResponseEntity<UsuarioLogueadoDTO> {
        val respuesta = usuarioService.login(dataUser)
        return ResponseEntity.ok(respuesta)
    }

    // 1. Obtener solicitudes propias
    @GetMapping("/me/solicitudes")
    fun obtenerSolicitudesPendientes(auth: Authentication?): ResponseEntity<List<SolicitudAmistadFront>> {
        val authentication = auth ?: throw Exception("No autorizado: Token no encontrado")
        val solicitudes = usuarioService.obtenerSolicitudesPendientes(authentication)
        return ResponseEntity.ok(solicitudes)
    }

    // 2. Enviar solicitud
    @PostMapping("/me/invitar/{idReceptor}")
    fun enviarSolicitudAmistad(
        auth: Authentication?,
        @PathVariable idReceptor: Long
    ): ResponseEntity<Long> {
        val authentication = auth ?: throw Exception("No autorizado: Token no encontrado")
        val response = usuarioService.enviarSolicitudAmistad(authentication, idReceptor)
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

    @PatchMapping("/me/solicitud/{idSolicitud}")
    fun responderSolicitudAmistad(
        auth: Authentication?,
        @PathVariable idSolicitud: Long,
        @RequestParam aceptar: Boolean
    ): ResponseEntity<Long> {
        val authentication = auth ?: throw Exception("No autorizado: Token no encontrado")
        val result = usuarioService.responderSolicitudAmistad(authentication, idSolicitud, aceptar)
        return ResponseEntity.ok(result)
    }
}