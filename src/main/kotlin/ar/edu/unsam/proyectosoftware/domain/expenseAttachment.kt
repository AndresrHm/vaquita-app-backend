package ar.edu.unsam.proyectosoftware.domain

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "expense_attachment",
    indexes = [
        Index(name = "idx_attachment_expense", columnList = "expense_id"),
        Index(name = "idx_attachment_uploaded_by", columnList = "uploaded_by")
    ]
)
class ExpenseAttachment(

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "expense_id", nullable = false)
    val expense: Gasto,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploaded_by", nullable = false)
    val uploadedBy: Usuario,

    @Column(name = "storage_key", nullable = false)
    val storageKey: String,

    @Column(name = "storage_bucket", nullable = false)
    val storageBucket: String = "receipts",

    @Column(name = "original_filename", nullable = false)
    val originalFilename: String,

    @Column(name = "mime_type", nullable = false)
    val mimeType: String,

    @Column(name = "file_size_bytes", nullable = false)
    val fileSizeBytes: Long,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
)