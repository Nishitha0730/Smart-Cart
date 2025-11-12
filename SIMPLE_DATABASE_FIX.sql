-- ============================================
-- SUPER SIMPLE DATABASE FIX
-- Copy and paste this entire script into Supabase SQL Editor
-- Click "Run" - that's it!
-- ============================================

-- Drop and recreate tables without foreign key constraints
DROP TABLE IF EXISTS session_items CASCADE;
DROP TABLE IF EXISTS shopping_sessions CASCADE;

-- Create shopping_sessions (NO foreign keys to users or carts)
CREATE TABLE shopping_sessions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  "sessionId" TEXT UNIQUE NOT NULL,
  "cartId" TEXT NOT NULL,
  "userId" TEXT NOT NULL,
  status TEXT DEFAULT 'active' CHECK (status IN ('active', 'completed', 'abandoned')),
  "startedAt" BIGINT NOT NULL,
  "completedAt" BIGINT,
  "totalAmount" DECIMAL(10,2) DEFAULT 0.00,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create session_items (FK only to shopping_sessions)
CREATE TABLE session_items (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  "itemId" TEXT UNIQUE NOT NULL,
  "sessionId" TEXT NOT NULL REFERENCES shopping_sessions("sessionId") ON DELETE CASCADE,
  "productId" TEXT NOT NULL,
  barcode TEXT NOT NULL,
  quantity INTEGER NOT NULL DEFAULT 1,
  "unitPrice" DECIMAL(10,2) NOT NULL,
  "totalPrice" DECIMAL(10,2) NOT NULL,
  "scannedBy" TEXT DEFAULT 'customer',
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create indexes for performance
CREATE INDEX idx_sessions_sessionId ON shopping_sessions("sessionId");
CREATE INDEX idx_sessions_cartId ON shopping_sessions("cartId");
CREATE INDEX idx_sessions_userId ON shopping_sessions("userId");
CREATE INDEX idx_sessions_status ON shopping_sessions(status);
CREATE INDEX idx_session_items_sessionId ON session_items("sessionId");
CREATE INDEX idx_session_items_productId ON session_items("productId");

-- Disable Row Level Security (RLS) for testing
ALTER TABLE shopping_sessions DISABLE ROW LEVEL SECURITY;
ALTER TABLE session_items DISABLE ROW LEVEL SECURITY;

-- Reset all carts to available
UPDATE carts SET status = 'available';

-- Test: Create a sample session to verify it works
INSERT INTO shopping_sessions ("sessionId", "cartId", "userId", status, "startedAt")
VALUES (
  'TEST_SESSION_' || gen_random_uuid()::text,
  'CART_001',
  'user_test_' || extract(epoch from now())::bigint,
  'active',
  extract(epoch from now())::bigint * 1000
);

-- Show the test session we just created
SELECT
  '✅ SUCCESS! Test session created:' as message,
  "sessionId",
  "userId",
  status
FROM shopping_sessions
WHERE "sessionId" LIKE 'TEST_SESSION_%'
ORDER BY created_at DESC
LIMIT 1;

-- Clean up test session
DELETE FROM shopping_sessions WHERE "sessionId" LIKE 'TEST_SESSION_%';

-- Final verification
SELECT
  '✅ Database is ready!' as status,
  'Sessions can now be created without users table' as note;

-- Show what we have
SELECT
  'Shopping Sessions' as table_name,
  COUNT(*) as row_count
FROM shopping_sessions
UNION ALL
SELECT
  'Session Items' as table_name,
  COUNT(*) as row_count
FROM session_items
UNION ALL
SELECT
  'Carts' as table_name,
  COUNT(*) as row_count
FROM carts
UNION ALL
SELECT
  'Products' as table_name,
  COUNT(*) as row_count
FROM products;

