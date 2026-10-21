package com.assessment.card_product_management.migration;

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

    @Test
    void appliedMigrationsAreValidAndCardProductsAreSeeded() {
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

        Integer productCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM dbo.card_products",
                Integer.class
        );
        assertThat(productCount).isGreaterThanOrEqualTo(30);
    }

    @Test
    void pricingMigrationsSeedValidMyRPricingForOriginalProducts() {
        Integer validSeededPricingCount = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM dbo.card_products
                        WHERE id BETWEEN 1 AND 30
                          AND currency_code = 'MYR'
                          AND annual_fee >= 0
                          AND (
                              (card_type = 'CREDIT' AND annual_interest_rate BETWEEN 0 AND 100)
                              OR (card_type = 'DEBIT' AND annual_interest_rate IS NULL)
                          )
                        """,
                Integer.class
        );

        Integer debitProductsWithInterest = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM dbo.card_products
                        WHERE card_type = 'DEBIT'
                          AND annual_interest_rate IS NOT NULL
                        """,
                Integer.class
        );

        assertThat(validSeededPricingCount).isEqualTo(30);
        assertThat(debitProductsWithInterest).isZero();
    }
}
