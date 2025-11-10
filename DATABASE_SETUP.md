# Database Setup for SmartCart

## Required Tables

You need to create these tables in your Supabase database:

### 1. Create `carts` Table

```sql
-- Create carts table
CREATE TABLE IF NOT EXISTS carts (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  "cartId" TEXT UNIQUE NOT NULL,
  status TEXT DEFAULT 'available',
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create index for faster lookups
CREATE INDEX idx_carts_cartId ON carts("cartId");
CREATE INDEX idx_carts_status ON carts(status);
```

### 2. Create `shopping_sessions` Table

```sql
-- Create shopping_sessions table
CREATE TABLE IF NOT EXISTS shopping_sessions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  "sessionId" TEXT UNIQUE NOT NULL,
  "cartId" TEXT NOT NULL,
  "userId" TEXT NOT NULL,
  status TEXT DEFAULT 'active',
  "startedAt" BIGINT NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  FOREIGN KEY ("cartId") REFERENCES carts("cartId")
);

-- Create indexes
CREATE INDEX idx_sessions_sessionId ON shopping_sessions("sessionId");
CREATE INDEX idx_sessions_cartId ON shopping_sessions("cartId");
CREATE INDEX idx_sessions_userId ON shopping_sessions("userId");
```

### 3. Create `products` Table

```sql
-- Create products table
CREATE TABLE IF NOT EXISTS products (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  "productId" TEXT UNIQUE NOT NULL,
  barcode TEXT UNIQUE NOT NULL,
  name TEXT NOT NULL,
  price DECIMAL(10,2) NOT NULL,
  category TEXT,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create indexes
CREATE INDEX idx_products_barcode ON products(barcode);
CREATE INDEX idx_products_productId ON products("productId");
```

### 4. Create `session_items` Table

```sql
-- Create session_items table
CREATE TABLE IF NOT EXISTS session_items (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  "itemId" TEXT UNIQUE NOT NULL,
  "sessionId" TEXT NOT NULL,
  "productId" TEXT NOT NULL,
  barcode TEXT NOT NULL,
  quantity INTEGER NOT NULL DEFAULT 1,
  "unitPrice" DECIMAL(10,2) NOT NULL,
  "totalPrice" DECIMAL(10,2) NOT NULL,
  "scannedBy" TEXT DEFAULT 'customer',
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  FOREIGN KEY ("sessionId") REFERENCES shopping_sessions("sessionId"),
  FOREIGN KEY ("productId") REFERENCES products("productId")
);

-- Create indexes
CREATE INDEX idx_session_items_sessionId ON session_items("sessionId");
CREATE INDEX idx_session_items_itemId ON session_items("itemId");
```

## Insert Test Data

### Insert Test Cart

```sql
-- Insert a test cart that the app will use
INSERT INTO carts ("cartId", status)
VALUES ('CART_001', 'available')
ON CONFLICT ("cartId") DO NOTHING;

-- Insert a few more test carts
INSERT INTO carts ("cartId", status)
VALUES 
  ('CART_002', 'available'),
  ('CART_003', 'available'),
  ('CART_004', 'available')
ON CONFLICT ("cartId") DO NOTHING;
```

### Insert Test Products

```sql
-- Insert some test products
INSERT INTO products ("productId", barcode, name, price, category)
VALUES 
  ('PROD_001', '1234567890123', 'Apple iPhone 15', 999.99, 'Electronics'),
  ('PROD_002', '2345678901234', 'Samsung Galaxy S24', 899.99, 'Electronics'),
  ('PROD_003', '3456789012345', 'Coca Cola 500ml', 2.50, 'Beverages'),
  ('PROD_004', '4567890123456', 'Lays Chips', 3.99, 'Snacks'),
  ('PROD_005', '5678901234567', 'Milk 1L', 4.50, 'Dairy')
ON CONFLICT ("productId") DO NOTHING;
```

## How to Run These in Supabase

### Method 1: Using Supabase SQL Editor (Recommended)

1. Go to your Supabase Dashboard: https://app.supabase.com
2. Select your project
3. Click **SQL Editor** in the left sidebar
4. Click **New Query**
5. Copy and paste the CREATE TABLE statements
6. Click **Run** (or press Ctrl+Enter)
7. Repeat for INSERT statements

### Method 2: Using Supabase Table Editor

1. Go to **Table Editor** in Supabase Dashboard
2. Click **New Table**
3. Manually create each table with the columns specified above
4. Use the **Insert Row** button to add test data

## Verify Setup

After creating tables and inserting data, verify:

```sql
-- Check if cart exists
SELECT * FROM carts WHERE "cartId" = 'CART_001';

-- Check products
SELECT * FROM products;

-- Check all carts
SELECT * FROM carts;
```

You should see:
- At least 1 cart with cartId = 'CART_001' and status = 'available'
- At least 5 products

## Enable Row Level Security (Optional but Recommended)

For production, enable RLS:

```sql
-- Enable RLS
ALTER TABLE carts ENABLE ROW LEVEL SECURITY;
ALTER TABLE shopping_sessions ENABLE ROW LEVEL SECURITY;
ALTER TABLE products ENABLE ROW LEVEL SECURITY;
ALTER TABLE session_items ENABLE ROW LEVEL SECURITY;

-- Create policies (allow all for now, customize later)
CREATE POLICY "Allow all operations on carts" ON carts FOR ALL USING (true);
CREATE POLICY "Allow all operations on sessions" ON shopping_sessions FOR ALL USING (true);
CREATE POLICY "Allow all operations on products" ON products FOR ALL USING (true);
CREATE POLICY "Allow all operations on items" ON session_items FOR ALL USING (true);
```

## Test the Setup

After running all the SQL above:

1. Rebuild your Android app
2. Run the app
3. Click "SCAN QR CODE" button
4. You should see:
   - Loading indicator
   - Success navigation to the next screen
   - OR a clear error message if something is wrong

## Troubleshooting

### Error: "Cart not found"
**Solution:** Make sure you inserted the cart with cartId = 'CART_001'
```sql
SELECT * FROM carts WHERE "cartId" = 'CART_001';
```

### Error: "relation does not exist"
**Solution:** Tables weren't created. Run the CREATE TABLE statements again.

### Error: "permission denied"
**Solution:** Check RLS policies or disable RLS for testing:
```sql
ALTER TABLE carts DISABLE ROW LEVEL SECURITY;
ALTER TABLE shopping_sessions DISABLE ROW LEVEL SECURITY;
ALTER TABLE products DISABLE ROW LEVEL SECURITY;
ALTER TABLE session_items DISABLE ROW LEVEL SECURITY;
```

## Next Steps

After setup works:
1. Test scanning with different cart IDs
2. Add more products to the database
3. Implement actual QR code scanning (replace the button with camera scan)
4. Customize the cart IDs to match your physical QR codes

