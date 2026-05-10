package ar.edu.unsam.proyectosoftware.security

import ar.edu.unsam.proyectosoftware.domain.Usuario
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.util.ReflectionTestUtils
import java.util.*

class JwtUtilsTest {

    private lateinit var jwtUtils: JwtUtils
    private val secretBase64 = "TWV0ZVN1Y2hlQ2xhdmVTVVBFUlNFR1VSQTEyMzQ1Njc4OTA=" // Una clave en Base64 para el test

    @BeforeEach
    fun setup() {
        jwtUtils = JwtUtils()
        // Inyectamos manualmente el secreto porque no estamos levantando Spring completo
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", secretBase64)
    }

    @Test
    fun `generarAccessToken - crea un token que es valido y contiene el email correcto`() {
        val usuario = Usuario(nombre = "Giuli", email = "giuli@mail.com", contrasena = "123")

        val token = jwtUtils.generarAccessToken(usuario)

        assertThat(token).isNotBlank()
        assertThat(jwtUtils.esTokenValido(token)).isTrue()
        assertThat(jwtUtils.obtenerEmailDeToken(token)).isEqualTo("giuli@mail.com")
    }

    @Test
    fun `esTokenValido - devuelve false si el token esta mal formado o es trucho`() {
        val tokenTrucho = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.un_token_inventado"

        val esValido = jwtUtils.esTokenValido(tokenTrucho)

        assertThat(esValido).isFalse()
    }

    @Test
    fun `esTokenValido - detecta cuando un token ya expiro`() {
        // Para este test, fabricamos un token que ya nacio vencido (de hace una hora)
        val key = Keys.hmacShaKeyFor(io.jsonwebtoken.io.Decoders.BASE64.decode(secretBase64))
        val tokenExpirado = Jwts.builder()
            .setSubject("giuli@mail.com")
            .setIssuedAt(Date(System.currentTimeMillis() - 7200000)) // Hace 2 horas
            .setExpiration(Date(System.currentTimeMillis() - 3600000)) // Murio hace 1 hora
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()

        val esValido = jwtUtils.esTokenValido(tokenExpirado)

        assertThat(esValido).isFalse() // Deberia decir que no es valido
    }

    @Test
    fun `generarRefreshToken - crea un token distinto al de acceso`() {
        val usuario = Usuario(nombre = "Giuli", email = "giuli@mail.com", contrasena = "123")

        val access = jwtUtils.generarAccessToken(usuario)
        val refresh = jwtUtils.generarRefreshToken(usuario)

        assertThat(access).isNotEqualTo(refresh)
        assertThat(jwtUtils.esTokenValido(refresh)).isTrue()
    }
}