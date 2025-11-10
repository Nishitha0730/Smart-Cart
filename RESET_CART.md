# Reset Cart Status

## Option 1: Reset CART_001 to Available

If you want to use CART_001 again, run this in Supabase SQL Editor:

```sql
-- Reset CART_001 status to available
UPDATE carts 
SET status = 'available' 
WHERE "cartId" = 'CART_001';
```

After running this, CART_001 will be available for use again.

## Option 2: Use CART_002 (Already Done!)

I've already updated the app to use **CART_002** instead of CART_001.

The app now scans with CART_002, which should have status='available' in your database.

## Verify Cart Status

Check which carts are available:

```sql
-- See all carts and their status
SELECT "cartId", status, created_at 
FROM carts 
ORDER BY "cartId";
```

You should see:
```
CART_001 | in_use    | ...
CART_002 | available | ...
CART_003 | available | ...
CART_004 | available | ...
```

## Check Active Sessions

See which cart is being used:

```sql
-- See active shopping sessions
SELECT "sessionId", "cartId", "userId", status, "startedAt"
FROM shopping_sessions
WHERE status = 'active'
ORDER BY "startedAt" DESC;
```

## Complete a Session and Free the Cart

If you want to end the session and free CART_001:

```sql
-- Complete the active session for CART_001
UPDATE shopping_sessions
SET status = 'completed',
    "completedAt" = EXTRACT(EPOCH FROM NOW()) * 1000
WHERE "cartId" = 'CART_001' 
  AND status = 'active';

-- Then free up CART_001
UPDATE carts
SET status = 'available'
WHERE "cartId" = 'CART_001';
```

## Quick Reset All Carts

To reset all carts to available:

```sql
-- Mark all sessions as completed
UPDATE shopping_sessions
SET status = 'completed',
    "completedAt" = EXTRACT(EPOCH FROM NOW()) * 1000
WHERE status = 'active';

-- Mark all carts as available
UPDATE carts
SET status = 'available';
```

---

## What I Changed in the App

**File:** `ScannerScreen.kt`

**Before:**
```kotlin
val testCartId = "CART_001"  // This cart is in use
```

**After:**
```kotlin
val testCartId = "CART_002"  // Using CART_002 since CART_001 is in use
```

---

## Testing

1. **Rebuild the app:**
   ```
   Build → Clean Project
   Build → Rebuild Project
   Run
   ```

2. **Test with CART_002:**
   - Navigate to Scanner screen
   - Click "SCAN QR CODE"
   - Should use CART_002 now
   - Should navigate to Success screen ✅

3. **Verify in Logcat:**
   ```
   Filter: SupabaseManager
   
   Should see:
   REQUEST: .../carts?cartId=eq.CART_002
   RESPONSE: 200 OK
   [{"cartId":"CART_002","status":"available"...}]
   ```

---

## Summary

✅ **App updated to use CART_002**
✅ CART_002 should be available in your database
✅ You can now start a new shopping session
✅ Or reset CART_001 using the SQL above

**Rebuild and run to test with CART_002!** 🚀

