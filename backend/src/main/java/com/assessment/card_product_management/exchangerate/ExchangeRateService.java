package com.assessment.card_product_management.exchangerate;

import com.assessment.card_product_management.config.CacheConfig;
import com.assessment.card_product_management.exchangerate.dto.ExchangeRateResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;

@Service
public class ExchangeRateService {

    private static final String BASE_CURRENCY = "MYR";

    private final CurrencyCatalogueService currencyCatalogueService;
    private final FrankfurterClient frankfurterClient;
    private final Clock applicationClock;

    public ExchangeRateService(
            CurrencyCatalogueService currencyCatalogueService,
            FrankfurterClient frankfurterClient,
            Clock applicationClock
    ) {
        this.currencyCatalogueService = currencyCatalogueService;
        this.frankfurterClient = frankfurterClient;
        this.applicationClock = applicationClock;
    }

    @Cacheable(cacheNames = CacheConfig.EXCHANGE_RATES_CACHE, key = "#targetCurrency", sync = true)
    public ExchangeRateResponse getExchangeRate(String targetCurrency) {
        boolean isSupported = currencyCatalogueService.getCurrencies().stream()
                .anyMatch(currency -> currency.code().equals(targetCurrency));
        if (!isSupported) {
            throw new UnavailableCurrencyPairException();
        }
        if (BASE_CURRENCY.equals(targetCurrency)) {
            return new ExchangeRateResponse(
                    BASE_CURRENCY,
                    BASE_CURRENCY,
                    java.math.BigDecimal.ONE,
                    LocalDate.now(applicationClock)
            );
        }

        var providerRate = frankfurterClient.getRate(BASE_CURRENCY, targetCurrency);
        return new ExchangeRateResponse(
                BASE_CURRENCY,
                targetCurrency,
                providerRate.rate(),
                providerRate.date()
        );
    }
}
