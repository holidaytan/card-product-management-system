package com.assessment.card_product_management.cardproduct;

import com.assessment.card_product_management.cardproduct.dto.CreateCardProductRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardProductIdempotencyTest {

    @Mock
    private CardProductRepository cardProductRepository;

    @Mock
    private IdempotencyRecordRepository idempotencyRecordRepository;

    private CardProductService cardProductService;

    @BeforeEach
    void setUp() {
        cardProductService = new CardProductService(
                cardProductRepository,
                idempotencyRecordRepository,
                Clock.fixed(Instant.parse("2026-10-21T02:00:00Z"), ZoneId.of("Asia/Kuala_Lumpur"))
        );
    }

    @Test
    void returnsTheOriginalProductForAnIdempotentReplay() {
        CreateCardProductRequest request = new CreateCardProductRequest("Everyday Debit", CardType.DEBIT, 5);
        CardProduct product = product(7L);
        IdempotencyRecord record = new IdempotencyRecord("request-123", requestHash(request), product);
        when(idempotencyRecordRepository.findById("request-123")).thenReturn(Optional.of(record));

        var response = cardProductService.createCardProduct(request, "request-123");

        assertThat(response.id()).isEqualTo(7L);
        verify(cardProductRepository, never()).saveAndFlush(any());
    }

    @Test
    void rejectsAnIdempotencyKeyReusedForADifferentRequest() {
        CreateCardProductRequest originalRequest = new CreateCardProductRequest("Everyday Debit", CardType.DEBIT, 5);
        IdempotencyRecord record = new IdempotencyRecord("request-123", requestHash(originalRequest), product(7L));
        when(idempotencyRecordRepository.findById("request-123")).thenReturn(Optional.of(record));

        assertThatThrownBy(() -> cardProductService.createCardProduct(
                new CreateCardProductRequest("Everyday Debit", CardType.DEBIT, 6),
                "request-123"
        ))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode().value())
                .isEqualTo(409);
    }

    private CardProduct product(long id) {
        CardProduct product = new CardProduct(
                "Everyday Debit",
                CardType.DEBIT,
                5,
                java.time.LocalDateTime.of(2026, 10, 21, 10, 0)
        );
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }

    private String requestHash(CreateCardProductRequest request) {
        try {
            byte[] value = (request.productName().trim() + "|" + request.cardType().name() + "|" + request.quantity())
                    .getBytes(java.nio.charset.StandardCharsets.UTF_8);
            return java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(value));
        } catch (java.security.NoSuchAlgorithmException exception) {
            throw new AssertionError(exception);
        }
    }
}
