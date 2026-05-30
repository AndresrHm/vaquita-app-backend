package ar.edu.unsam.proyectosoftware.controller

import ar.edu.unsam.proyectosoftware.dto.ConversionResult
import ar.edu.unsam.proyectosoftware.service.CurrencyService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

@RestController
@RequestMapping("/currency", "/api/v1/currency")
class CurrencyController(private val currencyService: CurrencyService) {

    @GetMapping("/convert")
    fun convert(
        @RequestParam from: String,
        @RequestParam to: String,
        @RequestParam amount: BigDecimal
    ): ConversionResult {
        return currencyService.convertCurrency(from, to, amount)
    }
}