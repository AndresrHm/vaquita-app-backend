package ar.edu.unsam.proyectosoftware.dto

import org.springframework.data.domain.Page

// Con este DTO y este extension method podemos mapear Page<T> a PageResponse de forma sencilla

data class PageResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean
)

fun <T, R> Page<T>.toPageResponse(mapper: (T) -> R): PageResponse<R> {
    return PageResponse(
        content = content.map(mapper),
        page = number,
        size = size,
        totalElements = totalElements,
        totalPages = totalPages,
        hasNext = hasNext()
    )
}