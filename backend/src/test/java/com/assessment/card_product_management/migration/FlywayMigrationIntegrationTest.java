package com.assessment.card_product_management.migration;

import com.assessment.card_product_management.cardproduct.CardProductRepository;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class FlywayMigrationIntegrationTest {

    @Autowired
    private Flyway flyway;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CardProductRepository cardProductRepository;

    @Test
    void appliedMigrationsAreValid() {
        flyway.validate();

        assertThat(flyway.info().applied())
                .extracting(info -> info.getVersion() + "__" + info.getDescription())
                .contains(
                        "1__create card products",
                        "2__seed card products",
                        "3__create idempotency records",
                        "4__add card product pricing",
                        "5__seed card product pricing"
                );

    }

    @Test
    void existingProductsRespectPricingRules() {
        Integer invalidPricingCount = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM dbo.card_products
                        WHERE currency_code <> 'MYR'
                           OR annual_fee < 0
                           OR annual_interest_rate < 0
                           OR annual_interest_rate > 100
                           OR (card_type = 'DEBIT' AND annual_interest_rate IS NOT NULL)
                        """,
                Integer.class
        );

        assertThat(invalidPricingCount).isZero();
    }

    @Test
    void summaryProjectionMatchesDatabaseAggregates() {
        var summary = cardProductRepository.getSummary();
        Long expectedProductCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM dbo.card_products", Long.class
        );
        Long expectedInventory = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(CAST(quantity AS BIGINT)), 0) FROM dbo.card_products", Long.class
        );

        assertThat(summary.getTotalProducts()).isEqualTo(expectedProductCount);
        assertThat(summary.getTotalAvailableInventory()).isEqualTo(expectedInventory);
    }
}
