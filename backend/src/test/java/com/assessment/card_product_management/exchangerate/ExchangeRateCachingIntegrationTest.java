package com.assessment.card_product_management.exchangerate;

import com.assessment.card_product_management.config.CacheConfig;
import com.assessment.card_product_management.config.ExchangeRateProperties;
import com.assessment.card_product_management.exchangerate.dto.FrankfurterCurrencyResponse;
import com.assessment.card_product_management.exchangerate.dto.FrankfurterRateResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringJUnitConfig(classes = {CacheConfig.class, ExchangeRateCachingIntegrationTest.TestConfig.class})
class ExchangeRateCachingIntegrationTest {

    @Autowired
    private ExchangeRateService exchangeRateService;

    @Autowired
    private FrankfurterClient frankfurterClient;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void clearCaches() {
        cacheManager.getCacheNames().forEach(cacheName -> cacheManager.getCache(cacheName).clear());
        org.mockito.Mockito.reset(frankfurterClient);
    }

    @Test
    void cachesCurrencyMetadataAndEachTargetRateSeparately() {
        when(frankfurterClient.getCurrencies()).thenReturn(List.of(
                new FrankfurterCurrencyResponse("MYR", "Malaysian Ringgit", "RM"),
                new FrankfurterCurrencyResponse("USD", "United States Dollar", "$")
        ));
        when(frankfurterClient.getRate("MYR", "USD")).thenReturn(
                new FrankfurterRateResponse(LocalDate.of(2026, 10, 21), "MYR", "USD", new BigDecimal("0.2365"))
        );

        assertThat(exchangeRateService.getExchangeRate("USD").rate()).isEqualByComparingTo("0.2365");
        assertThat(exchangeRateService.getExchangeRate("USD").rate()).isEqualByComparingTo("0.2365");

        verify(frankfurterClient, times(1)).getCurrencies();
        verify(frankfurterClient, times(1)).getRate("MYR", "USD");

        assertThatThrownBy(() -> exchangeRateService.getExchangeRate("ZZZ"))
                .isInstanceOf(UnavailableCurrencyPairException.class);
        verify(frankfurterClient, times(0)).getRate("MYR", "ZZZ");
    }

    @Test
    void returnsMyRIdentityRateWithoutCallingTheProviderRateEndpoint() {
        when(frankfurterClient.getCurrencies()).thenReturn(List.of(
                new FrankfurterCurrencyResponse("MYR", "Malaysian Ringgit", "RM")
        ));

        var response = exchangeRateService.getExchangeRate("MYR");

        assertThat(response.rate()).isEqualByComparingTo(BigDecimal.ONE);
        assertThat(response.rateDate()).isEqualTo(LocalDate.of(2026, 10, 21));
        verify(frankfurterClient, times(0)).getRate("MYR", "MYR");
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class TestConfig {

        @Bean
        ExchangeRateProperties exchangeRateProperties() {
            return new ExchangeRateProperties(
                    URI.create("https://provider.test"), Duration.ofSeconds(1), Duration.ofSeconds(1),
                    Duration.ofHours(24), Duration.ofHours(1)
            );
        }

        @Bean
        FrankfurterClient frankfurterClient() {
            return mock(FrankfurterClient.class);
        }

        @Bean
        CurrencyCatalogueService currencyCatalogueService(FrankfurterClient frankfurterClient) {
            return new CurrencyCatalogueService(frankfurterClient);
        }

        @Bean
        ExchangeRateService exchangeRateService(
                CurrencyCatalogueService currencyCatalogueService,
                FrankfurterClient frankfurterClient
        ) {
            return new ExchangeRateService(
                    currencyCatalogueService,
                    frankfurterClient,
                    Clock.fixed(Instant.parse("2026-10-21T02:00:00Z"), ZoneId.of("Asia/Kuala_Lumpur"))
            );
        }
    }
}
