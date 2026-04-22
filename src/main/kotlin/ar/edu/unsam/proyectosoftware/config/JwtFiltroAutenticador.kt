package ar.edu.unsam.proyectosoftware.config

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
                val email = jwtUtils.getEmailDesdeToken(token)
                if (email != null && SecurityContextHolder.getContext().authentication == null) {
                    val authToken = UsernamePasswordAuthenticationToken(email, null, mutableListOf())

                    // ESTA LÍNEA ES NUEVA: Agrega los detalles de la petición (IP, etc)
                    authToken.details = org.springframework.security.web.authentication.WebAuthenticationDetailsSource().buildDetails(request)

                    SecurityContextHolder.getContext().authentication = authToken
                }
            } catch (e: Exception) {
                println("Token inválido: ${e.message}")
            }
        }
        filterChain.doFilter(request, response)
    }
}
