package ar.edu.unsam.proyectosoftware.controller

import ar.edu.unsam.proyectosoftware.dto.ReceiptScanResultDto
import ar.edu.unsam.proyectosoftware.service.SmartScanService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/smart-scan")
class SmartScanController(
    private val smartScanService: SmartScanService
) {

    @PostMapping("/test")
    fun testScan(): ResponseEntity<ReceiptScanResultDto> {
        val result = smartScanService.scanTestReceipt()
        return ResponseEntity.ok(result)
    }
}