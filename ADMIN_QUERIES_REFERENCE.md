# 📊 Admin Dashboard - Quick SQL Queries Reference

## Most Useful Queries for Your Admin Panel

### 1. View All Customer Purchases (Most Common)
```sql
SELECT 
  u.name as customer,
  u.email,
  oi."productName" as product,
  oi.category,
  oi.quantity,
  oi."totalPrice" as spent,
  o.created_at as date
FROM users u
JOIN orders o ON u."userId" = o."userId"
JOIN order_items oi ON o."orderId" = oi."orderId"
ORDER BY o.created_at DESC
LIMIT 100;
```

---

### 2. Search What Specific Customer Bought
```sql
-- Replace 'John Doe' with customer name
SELECT 
  oi."productName",
  oi.category,
  SUM(oi.quantity) as total_qty,
  SUM(oi."totalPrice") as total_spent,
  COUNT(DISTINCT o."orderId") as times_ordered,
  MAX(o.created_at) as last_purchase
FROM users u
JOIN orders o ON u."userId" = o."userId"
JOIN order_items oi ON o."orderId" = oi."orderId"
WHERE u.name LIKE '%John Doe%'
GROUP BY oi."productName", oi.category
ORDER BY total_spent DESC;
```

---

### 3. Top 10 Best Customers
```sql
SELECT 
  u.name,
  u.email,
  COUNT(DISTINCT o."orderId") as total_orders,
  SUM(o."finalAmount") as total_spent,
  AVG(o."finalAmount") as avg_order_value,
  MAX(o.created_at) as last_order
FROM users u
JOIN orders o ON u."userId" = o."userId"
WHERE o."orderStatus" = 'completed'
GROUP BY u.name, u.email
ORDER BY total_spent DESC
LIMIT 10;
```

---

### 4. Top 10 Best-Selling Products
```sql
SELECT 
  oi."productName",
  oi.category,
  COUNT(DISTINCT oi."orderId") as times_ordered,
  SUM(oi.quantity) as units_sold,
  SUM(oi."totalPrice") as revenue,
  AVG(oi."unitPrice") as avg_price
FROM order_items oi
GROUP BY oi."productName", oi.category
ORDER BY units_sold DESC
LIMIT 10;
```

---

### 5. Sales by Category
```sql
SELECT 
  oi.category,
  COUNT(DISTINCT oi."orderId") as orders,
  SUM(oi.quantity) as items_sold,
  SUM(oi."totalPrice") as revenue
FROM order_items oi
GROUP BY oi.category
ORDER BY revenue DESC;
```

---

### 6. Today's Sales
```sql
SELECT 
  u.name as customer,
  o."orderId",
  o."finalAmount" as total,
  o."paymentMethod",
  o.created_at as time
FROM orders o
JOIN users u ON o."userId" = u."userId"
WHERE DATE(o.created_at) = CURRENT_DATE
ORDER BY o.created_at DESC;
```

---

### 7. This Month's Revenue
```sql
SELECT 
  DATE_TRUNC('day', o.created_at) as date,
  COUNT(*) as orders,
  SUM(o."finalAmount") as revenue
FROM orders o
WHERE o.created_at >= DATE_TRUNC('month', CURRENT_DATE)
  AND o."orderStatus" = 'completed'
GROUP BY DATE_TRUNC('day', o.created_at)
ORDER BY date DESC;
```

---

### 8. Customer Purchase Frequency
```sql
SELECT 
  u.name,
  u.email,
  COUNT(o."orderId") as purchase_count,
  MIN(o.created_at) as first_purchase,
  MAX(o.created_at) as last_purchase,
  EXTRACT(DAY FROM (MAX(o.created_at) - MIN(o.created_at))) as days_active
FROM users u
JOIN orders o ON u."userId" = o."userId"
GROUP BY u.name, u.email
HAVING COUNT(o."orderId") > 1
ORDER BY purchase_count DESC;
```

---

### 9. Products Bought Together
```sql
-- Find products frequently bought with iPhone
SELECT 
  oi2."productName",
  COUNT(*) as times_bought_together
FROM order_items oi1
JOIN order_items oi2 ON oi1."orderId" = oi2."orderId"
WHERE oi1."productName" LIKE '%iPhone%'
  AND oi2."productName" NOT LIKE '%iPhone%'
GROUP BY oi2."productName"
ORDER BY times_bought_together DESC
LIMIT 10;
```

---

### 10. Inactive Customers (Haven't Ordered in 30 Days)
```sql
SELECT 
  u.name,
  u.email,
  MAX(o.created_at) as last_order,
  CURRENT_DATE - MAX(o.created_at)::date as days_since_last_order
FROM users u
LEFT JOIN orders o ON u."userId" = o."userId"
GROUP BY u.name, u.email
HAVING MAX(o.created_at) < CURRENT_DATE - INTERVAL '30 days'
   OR MAX(o.created_at) IS NULL
ORDER BY last_order DESC NULLS LAST;
```

---

### 11. Average Order Value by Customer
```sql
SELECT 
  u.name,
  COUNT(o."orderId") as orders,
  AVG(o."finalAmount") as avg_order_value,
  MIN(o."finalAmount") as min_order,
  MAX(o."finalAmount") as max_order
FROM users u
JOIN orders o ON u."userId" = o."userId"
GROUP BY u.name
ORDER BY avg_order_value DESC;
```

---

### 12. Category Preferences by Customer
```sql
SELECT 
  u.name,
  oi.category,
  COUNT(*) as items,
  SUM(oi."totalPrice") as spent
FROM users u
JOIN orders o ON u."userId" = o."userId"
JOIN order_items oi ON o."orderId" = oi."orderId"
GROUP BY u.name, oi.category
ORDER BY u.name, spent DESC;
```

---

### 13. Revenue by Payment Method
```sql
SELECT 
  o."paymentMethod",
  COUNT(*) as transactions,
  SUM(o."finalAmount") as total_revenue,
  AVG(o."finalAmount") as avg_transaction
FROM orders o
WHERE o."paymentStatus" = 'completed'
GROUP BY o."paymentMethod"
ORDER BY total_revenue DESC;
```

---

### 14. Customer Lifetime Value (CLV)
```sql
SELECT 
  u.name,
  u.email,
  COUNT(o."orderId") as total_orders,
  SUM(o."finalAmount") as lifetime_value,
  MIN(o.created_at) as customer_since,
  EXTRACT(DAY FROM (CURRENT_DATE - MIN(o.created_at)::date)) as days_as_customer,
  SUM(o."finalAmount") / NULLIF(EXTRACT(DAY FROM (CURRENT_DATE - MIN(o.created_at)::date)), 0) as revenue_per_day
FROM users u
JOIN orders o ON u."userId" = o."userId"
GROUP BY u.name, u.email
ORDER BY lifetime_value DESC
LIMIT 20;
```

---

### 15. Stock Movement (If you track inventory)
```sql
SELECT 
  p.name,
  p.category,
  p."stockQuantity" as current_stock,
  COALESCE(SUM(oi.quantity), 0) as total_sold,
  p."stockQuantity" - COALESCE(SUM(oi.quantity), 0) as remaining
FROM products p
LEFT JOIN order_items oi ON p."productId" = oi."productId"
GROUP BY p.name, p.category, p."stockQuantity"
ORDER BY total_sold DESC;
```

---

## 🎯 Pro Tips for Admin Dashboard

### For Daily Operations:
- Use Query #6 (Today's Sales)
- Use Query #1 (All Customer Purchases)

### For Business Insights:
- Use Query #3 (Top Customers)
- Use Query #4 (Best-Selling Products)
- Use Query #5 (Sales by Category)

### For Marketing:
- Use Query #10 (Inactive Customers)
- Use Query #12 (Category Preferences)
- Use Query #9 (Products Bought Together)

### For Financial Reports:
- Use Query #7 (Monthly Revenue)
- Use Query #13 (Revenue by Payment Method)
- Use Query #14 (Customer Lifetime Value)

---

## 📱 How to Use These in Your Admin App

### Option 1: Direct Supabase Queries
```kotlin
// In your admin app
suspend fun getTopCustomers(): List<CustomerStats> {
    return supabase.from("top_customers")
        .select()
        .decodeList<CustomerStats>()
}
```

### Option 2: Create API Endpoints
```kotlin
// Create a backend API
@GET("/api/admin/top-customers")
suspend fun getTopCustomers(): List<TopCustomer> {
    // Run SQL query
    // Return results
}
```

### Option 3: Use Supabase Dashboard
1. Go to Supabase project
2. Click "SQL Editor"
3. Paste any query above
4. Click "Run"
5. View results!

---

**Save this file for quick reference when building your admin dashboard!** 📊

