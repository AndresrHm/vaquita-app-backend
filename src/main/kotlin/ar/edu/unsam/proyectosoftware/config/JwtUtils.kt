package ar.edu.unsam.proyectosoftware.config

import ar.edu.unsam.proyectosoftware.domain.Usuario
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.util.*

@Component
class JwtUtils {
    // La clave debe ser secreta y tener al menos 32 caracteres
    private val SECRET_KEY = Keys.hmacShaKeyFor("esta-es-una-clave-secreta-muy-larga-para-vaquita-2026".toByteArray())

    fun generarToken(usuario: Usuario): String {
        val ahora = Date()
        val expiracion = Date(ahora.time + 1000 * 60 * 60 * 10) // Expira en 10 horas

        return Jwts.builder()
            .subject(usuario.email)
            .claim("nombre", usuario.nombre)
            .claim("id", usuario.id)
            .issuedAt(ahora)
            .expiration(expiracion)
            .signWith(SECRET_KEY)
            .compact()
    }

    fun getEmailDesdeToken(token: String): String? {
        return Jwts.parser()
            .verifyWith(SECRET_KEY)
            .build()
            .parseSignedClaims(token)
            .payload
            .subject
    }
}