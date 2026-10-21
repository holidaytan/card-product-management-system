-- Malaysia uses UTC+08:00 year-round. The explicit time-zone conversion avoids
-- depending on the SQL Server host/container time zone.
ALTER TABLE card_products DROP CONSTRAINT DF_card_products_created_at;
ALTER TABLE card_products DROP CONSTRAINT DF_card_products_updated_at;

ALTER TABLE card_products
    ADD CONSTRAINT DF_card_products_created_at
    DEFAULT CONVERT(DATETIME2(3), SYSUTCDATETIME() AT TIME ZONE 'UTC' AT TIME ZONE 'Singapore Standard Time')
    FOR created_at;

ALTER TABLE card_products
    ADD CONSTRAINT DF_card_products_updated_at
    DEFAULT CONVERT(DATETIME2(3), SYSUTCDATETIME() AT TIME ZONE 'UTC' AT TIME ZONE 'Singapore Standard Time')
    FOR updated_at;

ALTER TABLE idempotency_records DROP CONSTRAINT df_idempotency_records_created_at;

ALTER TABLE idempotency_records
    ADD CONSTRAINT df_idempotency_records_created_at
    DEFAULT CONVERT(DATETIME2(3), SYSUTCDATETIME() AT TIME ZONE 'UTC' AT TIME ZONE 'Singapore Standard Time')
    FOR created_at;
