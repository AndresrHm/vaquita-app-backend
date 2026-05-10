package ar.edu.unsam.proyectosoftware.dto

data class InitUploadRequest(
    val filename: String,
    val mimeType: String,
    val fileSizeBytes: Long
)

data class InitUploadResponse(
    val uploadUrl: String,
    val storageKey: String
)