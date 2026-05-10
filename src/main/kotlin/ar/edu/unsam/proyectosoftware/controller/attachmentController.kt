package ar.edu.unsam.proyectosoftware.controller

import ar.edu.unsam.proyectosoftware.dto.InitUploadRequest
import ar.edu.unsam.proyectosoftware.dto.InitUploadResponse
import ar.edu.unsam.proyectosoftware.service.StorageService
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/expenses")
class AttachmentController(
    private val storageService: StorageService
) {

    @PostMapping("/{expenseId}/attachments/init")
    fun initUpload(
        @PathVariable expenseId: Long,
        @RequestBody request: InitUploadRequest
    ): InitUploadResponse {

        val (url, key) = storageService.generateUploadUrl(
            expenseId = expenseId,
            filename = request.filename,
            mimeType = request.mimeType
        )

        return InitUploadResponse(
            uploadUrl = url,
            storageKey = key
        )
    }
}