package ar.edu.unsam.proyectosoftware.dto
import java.time.Instant
import java.util.UUID

// init
data class InitUploadRequest(
    val filename: String,
    val mimeType: String,
    val fileSizeBytes: Long
)

data class InitUploadResponse(
    val uploadUrl: String,
    val storageKey: String
)

// confirm
data class ConfirmUploadRequest(
    val storageKey: String,
    val filename: String,
    val mimeType: String,
    val fileSizeBytes: Long
)

data class ConfirmUploadResponse(
    val id: UUID,
    val storageKey: String,
    val filename: String,
    val mimeType: String,
    val fileSizeBytes: Long,
    val createdAt: Instant
)

// get
data class AttachmentResponse(
    val id: UUID,
    val filename: String,
    val mimeType: String,
    val fileSizeBytes: Long,
    val downloadUrl: String,
    val createdAt: Instant
)