package ar.edu.unsam.proyectosoftware.security

import ar.edu.unsam.proyectosoftware.domain.Usuario
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.Key
import java.util.Date

@Component
class JwtUtils {

    // La clave secreta debería venir de tu application.properties / application.yml
    @Value("\${jwt.secret:UnaClaveSecretaMuyLargaYSuperSeguraParaQueNoFalle1234567890}")
    private lateinit var jwtSecret: String

    // Tiempos de expiración (en milisegundos)
    private val ACCESS_TOKEN_VALIDITY = 15 * 60 * 1000L // 15 minutos
    private val REFRESH_TOKEN_VALIDITY = 7 * 24 * 60 * 60 * 1000L // 7 días

    // VAQUITA-296: Generar Access Token (Corta duración)
    fun generarAccessToken(usuario: Usuario): String {
        return crearToken(usuario.email, ACCESS_TOKEN_VALIDITY)
    }

    // VAQUITA-297: Generar Refresh Token (Larga duración)
    fun generarRefreshToken(usuario: Usuario): String {
        return crearToken(usuario.email, REFRESH_TOKEN_VALIDITY)
    }

    private fun crearToken(subject: String, validezMilisegundos: Long): String {
        val claims: Map<String, Any> = HashMap()
        val fechaCreacion = Date(System.currentTimeMillis())
        val fechaExpiracion = Date(System.currentTimeMillis() + validezMilisegundos)

        return Jwts.builder()
            .setClaims(claims)
            .setSubject(subject)
            .setIssuedAt(fechaCreacion)
            .setExpiration(fechaExpiracion)
            .signWith(obtenerFirmaKey(), SignatureAlgorithm.HS256)
            .compact()
    }

    // Extraer info del token
    fun obtenerEmailDeToken(token: String): String {
        return obtenerTodasLasClaims(token).subject
    }

    // Validación robusta
    fun esTokenValido(token: String): Boolean {
        return try {
            val expiracion = obtenerTodasLasClaims(token).expiration
            !expiracion.before(Date())
        } catch (e: Exception) {
            // Si el token está mal formado, expirado, o firmado con otra clave, cae acá
            false
        }
    }

    private fun obtenerTodasLasClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(obtenerFirmaKey() as javax.crypto.SecretKey)
            .build()
            .parseSignedClaims(token)
            .payload
    }

    private fun obtenerFirmaKey(): Key {
        val keyBytes = Decoders.BASE64.decode(jwtSecret)
        return Keys.hmacShaKeyFor(keyBytes)
    }
}