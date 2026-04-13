CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(320) NOT NULL,
    google_sub VARCHAR(255) NOT NULL,
    display_name VARCHAR(255),
    avatar_url VARCHAR(1024),
    role VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at TIMESTAMPTZ
);

CREATE UNIQUE INDEX ux_users_google_sub_active ON users (google_sub) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX ux_users_email_active ON users (email) WHERE deleted_at IS NULL;

CREATE TABLE wallets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users (id),
    name VARCHAR(255) NOT NULL,
    currency CHAR(3) NOT NULL DEFAULT 'USD',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at TIMESTAMPTZ
);

CREATE INDEX ix_wallets_user_id ON wallets (user_id) WHERE deleted_at IS NULL;

CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users (id),
    name VARCHAR(255) NOT NULL,
    type VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at TIMESTAMPTZ
);

CREATE INDEX ix_categories_user_id ON categories (user_id) WHERE deleted_at IS NULL;

CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    wallet_id UUID NOT NULL REFERENCES wallets (id),
    category_id UUID NOT NULL REFERENCES categories (id),
    amount NUMERIC(19, 4) NOT NULL,
    direction VARCHAR(8) NOT NULL,
    transaction_date DATE NOT NULL,
    note VARCHAR(2000),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at TIMESTAMPTZ
);

CREATE INDEX ix_transactions_wallet_date ON transactions (wallet_id, transaction_date);
CREATE INDEX ix_transactions_tx_date ON transactions (transaction_date);
CREATE INDEX ix_transactions_category_id ON transactions (category_id);

CREATE TABLE budgets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users (id),
    category_id UUID NOT NULL REFERENCES categories (id),
    year_month CHAR(7) NOT NULL,
    limit_amount NUMERIC(19, 4) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at TIMESTAMPTZ
);

CREATE UNIQUE INDEX ux_budgets_user_cat_month_active ON budgets (user_id, category_id, year_month) WHERE deleted_at IS NULL;
CREATE INDEX ix_budgets_user_year_month ON budgets (user_id, year_month) WHERE deleted_at IS NULL;

CREATE TABLE budget_alerts (
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

CREATE INDEX ix_budget_alerts_user_month ON budget_alerts (user_id, year_month);

CREATE TABLE expense_groups (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    created_by_user_id UUID NOT NULL REFERENCES users (id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at TIMESTAMPTZ
);

CREATE INDEX ix_expense_groups_creator ON expense_groups (created_by_user_id) WHERE deleted_at IS NULL;

CREATE TABLE expense_group_members (
    group_id UUID NOT NULL REFERENCES expense_groups (id),
    user_id UUID NOT NULL REFERENCES users (id),
    joined_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (group_id, user_id)
);

CREATE TABLE shared_expenses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    group_id UUID NOT NULL REFERENCES expense_groups (id),
    total_amount NUMERIC(19, 4) NOT NULL,
    paid_by_user_id UUID NOT NULL REFERENCES users (id),
    split_type VARCHAR(32) NOT NULL,
    expense_date DATE NOT NULL,
    note VARCHAR(2000),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at TIMESTAMPTZ
);

CREATE INDEX ix_shared_expenses_group ON shared_expenses (group_id) WHERE deleted_at IS NULL;

CREATE TABLE shared_expense_allocations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shared_expense_id UUID NOT NULL REFERENCES shared_expenses (id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users (id),
    amount NUMERIC(19, 4) NOT NULL,
    UNIQUE (shared_expense_id, user_id)
);

CREATE INDEX ix_shared_expense_allocations_expense ON shared_expense_allocations (shared_expense_id);
