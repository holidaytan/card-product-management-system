package com.assessment.card_product_management.exchangerate;

import com.assessment.card_product_management.exchangerate.dto.FrankfurterCurrencyResponse;
import com.assessment.card_product_management.exchangerate.dto.FrankfurterRateResponse;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.SocketTimeoutException;
import java.net.http.HttpTimeoutException;
import java.util.List;

public class FrankfurterClient {

    private final RestClient restClient;

    public FrankfurterClient(RestClient frankfurterRestClient) {
        this.restClient = frankfurterRestClient;
    }

    public List<FrankfurterCurrencyResponse> getCurrencies() {
        try {
            List<FrankfurterCurrencyResponse> response = restClient.get()
                    .uri("/v2/currencies")
                    .retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<>() { });
            if (response == null) {
                throw new ExchangeRateProviderUnavailableException();
            }
            return response;
        } catch (ResourceAccessException exception) {
            throw providerFailure(exception);
        } catch (RestClientResponseException exception) {
            throw new ExchangeRateProviderUnavailableException(exception);
        }
    }

    public FrankfurterRateResponse getRate(String baseCurrency, String targetCurrency) {
        try {
            FrankfurterRateResponse response = restClient.get()
                    .uri("/v2/rate/{base}/{target}", baseCurrency, targetCurrency)
                    .retrieve()
                    .body(FrankfurterRateResponse.class);
            if (response == null) {
                throw new ExchangeRateProviderUnavailableException();
            }
            return response;
        } catch (ResourceAccessException exception) {
            throw providerFailure(exception);
        } catch (RestClientResponseException exception) {
            HttpStatusCode status = exception.getStatusCode();
            if (status.value() == 404 || status.value() == 422) {
                throw new UnavailableCurrencyPairException();
            }
            throw new ExchangeRateProviderUnavailableException(exception);
        }
    }

    private RuntimeException providerFailure(ResourceAccessException exception) {
        Throwable current = exception;
        while (current != null) {
            if (current instanceof HttpTimeoutException || current instanceof SocketTimeoutException) {
                return new ExchangeRateProviderTimeoutException(exception);
            }
            current = current.getCause();
        }
        return new ExchangeRateProviderUnavailableException(exception);
    }
}
