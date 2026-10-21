package com.assessment.card_product_management.config;

import com.assessment.card_product_management.cardproduct.CardProductController;
import com.assessment.card_product_management.cardproduct.CardProductService;
import com.assessment.card_product_management.error.ApiExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CardProductController.class)
@Import({SecurityConfig.class, ApiExceptionHandler.class, SecurityConfigIntegrationTest.TestConfig.class})
class SecurityConfigIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void permitsCardProductApiForTheCurrentUnauthenticatedAssessmentScope() throws Exception {
        mockMvc.perform(get("/api/v1/card-products"))
                .andExpect(status().isOk());
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class TestConfig {

        @Bean
        CardProductService cardProductService() {
            return mock(CardProductService.class);
        }
    }
}
