package ar.edu.unsam.proyectosoftware.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(private val jwtUtils: JwtUtils) : OncePerRequestFilter() {

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        val authHeader = request.getHeader("Authorization")

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            val token = authHeader.substring(7)
            try {
                if (jwtUtils.esTokenValido(token)) {

                    val email = jwtUtils.obtenerEmailDeToken(token)

                    if (email != null && SecurityContextHolder.getContext().authentication == null) {
                        val authToken = UsernamePasswordAuthenticationToken(email, null, mutableListOf())

                        authToken.details = org.springframework.security.web.authentication.WebAuthenticationDetailsSource().buildDetails(request)

                        SecurityContextHolder.getContext().authentication = authToken
                    }
                } else {
                    println("El token provisto es inválido o ya expiró.")
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido o expirado")
                    return // Corta la ejecución para que no llegue al Controller
                }
            } catch (e: Exception) {
                println("Error procesando token: ${e.message}")
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error al procesar el token")
                return
            }
        }
        filterChain.doFilter(request, response)
    }
}