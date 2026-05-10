package ar.edu.unsam.proyectosoftware.repository

import ar.edu.unsam.proyectosoftware.domain.ExpenseAttachment
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ExpenseAttachmentRepository :
    JpaRepository<ExpenseAttachment, UUID> {

    fun findAllByExpenseId(expenseId: Long): List<ExpenseAttachment>
}