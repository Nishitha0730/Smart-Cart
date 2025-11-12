-- ============================================
-- SmartCart Complete Database Setup with Customer Tracking
-- Run this in your Supabase SQL Editor
-- ============================================

-- 1. Create users/customers table
CREATE TABLE IF NOT EXISTS users (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  "userId" TEXT UNIQUE NOT NULL,
  email TEXT UNIQUE NOT NULL,
  name TEXT NOT NULL,
  phone TEXT,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  last_login TIMESTAMP WITH TIME ZONE
);

-- 2. Create carts table
CREATE TABLE IF NOT EXISTS carts (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  "cartId" TEXT UNIQUE NOT NULL,
  status TEXT DEFAULT 'available' CHECK (status IN ('available', 'in_use', 'maintenance')),
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 3. Create shopping_sessions table (links customer to cart)
CREATE TABLE IF NOT EXISTS shopping_sessions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  "sessionId" TEXT UNIQUE NOT NULL,
  "cartId" TEXT NOT NULL REFERENCES carts("cartId"),
  "userId" TEXT NOT NULL REFERENCES users("userId"),
  status TEXT DEFAULT 'active' CHECK (status IN ('active', 'completed', 'abandoned')),
  "startedAt" BIGINT NOT NULL,
  "completedAt" BIGINT,
  "totalAmount" DECIMAL(10,2) DEFAULT 0.00,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 4. Create products table
CREATE TABLE IF NOT EXISTS products (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  "productId" TEXT UNIQUE NOT NULL,
  barcode TEXT UNIQUE NOT NULL,
  name TEXT NOT NULL,
  description TEXT,
  price DECIMAL(10,2) NOT NULL,
  "imageUrl" TEXT,
  category TEXT,
  "stockQuantity" INTEGER DEFAULT 0,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 5. Create session_items table (items in current shopping cart)
CREATE TABLE IF NOT EXISTS session_items (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  "itemId" TEXT UNIQUE NOT NULL,
  "sessionId" TEXT NOT NULL REFERENCES shopping_sessions("sessionId") ON DELETE CASCADE,
  "productId" TEXT NOT NULL REFERENCES products("productId"),
  barcode TEXT NOT NULL,
  quantity INTEGER NOT NULL DEFAULT 1,
  "unitPrice" DECIMAL(10,2) NOT NULL,
  "totalPrice" DECIMAL(10,2) NOT NULL,
  "scannedBy" TEXT DEFAULT 'customer',
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 6. Create orders table (completed purchases)
CREATE TABLE IF NOT EXISTS orders (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  "orderId" TEXT UNIQUE NOT NULL,
  "sessionId" TEXT NOT NULL REFERENCES shopping_sessions("sessionId"),
  "userId" TEXT NOT NULL REFERENCES users("userId"),
  "totalAmount" DECIMAL(10,2) NOT NULL,
  "discountAmount" DECIMAL(10,2) DEFAULT 0.00,
  "finalAmount" DECIMAL(10,2) NOT NULL,
  "paymentMethod" TEXT NOT NULL,
  "paymentStatus" TEXT DEFAULT 'pending' CHECK ("paymentStatus" IN ('pending', 'completed', 'failed', 'refunded')),
  "orderStatus" TEXT DEFAULT 'processing' CHECK ("orderStatus" IN ('processing', 'completed', 'cancelled')),
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  completed_at TIMESTAMP WITH TIME ZONE
);

-- 7. Create order_items table (permanent record of what was purchased)
CREATE TABLE IF NOT EXISTS order_items (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  "orderItemId" TEXT UNIQUE NOT NULL DEFAULT gen_random_uuid()::text,
  "orderId" TEXT NOT NULL REFERENCES orders("orderId") ON DELETE CASCADE,
  "productId" TEXT NOT NULL REFERENCES products("productId"),
  "productName" TEXT NOT NULL,  -- Store name in case product is deleted
  barcode TEXT NOT NULL,
  quantity INTEGER NOT NULL,
  "unitPrice" DECIMAL(10,2) NOT NULL,
  "totalPrice" DECIMAL(10,2) NOT NULL,
  category TEXT,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ============================================
-- INDEXES for Performance
-- ============================================

-- Users indexes
CREATE INDEX IF NOT EXISTS idx_users_userId ON users("userId");
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- Carts indexes
CREATE INDEX IF NOT EXISTS idx_carts_cartId ON carts("cartId");
CREATE INDEX IF NOT EXISTS idx_carts_status ON carts(status);

-- Shopping sessions indexes
CREATE INDEX IF NOT EXISTS idx_sessions_sessionId ON shopping_sessions("sessionId");
CREATE INDEX IF NOT EXISTS idx_sessions_cartId ON shopping_sessions("cartId");
CREATE INDEX IF NOT EXISTS idx_sessions_userId ON shopping_sessions("userId");
CREATE INDEX IF NOT EXISTS idx_sessions_status ON shopping_sessions(status);

-- Products indexes
CREATE INDEX IF NOT EXISTS idx_products_productId ON products("productId");
CREATE INDEX IF NOT EXISTS idx_products_barcode ON products(barcode);
CREATE INDEX IF NOT EXISTS idx_products_category ON products(category);

-- Session items indexes
CREATE INDEX IF NOT EXISTS idx_session_items_sessionId ON session_items("sessionId");
CREATE INDEX IF NOT EXISTS idx_session_items_productId ON session_items("productId");

-- Orders indexes
CREATE INDEX IF NOT EXISTS idx_orders_orderId ON orders("orderId");
CREATE INDEX IF NOT EXISTS idx_orders_userId ON orders("userId");
CREATE INDEX IF NOT EXISTS idx_orders_sessionId ON orders("sessionId");
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders("orderStatus");
CREATE INDEX IF NOT EXISTS idx_orders_created_at ON orders(created_at);

-- Order items indexes
CREATE INDEX IF NOT EXISTS idx_order_items_orderId ON order_items("orderId");
CREATE INDEX IF NOT EXISTS idx_order_items_productId ON order_items("productId");
CREATE INDEX IF NOT EXISTS idx_order_items_category ON order_items(category);

-- ============================================
-- TRIGGERS for auto-updating timestamps
-- ============================================

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
   NEW.updated_at = NOW();
   RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply trigger to carts
DROP TRIGGER IF EXISTS update_carts_updated_at ON carts;
CREATE TRIGGER update_carts_updated_at
  BEFORE UPDATE ON carts
  FOR EACH ROW
  EXECUTE FUNCTION update_updated_at_column();

-- Apply trigger to products
DROP TRIGGER IF EXISTS update_products_updated_at ON products;
CREATE TRIGGER update_products_updated_at
  BEFORE UPDATE ON products
  FOR EACH ROW
  EXECUTE FUNCTION update_updated_at_column();

-- ============================================
-- SAMPLE DATA
-- ============================================

-- Insert test users/customers
INSERT INTO users ("userId", email, name, phone) VALUES
  ('USER_001', 'john.doe@example.com', 'John Doe', '+1234567890'),
  ('USER_002', 'jane.smith@example.com', 'Jane Smith', '+1234567891'),
  ('USER_003', 'bob.wilson@example.com', 'Bob Wilson', '+1234567892'),
  ('USER_004', 'alice.brown@example.com', 'Alice Brown', '+1234567893'),
  ('USER_005', 'charlie.davis@example.com', 'Charlie Davis', '+1234567894')
ON CONFLICT ("userId") DO NOTHING;

-- Insert test carts
INSERT INTO carts ("cartId", status) VALUES
  ('CART_001', 'available'),
  ('CART_002', 'available'),
  ('CART_003', 'available'),
  ('CART_004', 'available'),
  ('CART_005', 'available')
ON CONFLICT ("cartId") DO NOTHING;

-- Insert test products
INSERT INTO products ("productId", barcode, name, description, price, category, "stockQuantity", "imageUrl") VALUES
  ('PROD_001', '1234567890123', 'Apple iPhone 15', 'Latest iPhone with A17 chip', 999.99, 'Electronics', 50, 'https://example.com/iphone15.jpg'),
  ('PROD_002', '2345678901234', 'Samsung Galaxy S24', 'Premium Android smartphone', 899.99, 'Electronics', 45, 'https://example.com/galaxy.jpg'),
  ('PROD_003', '3456789012345', 'Coca Cola 500ml', 'Refreshing cola drink', 2.50, 'Beverages', 200, 'https://example.com/coke.jpg'),
  ('PROD_004', '4567890123456', 'Lays Chips Original', 'Classic potato chips', 3.99, 'Snacks', 150, 'https://example.com/lays.jpg'),
  ('PROD_005', '5678901234567', 'Whole Milk 1L', 'Fresh dairy milk', 4.50, 'Dairy', 100, 'https://example.com/milk.jpg'),
  ('PROD_006', '6789012345678', 'Bread Loaf White', 'Fresh baked bread', 2.99, 'Bakery', 80, 'https://example.com/bread.jpg'),
  ('PROD_007', '7890123456789', 'Orange Juice 1L', '100% pure orange juice', 5.99, 'Beverages', 75, 'https://example.com/oj.jpg'),
  ('PROD_008', '8901234567890', 'Chocolate Bar Dark', 'Premium dark chocolate', 1.99, 'Snacks', 120, 'https://example.com/choco.jpg'),
  ('PROD_009', '9012345678901', 'Eggs 12 Pack', 'Farm fresh eggs', 6.99, 'Dairy', 90, 'https://example.com/eggs.jpg'),
  ('PROD_010', '0123456789012', 'Banana 1kg', 'Fresh organic bananas', 3.50, 'Produce', 60, 'https://example.com/banana.jpg')
ON CONFLICT ("productId") DO NOTHING;

-- ============================================
-- VIEWS FOR ADMIN ANALYTICS
-- ============================================

-- View: Customer Purchase History
CREATE OR REPLACE VIEW customer_purchase_history AS
SELECT
  u."userId",
  u.name as customer_name,
  u.email,
  o."orderId",
  o.created_at as purchase_date,
  o."finalAmount" as total_paid,
  o."paymentMethod",
  o."orderStatus",
  COUNT(oi.id) as items_count
FROM users u
JOIN orders o ON u."userId" = o."userId"
LEFT JOIN order_items oi ON o."orderId" = oi."orderId"
GROUP BY u."userId", u.name, u.email, o."orderId", o.created_at, o."finalAmount", o."paymentMethod", o."orderStatus"
ORDER BY o.created_at DESC;

-- View: Product Purchase Summary
CREATE OR REPLACE VIEW product_purchase_summary AS
SELECT
  p."productId",
  p.name as product_name,
  p.category,
  p.price as current_price,
  COUNT(DISTINCT oi."orderId") as times_purchased,
  SUM(oi.quantity) as total_quantity_sold,
  SUM(oi."totalPrice") as total_revenue,
  AVG(oi."unitPrice") as avg_selling_price
FROM products p
LEFT JOIN order_items oi ON p."productId" = oi."productId"
GROUP BY p."productId", p.name, p.category, p.price
ORDER BY total_quantity_sold DESC NULLS LAST;

-- View: Customer Product Preferences (What each customer bought)
CREATE OR REPLACE VIEW customer_product_preferences AS
SELECT
  u."userId",
  u.name as customer_name,
  u.email,
  oi."productName",
  oi.category,
  SUM(oi.quantity) as total_purchased,
  SUM(oi."totalPrice") as total_spent,
  COUNT(DISTINCT o."orderId") as purchase_frequency,
  MAX(o.created_at) as last_purchased
FROM users u
JOIN orders o ON u."userId" = o."userId"
JOIN order_items oi ON o."orderId" = oi."orderId"
GROUP BY u."userId", u.name, u.email, oi."productName", oi.category
ORDER BY u.name, total_spent DESC;

-- View: Top Customers by Spending
CREATE OR REPLACE VIEW top_customers AS
SELECT
  u."userId",
  u.name,
  u.email,
  COUNT(DISTINCT o."orderId") as total_orders,
  SUM(o."finalAmount") as total_spent,
  AVG(o."finalAmount") as avg_order_value,
  MAX(o.created_at) as last_order_date
FROM users u
JOIN orders o ON u."userId" = o."userId"
WHERE o."orderStatus" = 'completed'
GROUP BY u."userId", u.name, u.email
ORDER BY total_spent DESC;

-- View: Category Popularity by Customer
CREATE OR REPLACE VIEW category_by_customer AS
SELECT
  u."userId",
  u.name as customer_name,
  oi.category,
  COUNT(*) as items_bought,
  SUM(oi."totalPrice") as amount_spent
FROM users u
JOIN orders o ON u."userId" = o."userId"
JOIN order_items oi ON o."orderId" = oi."orderId"
GROUP BY u."userId", u.name, oi.category
ORDER BY u.name, amount_spent DESC;

-- ============================================
-- VERIFICATION
-- ============================================

SELECT 'Setup Complete! Summary:' as message
UNION ALL
SELECT '========================' as message
UNION ALL
SELECT CONCAT('Users: ', COUNT(*)) FROM users
UNION ALL
SELECT CONCAT('Carts: ', COUNT(*)) FROM carts
UNION ALL
SELECT CONCAT('Products: ', COUNT(*)) FROM products
UNION ALL
SELECT CONCAT('Shopping Sessions: ', COUNT(*)) FROM shopping_sessions
UNION ALL
SELECT CONCAT('Orders: ', COUNT(*)) FROM orders;

-- Show sample queries you can use:
SELECT 'Sample Admin Queries:' as message
UNION ALL
SELECT '1. View all customer purchases: SELECT * FROM customer_purchase_history;'
UNION ALL
SELECT '2. View product sales: SELECT * FROM product_purchase_summary;'
UNION ALL
SELECT '3. View customer preferences: SELECT * FROM customer_product_preferences;'
UNION ALL
SELECT '4. View top customers: SELECT * FROM top_customers;'
UNION ALL
SELECT '5. View category preferences: SELECT * FROM category_by_customer;';

