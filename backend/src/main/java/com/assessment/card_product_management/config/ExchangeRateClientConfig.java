package com.assessment.card_product_management.config;

import com.assessment.card_product_management.exchangerate.FrankfurterClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;

@Configuration
@EnableConfigurationProperties(ExchangeRateProperties.class)
public class ExchangeRateClientConfig {

    @Bean
    RestClient frankfurterRestClient(ExchangeRateProperties properties) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.connectTimeout())
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(properties.readTimeout());

        return RestClient.builder()
                .baseUrl(properties.providerBaseUrl().toString())
                .requestFactory(requestFactory)
                .build();
    }

    @Bean
    FrankfurterClient frankfurterClient(RestClient frankfurterRestClient) {
        return new FrankfurterClient(frankfurterRestClient);
    }
}
