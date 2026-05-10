package ar.edu.unsam.proyectosoftware.service

import ar.edu.unsam.proyectosoftware.domain.ExpenseAttachment
import ar.edu.unsam.proyectosoftware.domain.Gasto
import ar.edu.unsam.proyectosoftware.domain.Usuario
import ar.edu.unsam.proyectosoftware.dto.AttachmentResponse
import ar.edu.unsam.proyectosoftware.dto.ConfirmUploadRequest
import ar.edu.unsam.proyectosoftware.repository.ExpenseAttachmentRepository
import ar.edu.unsam.proyectosoftware.repository.GastoRepository
import ar.edu.unsam.proyectosoftware.repository.UsuarioRepository
import org.springframework.stereotype.Service

@Service
class AttachmentService(
    private val attachmentRepository: ExpenseAttachmentRepository,
    private val gastoRepository: GastoRepository,
    private val usuarioRepository: UsuarioRepository,
    private val storageService: StorageService,
) {
    fun confirmUpload(
        expenseId: Long,
        request: ConfirmUploadRequest
    ): ExpenseAttachment {

        // TODO:
        // sacar usuario autenticado desde Spring Security
        // por ahora hardcodeamos uno

        val usuario: Usuario = usuarioRepository.findById(1)
            .orElseThrow()

        val gasto: Gasto = gastoRepository.findById(expenseId)
            .orElseThrow()

        val attachment = ExpenseAttachment(
            expense = gasto,
            uploadedBy = usuario,
            storageKey = request.storageKey,
            originalFilename = request.filename,
            mimeType = request.mimeType,
            fileSizeBytes = request.fileSizeBytes
        )

        return attachmentRepository.save(attachment)
    }

    // get
    fun findByExpense(expenseId: Long): List<ExpenseAttachment> {
        return attachmentRepository.findAllByExpenseId(expenseId)
    }
}