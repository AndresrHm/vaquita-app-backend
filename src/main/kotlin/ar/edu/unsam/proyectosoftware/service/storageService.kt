package ar.edu.unsam.proyectosoftware.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import java.time.Duration
import java.util.UUID

@Service
class StorageService(

    @Value("\${storage.bucket}")
    private val bucket: String,

    private val presigner: S3Presigner

) {

    fun generateUploadUrl(
        expenseId: Long,
        filename: String,
        mimeType: String
    ): Pair<String, String> {

        val ext = filename.substringAfterLast(".")
        val key = "expenses/$expenseId/${UUID.randomUUID()}.$ext"

        val putObjectRequest = PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .contentType(mimeType)
            .build()

        val presignRequest = PutObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(15))
            .putObjectRequest(putObjectRequest)
            .build()

        val presignedRequest = presigner.presignPutObject(presignRequest)

        println("UPLOAD URL:")
        println(presignedRequest.url())

        return Pair(
            presignedRequest.url().toString(),
            key
        )
    }
}