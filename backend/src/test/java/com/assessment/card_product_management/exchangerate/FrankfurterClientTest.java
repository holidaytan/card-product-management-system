package com.assessment.card_product_management.exchangerate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

class FrankfurterClientTest {

    private MockRestServiceServer server;
    private FrankfurterClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://provider.test");
        server = MockRestServiceServer.bindTo(builder).build();
        client = new FrankfurterClient(builder.build());
    }

    @Test
    void mapsCurrencyMetadataWithAnOptionalSymbol() {
        server.expect(requestTo("https://provider.test/v2/currencies"))
                .andRespond(withStatus(org.springframework.http.HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("""
                        [{"iso_code":"USD","name":"United States Dollar","symbol":"$"},
                         {"iso_code":"XDR","name":"Special Drawing Rights"}]
                        """));

        var currencies = client.getCurrencies();

        assertThat(currencies).hasSize(2);
        assertThat(currencies.get(0).isoCode()).isEqualTo("USD");
        assertThat(currencies.get(1).symbol()).isNull();
        server.verify();
    }

    @Test
    void mapsUnavailablePairFromProviderNotFound() {
        server.expect(requestTo("https://provider.test/v2/rate/MYR/ZZZ"))
                .andRespond(withStatus(org.springframework.http.HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"status\":404}"));

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> client.getRate("MYR", "ZZZ"))
                .isInstanceOf(UnavailableCurrencyPairException.class);
        server.verify();
    }
}
