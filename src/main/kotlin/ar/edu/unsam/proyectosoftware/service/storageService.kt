package ar.edu.unsam.proyectosoftware.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import java.time.Duration
import java.util.UUID

@Service
class StorageService(

    @Value("\${storage.bucket}")
    private val bucket: String,

    private val presigner: S3Presigner,
    private val s3Client: S3Client
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

    fun exists(storageKey: String): Boolean {
        return try {

            s3Client.headObject {
                it.bucket(bucket)
                it.key(storageKey)
            }

            true

        } catch (e: Exception) {
            false
        }
    }

    fun generateDownloadUrl(storageKey: String): String {

        val request = GetObjectRequest.builder()
            .bucket(bucket)
            .key(storageKey)
            .build()

        val presignRequest = GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(15))
            .getObjectRequest(request)
            .build()

        return presigner
            .presignGetObject(presignRequest)
            .url()
            .toString()
    }
}