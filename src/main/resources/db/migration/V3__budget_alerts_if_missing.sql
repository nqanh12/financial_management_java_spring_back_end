-- Databases that applied an older V1 before budget_alerts existed will not re-run V1; add the table here.
CREATE TABLE IF NOT EXISTS budget_alerts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users (id),
    category_id UUID NOT NULL REFERENCES categories (id),
    year_month CHAR(7) NOT NULL,
    spent_amount NUMERIC(19, 4) NOT NULL,
    limit_amount NUMERIC(19, 4) NOT NULL,
    message VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS ix_budget_alerts_user_month ON budget_alerts (user_id, year_month);
