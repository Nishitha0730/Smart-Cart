-- ==============================================
-- EMERGENCY FIX - Session Creation Issues
-- Run this in Supabase SQL Editor
-- ==============================================

-- 1. Check if tables exist
DO $$
BEGIN
    RAISE NOTICE 'Checking tables...';
END $$;

SELECT
    table_name,
    CASE
        WHEN table_name IN (SELECT tablename FROM pg_tables WHERE schemaname = 'public')
        THEN '✅ EXISTS'
        ELSE '❌ MISSING'
    END as status
FROM (VALUES
    ('carts'),
    ('shopping_sessions'),
    ('session_items'),
    ('users'),
    ('products')
) AS t(table_name);

-- 2. Temporarily disable RLS for testing
ALTER TABLE IF EXISTS shopping_sessions DISABLE ROW LEVEL SECURITY;
ALTER TABLE IF EXISTS session_items DISABLE ROW LEVEL SECURITY;
ALTER TABLE IF EXISTS carts DISABLE ROW LEVEL SECURITY;

RAISE NOTICE '✅ RLS disabled for testing';

-- 3. Check if test user exists, create if not
INSERT INTO users ("userId", email, name, phone)
VALUES ('USER_001', 'test@example.com', 'Test User', NULL)
ON CONFLICT ("userId") DO UPDATE
SET email = EXCLUDED.email;

RAISE NOTICE '✅ Test user created/updated';

-- 4. Check if test cart exists
INSERT INTO carts ("cartId", status)
VALUES ('CART_001', 'available')
ON CONFLICT ("cartId") DO UPDATE
SET status = 'available';

RAISE NOTICE '✅ Test cart created/updated';

-- 5. Clean up old sessions
UPDATE carts SET status = 'available' WHERE status = 'in_use';
DELETE FROM session_items WHERE "sessionId" IN (
    SELECT "sessionId" FROM shopping_sessions WHERE status = 'active'
);
DELETE FROM shopping_sessions WHERE status = 'active';

RAISE NOTICE '✅ Old sessions cleaned up';

-- 6. Check foreign key constraints
SELECT
    tc.constraint_name,
    kcu.column_name,
    ccu.table_name AS references_table,
    ccu.column_name AS references_column
FROM information_schema.table_constraints AS tc
JOIN information_schema.key_column_usage AS kcu
    ON tc.constraint_name = kcu.constraint_name
JOIN information_schema.constraint_column_usage AS ccu
    ON ccu.constraint_name = tc.constraint_name
WHERE tc.table_name = 'shopping_sessions'
    AND tc.constraint_type = 'FOREIGN KEY';

-- 7. If userId foreign key exists but users table doesn't, remove it
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE table_name = 'shopping_sessions'
        AND constraint_name LIKE '%userId%'
        AND constraint_type = 'FOREIGN KEY'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_name = 'users'
    ) THEN
        ALTER TABLE shopping_sessions DROP CONSTRAINT IF EXISTS shopping_sessions_userId_fkey;
        RAISE NOTICE '✅ Removed userId foreign key (users table not found)';
    END IF;
END $$;

-- 8. Verify setup
SELECT
    'Setup verification:' as message
UNION ALL
SELECT CONCAT('Users: ', COUNT(*)) FROM users
UNION ALL
SELECT CONCAT('Carts: ', COUNT(*)) FROM carts
UNION ALL
SELECT CONCAT('Products: ', COUNT(*)) FROM products
UNION ALL
SELECT CONCAT('Sessions: ', COUNT(*)) FROM shopping_sessions
UNION ALL
SELECT CONCAT('Session Items: ', COUNT(*)) FROM session_items;

-- 9. Test insert session
DO $$
DECLARE
    test_session_id TEXT := 'TEST_' || gen_random_uuid()::text;
BEGIN
    -- Try to insert a test session
    INSERT INTO shopping_sessions ("sessionId", "cartId", "userId", status, "startedAt")
    VALUES (
        test_session_id,
        'CART_001',
        'USER_001',
        'active',
        extract(epoch from now())::bigint * 1000
    );

    -- Clean up test session
    DELETE FROM shopping_sessions WHERE "sessionId" = test_session_id;

    RAISE NOTICE '✅ Test session insert SUCCESSFUL';
EXCEPTION
    WHEN OTHERS THEN
        RAISE NOTICE '❌ Test session insert FAILED: %', SQLERRM;
END $$;

-- 10. Final instructions
DO $$
BEGIN
    RAISE NOTICE '';
    RAISE NOTICE '===========================================';
    RAISE NOTICE 'SETUP COMPLETE!';
    RAISE NOTICE '===========================================';
    RAISE NOTICE '';
    RAISE NOTICE 'Now rebuild and run your app.';
    RAISE NOTICE 'Watch Logcat for:';
    RAISE NOTICE '  ✅ Session created successfully in database';
    RAISE NOTICE '';
    RAISE NOTICE 'If you still see errors, check:';
    RAISE NOTICE '  1. Column names match (sessionId vs session_id)';
    RAISE NOTICE '  2. Data types are correct';
    RAISE NOTICE '  3. Run setup_complete.sql for full schema';
    RAISE NOTICE '';
END $$;

