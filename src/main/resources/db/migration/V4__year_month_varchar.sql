-- Hibernate validates String columns as VARCHAR; CHAR(n) in PostgreSQL is reported as JDBC CHAR (bpchar).
ALTER TABLE budgets
    ALTER COLUMN year_month TYPE VARCHAR(7) USING year_month::varchar;

ALTER TABLE budget_alerts
    ALTER COLUMN year_month TYPE VARCHAR(7) USING year_month::varchar;
