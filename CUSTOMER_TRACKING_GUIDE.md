# Customer Purchase Tracking System - Complete Guide

## Overview
The SmartCart system now includes comprehensive customer tracking that allows admins to see:
- Which customers purchased which products
- Purchase history for each customer
- Product popularity and revenue
- Customer spending patterns
- Category preferences

---

## Database Structure

### Core Tables

#### 1. **users** - Customer Information
Stores all customer data for the shopping app.

| Column | Type | Description |
|--------|------|-------------|
| userId | TEXT (PK) | Unique customer ID |
| email | TEXT | Customer email (unique) |
| name | TEXT | Customer name |
| phone | TEXT | Phone number (optional) |
| created_at | TIMESTAMP | Account creation date |
| last_login | TIMESTAMP | Last login timestamp |

**Example:**
```sql
userId: USER_001
email: john.doe@example.com
name: John Doe
phone: +1234567890
```

#### 2. **carts** - Shopping Carts
Physical shopping carts in the store.

| Column | Type | Description |
|--------|------|-------------|
| cartId | TEXT (PK) | Cart identifier (e.g., CART_001) |
| status | TEXT | available, in_use, or maintenance |
| created_at | TIMESTAMP | When cart was added |

#### 3. **shopping_sessions** - Active Shopping Sessions
Links customers to carts and tracks their shopping.

| Column | Type | Description |
|--------|------|-------------|
| sessionId | TEXT (PK) | Unique session ID |
| cartId | TEXT (FK) | Which cart is being used |
| userId | TEXT (FK) | Which customer is shopping |
| status | TEXT | active, completed, or abandoned |
| startedAt | BIGINT | Session start time |
| completedAt | BIGINT | Session end time |
| totalAmount | DECIMAL | Total purchase amount |

**This links: Customer → Cart → Shopping Session**

#### 4. **products** - Product Catalog
All products available in the store.

| Column | Type | Description |
|--------|------|-------------|
| productId | TEXT (PK) | Unique product ID |
| barcode | TEXT | Scannable barcode |
| name | TEXT | Product name |
| description | TEXT | Product description |
| price | DECIMAL | Product price |
| category | TEXT | Product category |
| stockQuantity | INTEGER | Available stock |
| imageUrl | TEXT | Product image URL |

#### 5. **session_items** - Items in Current Cart
Temporary table while customer is shopping.

| Column | Type | Description |
|--------|------|-------------|
| itemId | TEXT (PK) | Unique item ID |
| sessionId | TEXT (FK) | Which session |
| productId | TEXT (FK) | Which product |
| barcode | TEXT | Product barcode |
| quantity | INTEGER | How many items |
| unitPrice | DECIMAL | Price per unit |
| totalPrice | DECIMAL | Total for this item |
| scannedBy | TEXT | Who scanned (customer/staff) |

**Note:** These items are TEMPORARY and get deleted after checkout.

#### 6. **orders** - Completed Purchases ⭐
**PERMANENT RECORD** of all customer purchases.

| Column | Type | Description |
|--------|------|-------------|
| orderId | TEXT (PK) | Unique order ID |
| sessionId | TEXT (FK) | Which session generated this order |
| userId | TEXT (FK) | Which customer made purchase |
| totalAmount | DECIMAL | Total before discounts |
| discountAmount | DECIMAL | Total discount applied |
| finalAmount | DECIMAL | Final amount paid |
| paymentMethod | TEXT | PayHere, cash, card, etc. |
| paymentStatus | TEXT | completed, pending, failed, refunded |
| orderStatus | TEXT | processing, completed, cancelled |
| created_at | TIMESTAMP | When order was placed |

**This is the KEY table for tracking customer purchases!**

#### 7. **order_items** - What Was Purchased ⭐
**PERMANENT RECORD** of every product in every order.

| Column | Type | Description |
|--------|------|-------------|
| orderItemId | TEXT (PK) | Unique item ID |
| orderId | TEXT (FK) | Which order this belongs to |
| productId | TEXT (FK) | Which product was bought |
| productName | TEXT | Product name (saved for history) |
| barcode | TEXT | Product barcode |
| quantity | INTEGER | How many were bought |
| unitPrice | DECIMAL | Price per unit at time of purchase |
| totalPrice | DECIMAL | Total for this item |
| category | TEXT | Product category |

**Why save productName?** 
- If product is deleted later, we still have the name in order history
- Prices can change over time, we preserve historical prices

---

## How Purchase Tracking Works

### The Complete Flow:

```
1. Customer Signs In
   ↓
   User record created in 'users' table
   (userId: USER_001, name: "John Doe")

2. Customer Scans Cart QR Code
   ↓
   Shopping session created in 'shopping_sessions' table
   (links: USER_001 → CART_001 → SESSION_001)
   Cart status changed to "in_use"

3. Customer Scans Products
   ↓
   Items added to 'session_items' table
   (temporary items in cart)

4. Customer Goes to Checkout & Pays 💰
   ↓
   App calls SupabaseManager.completeCheckout()
   
   This creates:
   ✅ Order record in 'orders' table
   ✅ Order items in 'order_items' table (one per product)
   ✅ Updates session status to "completed"
   ✅ Changes cart status to "available"

5. Admin Can Now See Purchase History! 📊
```

---

## Admin Analytics Queries

### Pre-built Views Available:

#### 1. **customer_purchase_history**
See all purchases by all customers:

```sql
SELECT * FROM customer_purchase_history;
```

**Shows:**
- Customer name and email
- Order ID
- Purchase date
- Total paid
- Payment method
- Number of items

#### 2. **product_purchase_summary**
See which products are selling:

```sql
SELECT * FROM product_purchase_summary;
```

**Shows:**
- Product name and category
- Times purchased
- Total quantity sold
- Total revenue
- Average selling price

#### 3. **customer_product_preferences** ⭐
**MOST IMPORTANT** - See what each customer bought:

```sql
SELECT * FROM customer_product_preferences;
```

**Shows:**
- Customer name
- Product name
- Category
- Total quantity purchased
- Total spent on that product
- Purchase frequency
- Last purchase date

**Example output:**
```
John Doe | iPhone 15 | Electronics | Qty: 2 | Spent: $1,999.98 | Last: 2025-11-10
John Doe | Coca Cola | Beverages  | Qty: 5 | Spent: $12.50    | Last: 2025-11-09
Jane Smith | Milk 1L | Dairy       | Qty: 3 | Spent: $13.50    | Last: 2025-11-10
```

#### 4. **top_customers**
See your best customers:

```sql
SELECT * FROM top_customers;
```

**Shows:**
- Customer name
- Total orders
- Total spent
- Average order value
- Last order date

#### 5. **category_by_customer**
See which categories each customer prefers:

```sql
SELECT * FROM category_by_customer;
```

**Shows:**
- Customer name
- Category (Electronics, Beverages, etc.)
- Items bought in that category
- Amount spent in that category

---

## Custom Queries Examples

### Get All Products Purchased by Specific Customer:

```sql
SELECT 
  u.name as customer_name,
  oi."productName",
  oi.quantity,
  oi."totalPrice",
  o.created_at as purchase_date
FROM users u
JOIN orders o ON u."userId" = o."userId"
JOIN order_items oi ON o."orderId" = oi."orderId"
WHERE u.email = 'john.doe@example.com'
ORDER BY o.created_at DESC;
```

### Get Customer's Favorite Products:

```sql
SELECT 
  oi."productName",
  SUM(oi.quantity) as total_bought,
  COUNT(DISTINCT o."orderId") as times_ordered
FROM users u
JOIN orders o ON u."userId" = o."userId"
JOIN order_items oi ON o."orderId" = oi."orderId"
WHERE u."userId" = 'USER_001'
GROUP BY oi."productName"
ORDER BY total_bought DESC
LIMIT 10;
```

### Get Monthly Sales by Customer:

```sql
SELECT 
  u.name,
  DATE_TRUNC('month', o.created_at) as month,
  COUNT(o."orderId") as orders,
  SUM(o."finalAmount") as total_spent
FROM users u
JOIN orders o ON u."userId" = o."userId"
WHERE o."orderStatus" = 'completed'
GROUP BY u.name, DATE_TRUNC('month', o.created_at)
ORDER BY month DESC, total_spent DESC;
```

### Find Customers Who Bought Specific Product:

```sql
SELECT DISTINCT
  u.name,
  u.email,
  COUNT(DISTINCT o."orderId") as times_purchased,
  SUM(oi.quantity) as total_quantity
FROM users u
JOIN orders o ON u."userId" = o."userId"
JOIN order_items oi ON o."orderId" = oi."orderId"
WHERE oi."productName" LIKE '%iPhone%'
GROUP BY u.name, u.email
ORDER BY total_quantity DESC;
```

---

## App Code - How It Works

### In SupabaseManager.kt:

When customer completes checkout:

```kotlin
suspend fun completeCheckout(...): Result<Order> {
    // 1. Create order record
    val order = Order(...)
    http.post("/orders") { setBody(order) }
    
    // 2. Save each product to order_items ⭐
    for (item in _cartItems.value) {
        val product = getProductDetails(item.productId)
        val orderItem = OrderItem(
            orderId = order.orderId,
            productId = item.productId,
            productName = product.name,  // Save name!
            quantity = item.quantity,
            unitPrice = item.unitPrice,
            totalPrice = item.totalPrice,
            category = product.category
        )
        http.post("/order_items") { setBody(orderItem) }
    }
    
    // 3. Mark session complete
    // 4. Release cart
}
```

---

## Setup Instructions

### 1. Run the SQL Script

Copy and run `setup_complete.sql` in your Supabase SQL Editor.

This creates:
- All tables with proper relationships
- All indexes for performance
- Sample data (5 users, 5 carts, 10 products)
- All admin views for analytics

### 2. Add Row Level Security (RLS) Policies

In Supabase dashboard:

**For `users` table:**
```sql
-- Allow anyone to read users
CREATE POLICY "Users are viewable by everyone"
  ON users FOR SELECT
  USING (true);

-- Allow users to update their own data
CREATE POLICY "Users can update own data"
  ON users FOR UPDATE
  USING (auth.uid()::text = "userId");
```

**For `orders` and `order_items` tables:**
```sql
-- Allow anyone to insert orders (for checkout)
CREATE POLICY "Anyone can create orders"
  ON orders FOR INSERT
  WITH CHECK (true);

CREATE POLICY "Anyone can create order items"
  ON order_items FOR INSERT
  WITH CHECK (true);

-- Allow users to read their own orders
CREATE POLICY "Users can view own orders"
  ON orders FOR SELECT
  USING (auth.uid()::text = "userId");
```

### 3. Test the System

1. Build and run the app
2. Sign in as a test user
3. Scan cart CART_001
4. Add products
5. Complete checkout
6. Check database:

```sql
-- See the order
SELECT * FROM orders WHERE "userId" = 'USER_001';

-- See what was purchased
SELECT * FROM order_items 
WHERE "orderId" = (SELECT "orderId" FROM orders WHERE "userId" = 'USER_001' LIMIT 1);

-- See in analytics view
SELECT * FROM customer_product_preferences WHERE customer_name = 'John Doe';
```

---

## Benefits of This System

✅ **Complete Purchase History**
- Never lose customer purchase data
- Even if products are deleted, order history is preserved

✅ **Customer Insights**
- Know what each customer prefers
- Personalize recommendations
- Identify valuable customers

✅ **Business Analytics**
- Track product performance
- Identify trends
- Revenue analysis

✅ **Marketing Opportunities**
- Send targeted promotions
- Loyalty programs
- Reorder reminders

✅ **Compliance**
- Keep records for returns/refunds
- Tax documentation
- Audit trail

---

## Next Steps

1. **Build Admin Dashboard** (separate web app) to:
   - View customer purchase history
   - Generate reports
   - Export data
   - Manage customers

2. **Add Features:**
   - Email receipts with order details
   - Reorder previous purchases
   - Product recommendations based on history
   - Loyalty points system

3. **Analytics:**
   - Customer lifetime value (CLV)
   - Purchase frequency
   - Category analysis
   - Seasonal trends

---

**The system is now ready! Run `setup_complete.sql` in Supabase and start tracking customer purchases!** 🎉

