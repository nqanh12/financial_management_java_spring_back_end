-- Hibernate validates String columns as VARCHAR; CHAR(3) is reported as JDBC CHAR (bpchar).
ALTER TABLE wallets
    ALTER COLUMN currency TYPE VARCHAR(3) USING trim(currency::varchar);
