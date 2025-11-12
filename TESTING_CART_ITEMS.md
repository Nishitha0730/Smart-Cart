# Testing Guide - Cart Items Not Showing Issue

## What to Check Now

### Step 1: Build and Install the App
```bash
# Clean and rebuild
gradlew clean
gradlew assembleDebug
```

### Step 2: Run the App and Check Logcat

Open Android Studio Logcat and filter by "SupabaseManager" OR "CartScreen"

### Step 3: Test the Flow

1. **Open the app**
2. **Scan cart QR code** (e.g., CART_001)
3. **Go to Home screen**
4. **Click "Add to Cart" on a product**
5. **Watch Logcat for these messages:**

```
Expected Logs:
==============

✅ When you click "Add to Cart":
SupabaseManager: 🛒 addItemToCart called - barcode: 1234567890123, sessionId: SESSION_XXX
SupabaseManager: ✅ Found product: Apple iPhone 15
SupabaseManager: Adding new item to cart
SupabaseManager: ✅ Added new item: Apple iPhone 15
SupabaseManager: Loading items for session: SESSION_XXX
SupabaseManager: ✅ Loaded 1 items for session SESSION_XXX

✅ When you open Cart screen:
CartScreen: 🔄 Session changed: SESSION_XXX
CartScreen: Loading items for session: SESSION_XXX
SupabaseManager: Loading items for session: SESSION_XXX
SupabaseManager: ✅ Loaded 1 items for session SESSION_XXX
CartScreen: 📦 Cart items changed: 1 items
CartScreen:   - 1234567890123: qty 1
```

### Step 4: What to Look For

#### ❌ **If you see this:**
```
SupabaseManager: ✅ Loaded 0 items for session SESSION_XXX
CartScreen: 📦 Cart items changed: 0 items
```

**Problem**: Items not in database
**Solutions**:
- Check session_items table in Supabase
- Verify RLS policies allow INSERT
- Check network connection

#### ❌ **If items load but don't display:**
```
SupabaseManager: ✅ Loaded 3 items
CartScreen: 📦 Cart items changed: 0 items  <- MISMATCH!
```

**Problem**: State flow not updating
**Solution**: Check StateFlow observation

#### ❌ **If you see errors:**
```
SupabaseManager: ❌ Failed to add item to cart
```

**Problem**: Network or permission issue
**Solution**: Check error message details

### Step 5: Manual Database Check

Open Supabase SQL Editor and run:

```sql
-- Check if items are being added
SELECT * FROM session_items 
ORDER BY created_at DESC 
LIMIT 10;
```

**Expected Result:**
```
itemId | sessionId | barcode | quantity | ...
-------|-----------|---------|----------|----
XXX    | SESSION_1 | 123...  | 1        | ...
```

If items exist in database but not showing in app, it's a state sync issue.
If items don't exist in database, it's an INSERT issue.

### Step 6: Force Refresh Test

In Cart screen, try:
1. Navigate away (to Home)
2. Navigate back to Cart
3. Check if items appear

If they appear after navigation, it's a refresh trigger issue.

## Common Issues & Solutions

### Issue 1: "Added to cart!" shows but no items in database
**Cause**: POST request failing silently
**Check**: Logcat for "Failed to add item" errors
**Fix**: Check RLS policies, API keys

### Issue 2: Items in database but not showing in app
**Cause**: State not refreshing
**Check**: Logcat shows "Loaded X items" but CartScreen shows 0
**Fix**: Ensure loadSessionItems() is being called

### Issue 3: Session is null
**Cause**: QR scan didn't create session
**Check**: Navigate to ScannerScreen and verify session creation
**Fix**: Make sure startShoppingSession() completed successfully

### Issue 4: Wrong session ID
**Cause**: Multiple sessions, loading wrong one
**Check**: Compare sessionId in logs vs database
**Fix**: Ensure only one active session per user

## Debug Commands

### Check Active Session
```kotlin
// In HomeScreen or CartScreen
Log.d("DEBUG", "Current session: ${currentSession?.sessionId}")
Log.d("DEBUG", "Cart items count: ${cartItems.size}")
```

### Force Reload
```kotlin
// Add a refresh button temporarily
Button(onClick = {
    scope.launch {
        currentSession?.let { SupabaseManager.loadSessionItems(it.sessionId) }
    }
}) {
    Text("Force Refresh")
}
```

### Check State Flow
```kotlin
// In SupabaseManager, after loadSessionItems
Log.d("SupabaseManager", "_cartItems.value size: ${_cartItems.value.size}")
Log.d("SupabaseManager", "_cartItems current: ${_cartItems.value}")
```

## Expected Flow Diagram

```
User clicks "Add to Cart"
        ↓
addItemToCart(barcode, sessionId) called
        ↓
Product found in database
        ↓
Insert into session_items table
        ↓
loadSessionItems(sessionId) called
        ↓
GET /session_items?sessionId=eq.XXX
        ↓
_cartItems.value = [item1, item2, ...]
        ↓
CartScreen.cartItems updates (StateFlow)
        ↓
UI recomposes
        ↓
Items displayed! ✅
```

## If All Else Fails

### Nuclear Option: Clear Everything
```sql
-- In Supabase SQL Editor
DELETE FROM session_items;
DELETE FROM shopping_sessions;
UPDATE carts SET status = 'available';
```

Then:
1. Restart app
2. Scan cart again
3. Add items
4. Check cart

---

## Checklist Before Reporting Issue

- [ ] Rebuilt the app after latest changes
- [ ] Checked Logcat for error messages
- [ ] Verified items exist in session_items table
- [ ] Confirmed session is active
- [ ] Tried navigating away and back
- [ ] Checked RLS policies in Supabase
- [ ] Verified API keys are correct
- [ ] Tested with fresh session (new cart scan)

---

**Run the app with Logcat open and send me the logs!**

