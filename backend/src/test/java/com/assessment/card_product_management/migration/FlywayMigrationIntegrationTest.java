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
                .contains("1__create card products", "2__seed card products");

        Integer productCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM dbo.card_products",
                Integer.class
        );
        assertThat(productCount).isGreaterThanOrEqualTo(30);
    }
}
