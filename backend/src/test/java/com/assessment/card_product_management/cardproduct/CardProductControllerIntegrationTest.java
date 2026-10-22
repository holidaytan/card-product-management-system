package com.assessment.card_product_management.cardproduct;

import com.assessment.card_product_management.cardproduct.dto.CardProductPageResponse;
import com.assessment.card_product_management.cardproduct.dto.CardProductResponse;
import com.assessment.card_product_management.cardproduct.dto.CardProductSummaryResponse;
import com.assessment.card_product_management.config.SecurityConfig;
import com.assessment.card_product_management.error.ApiExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.math.BigDecimal;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CardProductController.class)
@Import({SecurityConfig.class, ApiExceptionHandler.class, CardProductControllerIntegrationTest.TestConfig.class})
class CardProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CardProductService cardProductService;

    @Test
    void listCardProductsReturnsThePaginationResponse() throws Exception {
        when(cardProductService.getCardProducts(0, null, null)).thenReturn(
                new CardProductPageResponse(List.of(), 0, 10, 30, 3, true, false)
        );

        mockMvc.perform(get("/api/v1/card-products?page=0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(30));

        verify(cardProductService).getCardProducts(0, null, null);
    }

    @Test
    void listCardProductsForwardsTrimmedSearchAndCardType() throws Exception {
        when(cardProductService.getCardProducts(0, "platinum", CardType.CREDIT)).thenReturn(
                new CardProductPageResponse(List.of(), 0, 10, 0, 0, true, true)
        );

        mockMvc.perform(get("/api/v1/card-products?page=0&type=credit").param("name", " platinum "))
                .andExpect(status().isOk());

        verify(cardProductService).getCardProducts(0, "platinum", CardType.CREDIT);
    }

    @Test
    void listCardProductsRejectsInvalidCardTypeFilter() throws Exception {
        mockMvc.perform(get("/api/v1/card-products?type=PREPAID"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsDatabaseWideProductSummary() throws Exception {
        when(cardProductService.getCardProductSummary()).thenReturn(
                new CardProductSummaryResponse(34L, 3_250L)
        );

        mockMvc.perform(get("/api/v1/card-products/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProducts").value(34))
                .andExpect(jsonPath("$.totalAvailableInventory").value(3250));

        verify(cardProductService).getCardProductSummary();
    }

    @Test
    void getCardProductReturnsTheProduct() throws Exception {
        when(cardProductService.getCardProduct(1L)).thenReturn(
                new CardProductResponse(1L, "Everyday Debit", CardType.DEBIT, 5, BigDecimal.ZERO, null, "MYR", null, null, 0L)
        );

        mockMvc.perform(get("/api/v1/card-products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.productName").value("Everyday Debit"))
                .andExpect(jsonPath("$.currencyCode").value("MYR"));

        verify(cardProductService).getCardProduct(1L);
    }

    @Test
    void createCardProductRejectsInvalidInput() throws Exception {
        mockMvc.perform(post("/api/v1/card-products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productName":" ","cardType":"CREDIT","quantity":-1,"annualFee":-1,"annualInterestRate":101}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Request validation failed"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void createCardProductReturnsCreatedAndForwardsIdempotencyKey() throws Exception {
        when(cardProductService.createCardProduct(
                new com.assessment.card_product_management.cardproduct.dto.CreateCardProductRequest(
                        "Everyday Credit", CardType.CREDIT, 5, new BigDecimal("100.00"), new BigDecimal("15.00")
                ),
                "create-123"
        )).thenReturn(new CardProductResponse(1L, "Everyday Credit", CardType.CREDIT, 5, new BigDecimal("100.00"), new BigDecimal("15.00"), "MYR", null, null, 0L));

        mockMvc.perform(post("/api/v1/card-products")
                        .header("Idempotency-Key", "create-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productName":"Everyday Credit","cardType":"CREDIT","quantity":5,"annualFee":100.00,"annualInterestRate":15.00,"currencyCode":"USD"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(cardProductService).createCardProduct(
                new com.assessment.card_product_management.cardproduct.dto.CreateCardProductRequest(
                        "Everyday Credit", CardType.CREDIT, 5, new BigDecimal("100.00"), new BigDecimal("15.00")
                ),
                "create-123"
        );
    }

    @Test
    void nonApiRoutesReturnNotFound() throws Exception {
        mockMvc.perform(get("/internal-only"))
                .andExpect(status().isNotFound());
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class TestConfig {

        @Bean
        CardProductService cardProductService() {
            return mock(CardProductService.class);
        }
    }
}
