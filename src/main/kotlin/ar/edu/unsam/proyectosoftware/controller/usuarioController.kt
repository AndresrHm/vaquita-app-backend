package ar.edu.unsam.proyectosoftware.controller

import ar.edu.unsam.proyectosoftware.domain.Usuario
import ar.edu.unsam.proyectosoftware.dto.UsuarioRegistradoDTO
import ar.edu.unsam.proyectosoftware.service.UsuarioService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class UsuarioController(private val usuarioService: UsuarioService) {

    @GetMapping("/{id}")
    fun obtenerUsuario(@PathVariable id: Long): ResponseEntity<Usuario> {
        val usuario = usuarioService.obtenerUsuario(id)
        return ResponseEntity.ok(usuario)
    }

    @GetMapping
    fun obtenerTodosLosUsuarios(): ResponseEntity<Iterable<Usuario>>{
        val usuarios = usuarioService.obtenerTodosLosUsuarios()
        return ResponseEntity.ok(usuarios)
    }

    @PostMapping("/register")
    fun registarUsuario(@RequestBody dto: UsuarioRegistradoDTO): ResponseEntity<Any> {
        usuarioService.registrarUsuario(dto)
        return ResponseEntity.ok("Usuario registrado con éxito")
    }
}