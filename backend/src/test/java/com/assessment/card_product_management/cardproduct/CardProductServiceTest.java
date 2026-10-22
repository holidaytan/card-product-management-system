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
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class CardProductServiceTest {

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
    void returnsTenRecordsPerPageSortedByIdAscending() {
        List<CardProduct> products = List.of(product(1L, 0L), product(2L, 0L));
        when(cardProductRepository.findAllByFilters(any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(products, PageRequest.of(2, 10), 32));

        var response = cardProductService.getCardProducts(2);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(cardProductRepository).findAllByFilters(org.mockito.ArgumentMatchers.isNull(), org.mockito.ArgumentMatchers.isNull(), pageableCaptor.capture());
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
    void appliesNameAndCardTypeFiltersAtTheRepositoryLevel() {
        when(cardProductRepository.findAllByFilters(org.mockito.ArgumentMatchers.eq("platinum"), org.mockito.ArgumentMatchers.eq(CardType.CREDIT), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(product(1L, 0L)), PageRequest.of(0, 10), 1));

        var response = cardProductService.getCardProducts(0, "platinum", CardType.CREDIT);

        assertThat(response.totalElements()).isEqualTo(1);
        verify(cardProductRepository).findAllByFilters(org.mockito.ArgumentMatchers.eq("platinum"), org.mockito.ArgumentMatchers.eq(CardType.CREDIT), any(Pageable.class));
    }

    @Test
    void appliesANameFilterWithAccurateFilteredTotals() {
        when(cardProductRepository.findAllByFilters(org.mockito.ArgumentMatchers.eq("platinum"), org.mockito.ArgumentMatchers.isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(product(1L, 0L)), PageRequest.of(0, 10), 11));

        var response = cardProductService.getCardProducts(0, "platinum", null);

        assertThat(response.totalElements()).isEqualTo(11);
        assertThat(response.totalPages()).isEqualTo(2);
        verify(cardProductRepository).findAllByFilters(org.mockito.ArgumentMatchers.eq("platinum"), org.mockito.ArgumentMatchers.isNull(), any(Pageable.class));
    }

    @Test
    void appliesAnExactCardTypeFilter() {
        when(cardProductRepository.findAllByFilters(org.mockito.ArgumentMatchers.isNull(), org.mockito.ArgumentMatchers.eq(CardType.DEBIT), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(product(1L, 0L)), PageRequest.of(0, 10), 1));

        var response = cardProductService.getCardProducts(0, null, CardType.DEBIT);

        assertThat(response.content()).hasSize(1);
        verify(cardProductRepository).findAllByFilters(org.mockito.ArgumentMatchers.isNull(), org.mockito.ArgumentMatchers.eq(CardType.DEBIT), any(Pageable.class));
    }

    @Test
    void returnsProductById() {
        when(cardProductRepository.findById(1L)).thenReturn(Optional.of(product(1L, 0L)));

        var response = cardProductService.getCardProduct(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.productName()).isEqualTo("Everyday Debit");
    }

    @Test
    void returnsDatabaseWideProductAndInventorySummary() {
        CardProductSummaryProjection summary = mock(CardProductSummaryProjection.class);
        when(summary.getTotalProducts()).thenReturn(34L);
        when(summary.getTotalAvailableInventory()).thenReturn(3_250L);
        when(cardProductRepository.getSummary()).thenReturn(summary);

        var response = cardProductService.getCardProductSummary();

        assertThat(response.totalProducts()).isEqualTo(34L);
        assertThat(response.totalAvailableInventory()).isEqualTo(3_250L);
        verify(cardProductRepository).getSummary();
    }

    @Test
    void returnsZeroSummaryForAnEmptyDatabase() {
        CardProductSummaryProjection summary = mock(CardProductSummaryProjection.class);
        when(summary.getTotalProducts()).thenReturn(0L);
        when(summary.getTotalAvailableInventory()).thenReturn(0L);
        when(cardProductRepository.getSummary()).thenReturn(summary);

        var response = cardProductService.getCardProductSummary();

        assertThat(response.totalProducts()).isZero();
        assertThat(response.totalAvailableInventory()).isZero();
    }

    @Test
    void doesNotCacheSummaryResults() {
        CardProductSummaryProjection beforeMutation = mock(CardProductSummaryProjection.class);
        when(beforeMutation.getTotalProducts()).thenReturn(30L);
        when(beforeMutation.getTotalAvailableInventory()).thenReturn(1_000L);
        CardProductSummaryProjection afterMutation = mock(CardProductSummaryProjection.class);
        when(afterMutation.getTotalProducts()).thenReturn(31L);
        when(afterMutation.getTotalAvailableInventory()).thenReturn(1_025L);
        when(cardProductRepository.getSummary()).thenReturn(beforeMutation, afterMutation);

        var firstSummary = cardProductService.getCardProductSummary();
        var secondSummary = cardProductService.getCardProductSummary();

        assertThat(firstSummary.totalProducts()).isEqualTo(30L);
        assertThat(secondSummary.totalProducts()).isEqualTo(31L);
        assertThat(secondSummary.totalAvailableInventory()).isEqualTo(1_025L);
        verify(cardProductRepository, org.mockito.Mockito.times(2)).getSummary();
    }

    @Test
    void returnsNotFoundWhenRetrievingMissingProduct() {
        when(cardProductRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardProductService.getCardProduct(99L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void rejectsUpdateWhenClientVersionIsStale() {
        CardProduct product = product(1L, 3L);
        when(cardProductRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> cardProductService.updateCardProduct(
                1L,
                new UpdateCardProductRequest("Updated name", CardType.CREDIT, 5, new BigDecimal("100.00"), new BigDecimal("15.00"), 2L)
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
                new UpdateCardProductRequest("Updated name", CardType.CREDIT, 5, new BigDecimal("100.00"), new BigDecimal("15.00"), 0L)
        ))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deletesExistingProduct() {
        CardProduct product = product(4L, 0L);
        product.update("Everyday Debit", CardType.DEBIT, 0, BigDecimal.ZERO, null, LocalDateTime.now());
        when(cardProductRepository.findById(4L)).thenReturn(Optional.of(product));

        cardProductService.deleteCardProduct(4L);

        verify(cardProductRepository).delete(product);
    }

    @Test
    void rejectsDeletionWhenProductStillHasInventory() {
        CardProduct product = product(4L, 0L);
        product.update("Everyday Debit", CardType.DEBIT, 1, BigDecimal.ZERO, null, LocalDateTime.now());
        when(cardProductRepository.findById(4L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> cardProductService.deleteCardProduct(4L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);

        verify(cardProductRepository, never()).delete(any());
    }

    @Test
    void createsTimestampsUsingTheConfiguredClock() {
        when(cardProductRepository.saveAndFlush(any(CardProduct.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = cardProductService.createCardProduct(
                new CreateCardProductRequest("Everyday Debit", CardType.DEBIT, 5, BigDecimal.ZERO, null)
        );

        assertThat(response.createdAt()).isEqualTo(LocalDateTime.of(2026, 10, 21, 10, 0));
        assertThat(response.updatedAt()).isEqualTo(response.createdAt());
        assertThat(response.annualFee()).isZero();
        assertThat(response.annualInterestRate()).isNull();
        assertThat(response.currencyCode()).isEqualTo("MYR");
    }

    @Test
    void rejectsInterestRateForDebitProduct() {
        assertThatThrownBy(() -> cardProductService.createCardProduct(
                new CreateCardProductRequest("Everyday Debit", CardType.DEBIT, 5, BigDecimal.ZERO, new BigDecimal("15.00"))
        ))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private CardProduct product(long id, long version) {
        CardProduct product = new CardProduct(
                "Everyday Debit",
                CardType.DEBIT,
                5,
                BigDecimal.ZERO,
                null,
                LocalDateTime.of(2026, 10, 21, 10, 0)
        );
        ReflectionTestUtils.setField(product, "id", id);
        ReflectionTestUtils.setField(product, "version", version);
        return product;
    }
}
