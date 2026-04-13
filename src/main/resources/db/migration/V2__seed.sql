-- Sample admin user for documentation / local testing (real users are created via Google OAuth).
INSERT INTO users (id, email, google_sub, display_name, role, created_at, updated_at)
SELECT '00000000-0000-0000-0000-000000000001',
       'admin@example.com',
       'seed-admin-google-sub',
       'Seed Admin',
       'ADMIN',
       now(),
       now()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE id = '00000000-0000-0000-0000-000000000001');
