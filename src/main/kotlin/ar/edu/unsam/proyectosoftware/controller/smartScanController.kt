package ar.edu.unsam.proyectosoftware.controller

import ar.edu.unsam.proyectosoftware.dto.ReceiptItemDto
import ar.edu.unsam.proyectosoftware.dto.ReceiptScanResultDto
import ar.edu.unsam.proyectosoftware.service.SmartScanService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.util.Base64

@RestController
@RequestMapping("/api/smart-scan")
@CrossOrigin(origins = ["*"])
class SmartScanController(
    private val smartScanService: SmartScanService
) {

    // Endpoint de testing con imagen hardcoded
    // Funciona bien, está bueno para probar pegarle a la api de gemini
    @PostMapping("/test")
    fun testScan(): ResponseEntity<ReceiptScanResultDto> {
        val result = smartScanService.scanTestReceipt()
        return ResponseEntity.ok(result)
    }

    @PostMapping("/scan", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun scan(@RequestParam("image") image: MultipartFile): ResponseEntity<ReceiptScanResultDto> {
        val base64 = Base64.getEncoder().encodeToString(image.bytes)
        val mimeType = image.contentType ?: "image/jpeg"
        val result = smartScanService.scanReceipt(base64, mimeType)
        return ResponseEntity.ok(result)
    }

    // TODO: remover cuando Gemini esté estable
    @PostMapping("/scan/mock", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun scanMock(@RequestParam("image") image: MultipartFile): ResponseEntity<ReceiptScanResultDto> {
        val mock = ReceiptScanResultDto(
            title = "BAR RESTAURANTE CONTAMOS CONTIGO",
            totalAmount = 27.6,
            date = "2017-07-19",
            category = "food",
            items = listOf(
                ReceiptItemDto(name = "MENU DEL DIA", quantity = 1, price = 12.0),
                ReceiptItemDto(name = "VERDINAS", quantity = 1, price = 10.0),
                ReceiptItemDto(name = "PAN", quantity = 1, price = 0.7),
                ReceiptItemDto(name = "SIDRA", quantity = 1, price = 2.5),
                ReceiptItemDto(name = "CAFES", quantity = 2, price = 1.2),
            )
        )
        return ResponseEntity.ok(mock)
    }
}