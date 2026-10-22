package com.assessment.card_product_management.exchangerate;

import com.assessment.card_product_management.exchangerate.dto.CurrencyResponse;
import com.assessment.card_product_management.exchangerate.dto.ExchangeRateResponse;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;

@RestController
@Validated
@RequestMapping("/api/v1/exchange-rates")
public class ExchangeRateController {

    private final CurrencyCatalogueService currencyCatalogueService;
    private final ExchangeRateService exchangeRateService;

    public ExchangeRateController(
            CurrencyCatalogueService currencyCatalogueService,
            ExchangeRateService exchangeRateService
    ) {
        this.currencyCatalogueService = currencyCatalogueService;
        this.exchangeRateService = exchangeRateService;
    }

    @GetMapping("/currencies")
    public List<CurrencyResponse> getCurrencies() {
        return currencyCatalogueService.getCurrencies();
    }

    @GetMapping
    public ExchangeRateResponse getExchangeRate(
            @RequestParam("to")
            @Pattern(regexp = "^[A-Za-z]{3}$", message = "to must be a three-letter currency code")
            String to
    ) {
        return exchangeRateService.getExchangeRate(to.toUpperCase(Locale.ROOT));
    }
}
