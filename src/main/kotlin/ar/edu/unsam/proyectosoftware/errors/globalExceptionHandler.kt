package ar.edu.unsam.proyectosoftware.errors

import ar.edu.unsam.proyectosoftware.BusinessException
import ar.edu.unsam.proyectosoftware.NotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

// ---------------------------------------------------------
// VAQUITA-241: Estandarizar formato de error JSON
// ---------------------------------------------------------
data class ErrorResponseDTO(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val status: Int,
    val error: String,
    val message: String?
)

// ---------------------------------------------------------
// VAQUITA-240: Manejo global de excepciones
// ---------------------------------------------------------
@RestControllerAdvice
class GlobalExceptionHandler {

    // Atrapa todos los throw BusinessException("...")
    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(ex: BusinessException): ResponseEntity<ErrorResponseDTO> {
        val errorResponse = ErrorResponseDTO(
            status = HttpStatus.BAD_REQUEST.value(),
            error = HttpStatus.BAD_REQUEST.reasonPhrase,
            message = ex.message // Manda exactamente el texto que pusiste en el throw
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    // Atrapa todos los throw NotFoundException("...")
    @ExceptionHandler(NotFoundException::class)
    fun handleNotFoundException(ex: NotFoundException): ResponseEntity<ErrorResponseDTO> {
        val errorResponse = ErrorResponseDTO(
            status = HttpStatus.NOT_FOUND.value(),
            error = HttpStatus.NOT_FOUND.reasonPhrase,
            message = ex.message
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ErrorResponseDTO> {

        // VAQUITA-242: Logging de errores (Un adelanto del próximo ticket)
        println("🚨 ERROR GRAVE NO CONTROLADO: ${ex.message}")
        ex.printStackTrace()

        val errorResponse = ErrorResponseDTO(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase,
            // 🔒 IMPORTANTE: Nunca mandamos el error real de código al frontend por seguridad.
            message = "Ocurrió un error inesperado en el servidor. Por favor, intentá más tarde."
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }
}