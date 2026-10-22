package com.assessment.card_product_management.exchangerate;

public class ExchangeRateProviderUnavailableException extends RuntimeException {

    public ExchangeRateProviderUnavailableException(Throwable cause) {
        super(cause);
    }

    public ExchangeRateProviderUnavailableException() {
        super();
    }
}
