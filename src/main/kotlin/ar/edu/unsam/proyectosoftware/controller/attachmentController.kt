package ar.edu.unsam.proyectosoftware.controller

import ar.edu.unsam.proyectosoftware.dto.AttachmentResponse
import ar.edu.unsam.proyectosoftware.dto.ConfirmUploadRequest
import ar.edu.unsam.proyectosoftware.dto.ConfirmUploadResponse
import ar.edu.unsam.proyectosoftware.dto.InitUploadRequest
import ar.edu.unsam.proyectosoftware.dto.InitUploadResponse
import ar.edu.unsam.proyectosoftware.service.AttachmentService
import ar.edu.unsam.proyectosoftware.service.StorageService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/expenses")
class AttachmentController(
    private val storageService: StorageService,
    private val attachmentService: AttachmentService,
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


    @PostMapping("/{expenseId}/attachments/confirm")
    fun confirmUpload(
        @PathVariable expenseId: Long,
        @RequestBody request: ConfirmUploadRequest
    ): ConfirmUploadResponse {

//        if (!storageService.exists(request.storageKey)) {
//            throw RuntimeException("El archivo no existe en storage")
//        }

        val attachment = attachmentService.confirmUpload(
            expenseId = expenseId,
            request = request
        )

        return ConfirmUploadResponse(
            id = attachment.id,
            storageKey = attachment.storageKey,
            filename = attachment.originalFilename,
            mimeType = attachment.mimeType,
            fileSizeBytes = attachment.fileSizeBytes,
            createdAt = attachment.createdAt
        )
    }

    @GetMapping("/{expenseId}/attachments")
    fun getAttachments(
        @PathVariable expenseId: Long
    ): List<AttachmentResponse> {

        return attachmentService
            .findByExpense(expenseId)
            .map {
                AttachmentResponse(
                    id = it.id,
                    filename = it.originalFilename,
                    mimeType = it.mimeType,
                    fileSizeBytes = it.fileSizeBytes,
                    downloadUrl = storageService.generateDownloadUrl(it.storageKey),
                    createdAt = it.createdAt
                )
            }
    }
}