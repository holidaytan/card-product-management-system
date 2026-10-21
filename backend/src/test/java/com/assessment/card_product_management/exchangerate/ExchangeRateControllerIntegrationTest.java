package com.assessment.card_product_management.exchangerate;

import com.assessment.card_product_management.config.SecurityConfig;
import com.assessment.card_product_management.error.ApiExceptionHandler;
import com.assessment.card_product_management.exchangerate.dto.CurrencyResponse;
import com.assessment.card_product_management.exchangerate.dto.ExchangeRateResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ExchangeRateController.class)
@Import({SecurityConfig.class, ApiExceptionHandler.class, ExchangeRateControllerIntegrationTest.TestConfig.class})
class ExchangeRateControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CurrencyCatalogueService currencyCatalogueService;

    @Autowired
    private ExchangeRateService exchangeRateService;

    @Test
    void returnsNormalizedCurrencies() throws Exception {
        when(currencyCatalogueService.getCurrencies()).thenReturn(List.of(
                new CurrencyResponse("MYR", "Malaysian Ringgit", "RM"),
                new CurrencyResponse("XDR", "Special Drawing Rights", null)
        ));

        mockMvc.perform(get("/api/v1/exchange-rates/currencies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("MYR"))
                .andExpect(jsonPath("$[1].symbol").value(nullValue()));
    }

    @Test
    void returnsRateForUppercasedTargetCurrency() throws Exception {
        when(exchangeRateService.getExchangeRate("USD")).thenReturn(
                new ExchangeRateResponse("MYR", "USD", new BigDecimal("0.2365"), LocalDate.of(2026, 10, 21))
        );

        mockMvc.perform(get("/api/v1/exchange-rates?to=usd"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.baseCurrency").value("MYR"))
                .andExpect(jsonPath("$.targetCurrency").value("USD"))
                .andExpect(jsonPath("$.rate").value(0.2365));

        verify(exchangeRateService).getExchangeRate("USD");
    }

    @Test
    void rejectsInvalidCurrencyInput() throws Exception {
        mockMvc.perform(get("/api/v1/exchange-rates?to=US"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Request validation failed"));
    }

    @Test
    void mapsUnavailablePairToUnprocessableContent() throws Exception {
        when(exchangeRateService.getExchangeRate("XDR")).thenThrow(new UnavailableCurrencyPairException());

        mockMvc.perform(get("/api/v1/exchange-rates?to=XDR"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("No current MYR exchange rate is available for the requested currency"));
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class TestConfig {

        @Bean
        CurrencyCatalogueService currencyCatalogueService() {
            return mock(CurrencyCatalogueService.class);
        }

        @Bean
        ExchangeRateService exchangeRateService() {
            return mock(ExchangeRateService.class);
        }
    }
}
