IF OBJECT_ID(N'dbo.idempotency_records', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.idempotency_records (
        idempotency_key NVARCHAR(255) NOT NULL,
        request_hash VARCHAR(64) NOT NULL,
        card_product_id BIGINT NOT NULL,
        created_at DATETIME2(3) NOT NULL
            CONSTRAINT df_idempotency_records_created_at
                DEFAULT CONVERT(DATETIME2(3), SYSUTCDATETIME() AT TIME ZONE 'UTC' AT TIME ZONE 'Singapore Standard Time'),
        CONSTRAINT pk_idempotency_records PRIMARY KEY (idempotency_key),
        CONSTRAINT fk_idempotency_records_card_product
            FOREIGN KEY (card_product_id) REFERENCES dbo.card_products(id) ON DELETE CASCADE
    );
END;
