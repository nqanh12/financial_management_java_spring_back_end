-- One active alert per (user, category, month); support upserts.
-- Deduplicate before creating the unique index (keep latest created_at, then smallest id).
DELETE FROM budget_alerts a
WHERE a.deleted_at IS NULL
  AND a.id NOT IN (
        SELECT id
        FROM (
                 SELECT DISTINCT ON (user_id, category_id, year_month) id
                 FROM budget_alerts
                 WHERE deleted_at IS NULL
                 ORDER BY user_id, category_id, year_month, created_at DESC, id ASC
             ) keeper
    );

ALTER TABLE budget_alerts
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ NOT NULL DEFAULT now();

CREATE UNIQUE INDEX IF NOT EXISTS ux_budget_alerts_user_cat_month_active
    ON budget_alerts (user_id, category_id, year_month)
    WHERE deleted_at IS NULL;
