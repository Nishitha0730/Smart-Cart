# Serialization Error Fix - "Collections of Different Element Types" ✅

## Error Message
```
Serializing collections of different element types is not yet supported. 
Selected serializers: [kotlin.String, kotlin.Long, kotlin.Double]
```

## Problem
When clicking "Pay Now", the `completeCheckout()` function was trying to serialize maps with mixed types:

```kotlin
// ❌ BEFORE - Mixed types in map
setBody(mapOf(
    "status" to "completed",        // String
    "completedAt" to System.currentTimeMillis(),  // Long
    "totalAmount" to totalAmount    // Double
))
```

Kotlin's serialization library doesn't support heterogeneous maps (maps with different value types) without explicit type handling.

## Solution
Created properly typed data classes for all PATCH request bodies and replaced all `mapOf()` calls with these typed objects.

### New Data Classes Added

```kotlin
@Serializable
data class CartStatusUpdate(
    val status: String
)

@Serializable
data class SessionCompletionUpdate(
    val status: String,
    val completedAt: Long,
    val totalAmount: Double
)

@Serializable
data class ItemQuantityUpdate(
    val quantity: Int,
    val totalPrice: Double
)
```

### Changes Made

#### 1. Fixed `completeCheckout()` - Session Update
```kotlin
// ✅ AFTER - Typed object
setBody(SessionCompletionUpdate(
    status = "completed",
    completedAt = System.currentTimeMillis(),
    totalAmount = totalAmount
))
```

#### 2. Fixed `completeCheckout()` - Cart Release
```kotlin
// ✅ AFTER - Typed object
setBody(CartStatusUpdate(status = "available"))
```

#### 3. Fixed `startShoppingSession()` - Cart In Use
```kotlin
// ✅ AFTER - Typed object
setBody(CartStatusUpdate(status = "in_use"))
```

#### 4. Fixed `addItemToCart()` - Update Quantity
```kotlin
// ✅ AFTER - Typed object
setBody(ItemQuantityUpdate(quantity = newQuantity, totalPrice = newTotal))
```

#### 5. Fixed `updateItemQuantity()` - Update Quantity
```kotlin
// ✅ AFTER - Typed object
setBody(ItemQuantityUpdate(quantity = newQuantity, totalPrice = newTotal))
```

## Why This Works

Kotlin's `kotlinx.serialization` library needs to know the exact structure of the data at compile time. When you use:
- **Typed data classes**: ✅ Serializer knows exact types
- **Maps with mixed types**: ❌ Serializer can't determine type structure

The `@Serializable` annotation generates serializers that know how to convert:
- `CartStatusUpdate` → `{"status":"available"}`
- `SessionCompletionUpdate` → `{"status":"completed","completedAt":1234567890,"totalAmount":185.0}`
- `ItemQuantityUpdate` → `{"quantity":2,"totalPrice":15.98}`

## Testing

Now when you click "Pay Now":
1. ✅ Order is created
2. ✅ Session is marked complete
3. ✅ Cart is released (status = "available")
4. ✅ No serialization errors!

## Files Modified
- `SupabaseManager.kt` - Added 3 new data classes and updated 5 PATCH requests

---
**Status**: ✅ FIXED - Serialization now works correctly with properly typed objects

