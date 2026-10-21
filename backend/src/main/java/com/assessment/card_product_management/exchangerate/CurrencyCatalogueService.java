package com.assessment.card_product_management.exchangerate;

import com.assessment.card_product_management.config.CacheConfig;
import com.assessment.card_product_management.exchangerate.dto.CurrencyResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class CurrencyCatalogueService {

    private final FrankfurterClient frankfurterClient;

    public CurrencyCatalogueService(FrankfurterClient frankfurterClient) {
        this.frankfurterClient = frankfurterClient;
    }

    @Cacheable(cacheNames = CacheConfig.EXCHANGE_RATE_CURRENCIES_CACHE, sync = true)
    public List<CurrencyResponse> getCurrencies() {
        return frankfurterClient.getCurrencies().stream()
                .filter(currency -> currency.isoCode() != null && currency.name() != null)
                .map(currency -> new CurrencyResponse(
                        currency.isoCode().toUpperCase(Locale.ROOT),
                        currency.name(),
                        currency.symbol()
                ))
                .sorted(Comparator.comparing(CurrencyResponse::code))
                .toList();
    }

}
