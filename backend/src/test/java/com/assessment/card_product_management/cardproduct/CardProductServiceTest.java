package com.assessment.card_product_management.cardproduct;

import com.assessment.card_product_management.cardproduct.dto.CreateCardProductRequest;
import com.assessment.card_product_management.cardproduct.dto.UpdateCardProductRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardProductServiceTest {

    @Mock
    private CardProductRepository cardProductRepository;

    @Mock
    private IdempotencyRecordRepository idempotencyRecordRepository;

    private CardProductService cardProductService;

    @BeforeEach
    void setUp() {
        cardProductService = new CardProductService(cardProductRepository, idempotencyRecordRepository);
    }

    @Test
    void returnsTenRecordsPerPageSortedByIdAscending() {
        List<CardProduct> products = List.of(product(1L, 0L), product(2L, 0L));
        when(cardProductRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(products, PageRequest.of(2, 10), 32));

        var response = cardProductService.getCardProducts(2);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(cardProductRepository).findAll(pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(2);
        assertThat(pageable.getPageSize()).isEqualTo(10);
        assertThat(pageable.getSort().getOrderFor("id").getDirection().isAscending()).isTrue();
        assertThat(response.page()).isEqualTo(2);
        assertThat(response.size()).isEqualTo(10);
        assertThat(response.totalElements()).isEqualTo(32);
        assertThat(response.totalPages()).isEqualTo(4);
        assertThat(response.last()).isFalse();
    }

    @Test
    void rejectsUpdateWhenClientVersionIsStale() {
        CardProduct product = product(1L, 3L);
        when(cardProductRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> cardProductService.updateCardProduct(
                1L,
                new UpdateCardProductRequest("Updated name", CardType.CREDIT, 5, 2L)
        ))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);

        verify(cardProductRepository, never()).saveAndFlush(any());
    }

    @Test
    void returnsNotFoundWhenUpdatingMissingProduct() {
        when(cardProductRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardProductService.updateCardProduct(
                99L,
                new UpdateCardProductRequest("Updated name", CardType.CREDIT, 5, 0L)
        ))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deletesExistingProduct() {
        CardProduct product = product(4L, 0L);
        when(cardProductRepository.findById(4L)).thenReturn(Optional.of(product));

        cardProductService.deleteCardProduct(4L);

        verify(cardProductRepository).delete(product);
    }

    @Test
    void createsTimestampsInMalaysiaTime() {
        LocalDateTime beforeCreation = LocalDateTime.now(ZoneId.of("Asia/Kuala_Lumpur")).minusSeconds(1);

        CardProduct product = new CardProduct("Everyday Debit", CardType.DEBIT, 5);

        LocalDateTime afterCreation = LocalDateTime.now(ZoneId.of("Asia/Kuala_Lumpur")).plusSeconds(1);
        assertThat(product.getCreatedAt()).isBetween(beforeCreation, afterCreation);
        assertThat(product.getUpdatedAt()).isEqualTo(product.getCreatedAt());
    }

    private CardProduct product(long id, long version) {
        CardProduct product = new CardProduct("Everyday Debit", CardType.DEBIT, 5);
        ReflectionTestUtils.setField(product, "id", id);
        ReflectionTestUtils.setField(product, "version", version);
        return product;
    }
}
