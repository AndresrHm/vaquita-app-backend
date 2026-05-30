package ar.edu.unsam.proyectosoftware

import java.lang.RuntimeException

class BusinessException(msg: String) : RuntimeException(msg)

class NotFoundException(msg: String) : RuntimeException(msg)

class AnalyticsProcessingException(msg: String, cause: Throwable? = null) : RuntimeException(msg, cause)
