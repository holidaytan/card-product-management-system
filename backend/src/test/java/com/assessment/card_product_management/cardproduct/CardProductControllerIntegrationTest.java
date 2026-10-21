package com.assessment.card_product_management.cardproduct;

import com.assessment.card_product_management.cardproduct.dto.CardProductPageResponse;
import com.assessment.card_product_management.cardproduct.dto.CardProductResponse;
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
        when(cardProductService.getCardProducts(0)).thenReturn(
                new CardProductPageResponse(List.of(), 0, 10, 30, 3, true, false)
        );

        mockMvc.perform(get("/api/v1/card-products?page=0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(30));

        verify(cardProductService).getCardProducts(0);
    }

    @Test
    void createCardProductRejectsInvalidInput() throws Exception {
        mockMvc.perform(post("/api/v1/card-products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productName":" ","cardType":"CREDIT","quantity":-1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Request validation failed"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void createCardProductReturnsCreatedAndForwardsIdempotencyKey() throws Exception {
        when(cardProductService.createCardProduct(
                new com.assessment.card_product_management.cardproduct.dto.CreateCardProductRequest(
                        "Everyday Credit", CardType.CREDIT, 5
                ),
                "create-123"
        )).thenReturn(new CardProductResponse(1L, "Everyday Credit", CardType.CREDIT, 5, null, null, 0L));

        mockMvc.perform(post("/api/v1/card-products")
                        .header("Idempotency-Key", "create-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productName":"Everyday Credit","cardType":"CREDIT","quantity":5}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(cardProductService).createCardProduct(
                new com.assessment.card_product_management.cardproduct.dto.CreateCardProductRequest(
                        "Everyday Credit", CardType.CREDIT, 5
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
