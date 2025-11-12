package com.example.smartcart

import android.util.Log
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import java.util.UUID

object SupabaseManager {

    // Credentials from BuildConfig (injected via build.gradle)
    private val baseUrl: String = BuildConfig.SUPABASE_URL
    private val apiKey: String = BuildConfig.SUPABASE_KEY

    init {
        Log.d("SupabaseManager", "Loaded from BuildConfig - URL: ${if (baseUrl.isBlank()) "EMPTY" else baseUrl}, Key length: ${apiKey.length}")
        if (baseUrl.isBlank() || apiKey.isBlank()) {
            Log.e("SupabaseManager", "⚠️ Supabase credentials are EMPTY in BuildConfig! Did you rebuild after adding to gradle.properties?")
        }
    }

    private var client: HttpClient? = null

    private fun ensureClient(): Boolean {
        if (client == null) {
            if (baseUrl.isBlank() || apiKey.isBlank()) {
                Log.w("SupabaseManager", "No Supabase URL or KEY provided; call SupabaseManager.init(url, key) or set BuildConfig fields")
                return false
            }
            client = HttpClient(Android) {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
                // Add logging to see what's being sent
                install(io.ktor.client.plugins.logging.Logging) {
                    logger = object : io.ktor.client.plugins.logging.Logger {
                        override fun log(message: String) {
                            Log.d("SupabaseHTTP", message)
                        }
                    }
                    level = io.ktor.client.plugins.logging.LogLevel.ALL
                }
            }
            Log.i("SupabaseManager", "Ktor HTTP client for Supabase initialized")
        }
        return true
    }

    fun init(supabaseUrl: String = baseUrl, supabaseKey: String = apiKey) {
        // allow explicit init with custom values
        if (client != null) {
            Log.i("SupabaseManager", "Client already initialized")
            return
        }
        if (supabaseUrl.isBlank() || supabaseKey.isBlank()) {
            Log.w("SupabaseManager", "Supabase init called with empty url/key - credentials not configured")
            return
        }
        // set Build-time values (we don't persist, just set up client)
        // Note: BuildConfig values remain the source of truth; this init allows runtime override only per-call.
        try {
            client = HttpClient(Android) {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
                // Add logging to see what's being sent
                install(io.ktor.client.plugins.logging.Logging) {
                    logger = object : io.ktor.client.plugins.logging.Logger {
                        override fun log(message: String) {
                            Log.d("SupabaseHTTP", message)
                        }
                    }
                    level = io.ktor.client.plugins.logging.LogLevel.ALL
                }
            }
            Log.i("SupabaseManager", "SupabaseManager initialized successfully with url: $supabaseUrl")
        } catch (e: Exception) {
            Log.e("SupabaseManager", "Failed to initialize HTTP client", e)
        }
    }

    fun isAvailable(): Boolean {
        val available = client != null && baseUrl.isNotBlank() && apiKey.isNotBlank()
        if (!available) {
            Log.d("SupabaseManager", "isAvailable=false, client=${client != null}, baseUrl.isNotBlank=${baseUrl.isNotBlank()}, apiKey.isNotBlank=${apiKey.isNotBlank()}")
        }
        return available
    }

    // Current active session
    private val _currentSession = MutableStateFlow<ShoppingSession?>(null)
    val currentSession: StateFlow<ShoppingSession?> = _currentSession.asStateFlow()

    // Cart items in current session
    private val _cartItems = MutableStateFlow<List<SessionItem>>(emptyList())
    val cartItems: StateFlow<List<SessionItem>> = _cartItems.asStateFlow()


    /**
     * Ensure user exists in database (create if not exists)
     */
    private suspend fun ensureUserExists(userId: String, userName: String = "Guest User"): Result<Unit> {
        if (!ensureClient()) return Result.failure(Exception("Supabase client not available"))
        val http = client!!

        return try {
            // Check if user exists
            val users: List<User> = http.get("${baseUrl.trimEnd('/')}/rest/v1/users") {
                url {
                    parameters.append("select", "*")
                    parameters.append("userId", "eq.$userId")
                }
                headers {
                    append("apikey", apiKey)
                    append("Authorization", "Bearer $apiKey")
                    append("Accept", "application/json")
                }
            }.body()

            if (users.isEmpty()) {
                // User doesn't exist, create it
                val newUser = User(
                    userId = userId,
                    email = "$userId@smartcart.local",
                    name = userName,
                    phone = null
                )

                http.post("${baseUrl.trimEnd('/')}/rest/v1/users") {
                    contentType(ContentType.Application.Json)
                    headers {
                        append("apikey", apiKey)
                        append("Authorization", "Bearer $apiKey")
                        append("Prefer", "return=representation")
                    }
                    setBody(newUser)
                }
                Log.i("SupabaseManager", "✅ Created new user: $userId")
            } else {
                Log.d("SupabaseManager", "ℹ️  User already exists: $userId")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w("SupabaseManager", "⚠️ Could not ensure user exists (continuing anyway): ${e.message}")
            // Don't fail the whole operation if user creation fails
            Result.success(Unit)
        }
    }

    /**
     * Start shopping session when QR code is scanned
     */
    suspend fun startShoppingSession(cartId: String, userId: String): Result<ShoppingSession> {
        if (!ensureClient()) return Result.failure(Exception("Supabase client not available"))
        val http = client!!

        // Log credentials before making request
        Log.d("SupabaseManager", "🔍 About to make request with:")
        Log.d("SupabaseManager", "   baseUrl: $baseUrl")
        Log.d("SupabaseManager", "   apiKey length: ${apiKey.length}")
        Log.d("SupabaseManager", "   apiKey first 20 chars: ${apiKey.take(20)}...")

        // Ensure user exists in database (if users table exists)
        ensureUserExists(userId)


        return try {
            // GET cart by cartId
            val carts: List<Cart> = http.get("${baseUrl.trimEnd('/')}/rest/v1/carts") {
                url {
                    parameters.append("select", "*")
                    parameters.append("cartId", "eq.$cartId")
                }
                // Apply headers inline to ensure they're added
                headers {
                    Log.d("SupabaseManager", "🔧 Inside headers block - adding headers now")
                    append("apikey", apiKey)
                    append("Authorization", "Bearer $apiKey")
                    append("Accept", "application/json")
                    append("Content-Type", "application/json")
                    append("Prefer", "return=representation")
                    Log.d("SupabaseManager", "✅ Headers added: apikey length = ${apiKey.length}")
                }
            }.body()

            val matching = carts.filter { it.cartId == cartId }
            if (matching.isEmpty()) return Result.failure(Exception("Cart not found"))
            val cart = matching.first()
            if (cart.status != "available") return Result.failure(Exception("Cart is currently in use"))

            val session = ShoppingSession(
                sessionId = UUID.randomUUID().toString(),
                cartId = cartId,
                userId = userId,
                status = "active",
                startedAt = System.currentTimeMillis()
            )

            Log.d("SupabaseManager", "🔄 Creating session in database: ${session.sessionId}")

            // Insert session - MUST succeed or items can't be added
            try {
                val response = http.post("${baseUrl.trimEnd('/')}/rest/v1/shopping_sessions") {
                    contentType(ContentType.Application.Json)
                    headers {
                        append("apikey", apiKey)
                        append("Authorization", "Bearer $apiKey")
                        append("Prefer", "return=representation")
                    }
                    setBody(session)
                }
                Log.i("SupabaseManager", "✅ Session created successfully in database")
            } catch (e: Exception) {
                Log.e("SupabaseManager", "❌ FAILED to create session in database", e)
                return Result.failure(Exception("Failed to create shopping session: ${e.message}", e))
            }

            // Mark cart as in use
            http.patch("${baseUrl.trimEnd('/')}/rest/v1/carts") {
                url { parameters.append("cartId", "eq.$cartId") }
                contentType(ContentType.Application.Json)
                headers {
                    append("apikey", apiKey)
                    append("Authorization", "Bearer $apiKey")
                    append("Prefer", "return=representation")
                }
                setBody(CartStatusUpdate(status = "in_use"))
            }

            _currentSession.value = session
            Log.i("SupabaseManager", "✅ Session set in local state: ${session.sessionId}")

            // Optionally load items (empty now)
            loadSessionItems(session.sessionId)

            Result.success(session)
        } catch (e: Exception) {
            Log.e("SupabaseManager", "Failed to start shopping session", e)
            val errorMsg = when {
                e.message?.contains("Permission denied", ignoreCase = true) == true ->
                    "Network permission denied. Check AndroidManifest.xml for INTERNET permission."
                e.message?.contains("Unable to resolve host", ignoreCase = true) == true ->
                    "Cannot connect to Supabase. Check your internet connection."
                e.message?.contains("timeout", ignoreCase = true) == true ->
                    "Connection timeout. Please check your internet connection."
                e.message?.contains("401") == true || e.message?.contains("403") == true ->
                    "Authentication failed. Check your Supabase API key."
                else -> e.message ?: "Failed to start session"
            }
            Result.failure(Exception(errorMsg, e))
        }
    }

    suspend fun loadSessionItems(sessionId: String) {
        if (!ensureClient()) return
        val http = client!!
        try {
            Log.d("SupabaseManager", "Loading items for session: $sessionId")
            val items: List<SessionItem> = http.get("${baseUrl.trimEnd('/')}/rest/v1/session_items") {
                url {
                    parameters.append("select", "*")
                    parameters.append("sessionId", "eq.$sessionId")
                }
                headers {
                    append("apikey", apiKey)
                    append("Authorization", "Bearer $apiKey")
                    append("Accept", "application/json")
                }
            }.body()
            _cartItems.value = items.filter { it.sessionId == sessionId }
            Log.i("SupabaseManager", "✅ Loaded ${_cartItems.value.size} items for session $sessionId")
        } catch (e: Exception) {
            Log.e("SupabaseManager", "Failed to load session items", e)
        }
    }

    suspend fun getAllProducts(): Result<List<Product>> {
        if (!ensureClient()) return Result.failure(Exception("Supabase client not available"))
        val http = client!!
        return try {
            val products: List<Product> = http.get("${baseUrl.trimEnd('/')}/rest/v1/products") {
                url {
                    parameters.append("select", "*")
                }
                headers {
                    append("apikey", apiKey)
                    append("Authorization", "Bearer $apiKey")
                    append("Accept", "application/json")
                }
            }.body()
            Result.success(products)
        } catch (e: Exception) {
            Log.e("SupabaseManager", "Failed to load products", e)
            Result.failure(Exception("Failed to load products: ${e.message}", e))
        }
    }

    suspend fun addItemToCart(barcode: String, sessionId: String): Result<SessionItem> {
        if (!ensureClient()) return Result.failure(Exception("Supabase client not available"))
        val http = client!!

        Log.d("SupabaseManager", "🛒 addItemToCart called - barcode: $barcode, sessionId: $sessionId")

        return try {
            val products: List<Product> = http.get("${baseUrl.trimEnd('/')}/rest/v1/products") {
                url {
                    parameters.append("select", "*")
                    parameters.append("barcode", "eq.$barcode")
                }
                headers {
                    append("apikey", apiKey)
                    append("Authorization", "Bearer $apiKey")
                    append("Accept", "application/json")
                }
            }.body()

            val product = products.firstOrNull() ?: return Result.failure(Exception("Product not found"))

            Log.d("SupabaseManager", "✅ Found product: ${product.name}")

            // check existing item in session
            val existing: List<SessionItem> = http.get("${baseUrl.trimEnd('/')}/rest/v1/session_items") {
                url {
                    parameters.append("select", "*")
                    parameters.append("sessionId", "eq.$sessionId")
                    parameters.append("barcode", "eq.$barcode")
                }
                headers {
                    append("apikey", apiKey)
                    append("Authorization", "Bearer $apiKey")
                    append("Accept", "application/json")
                }
            }.body()

            if (existing.isNotEmpty()) {
                Log.d("SupabaseManager", "Item already exists, updating quantity")
                val item = existing.first()
                val newQuantity = item.quantity + 1
                val newTotal = newQuantity * item.unitPrice
                // update quantity
                http.patch("${baseUrl.trimEnd('/')}/rest/v1/session_items") {
                    url { parameters.append("itemId", "eq.${item.itemId}") }
                    contentType(ContentType.Application.Json)
                    headers {
                        append("apikey", apiKey)
                        append("Authorization", "Bearer $apiKey")
                    }
                    setBody(ItemQuantityUpdate(quantity = newQuantity, totalPrice = newTotal))
                }
                val updated = item.copy(quantity = newQuantity, totalPrice = newTotal)
                Log.i("SupabaseManager", "✅ Updated quantity to $newQuantity")
                // Refresh cart items
                loadSessionItems(sessionId)
                Result.success(updated)
            } else {
                Log.d("SupabaseManager", "Adding new item to cart")
                val newItem = SessionItem(
                    itemId = UUID.randomUUID().toString(),
                    sessionId = sessionId,
                    productId = product.productId,
                    barcode = barcode,
                    quantity = 1,
                    unitPrice = product.price,
                    totalPrice = product.price,
                    scannedBy = "customer"
                )
                http.post("${baseUrl.trimEnd('/')}/rest/v1/session_items") {
                    contentType(ContentType.Application.Json)
                    headers {
                        append("apikey", apiKey)
                        append("Authorization", "Bearer $apiKey")
                        append("Prefer", "return=representation")
                    }
                    setBody(newItem)
                }
                Log.i("SupabaseManager", "✅ Added new item: ${product.name}")
                // Refresh cart items
                loadSessionItems(sessionId)
                Result.success(newItem)
            }
        } catch (e: Exception) {
            Log.e("SupabaseManager", "❌ Failed to add item to cart", e)
            val errorMsg = when {
                e.message?.contains("Permission denied", ignoreCase = true) == true ->
                    "Network permission denied. Check internet connection."
                e.message?.contains("Unable to resolve host", ignoreCase = true) == true ->
                    "Cannot connect to server. Check your internet connection."
                else -> e.message ?: "Failed to add item"
            }
            Result.failure(Exception(errorMsg, e))
        }
    }

    suspend fun updateItemQuantity(itemId: String, newQuantity: Int) {
        if (!ensureClient()) return
        val http = client!!
        try {
            val items: List<SessionItem> = http.get("${baseUrl.trimEnd('/')}/rest/v1/session_items") {
                url { parameters.append("select", "*") ; parameters.append("itemId", "eq.$itemId") }
                headers {
                    append("apikey", apiKey)
                    append("Authorization", "Bearer $apiKey")
                    append("Accept", "application/json")
                }
            }.body()
            val item = items.firstOrNull() ?: return
            val newTotal = newQuantity * item.unitPrice
            http.patch("${baseUrl.trimEnd('/')}/rest/v1/session_items") {
                url { parameters.append("itemId", "eq.${item.itemId}") }
                contentType(ContentType.Application.Json)
                headers {
                    append("apikey", apiKey)
                    append("Authorization", "Bearer $apiKey")
                }
                setBody(ItemQuantityUpdate(quantity = newQuantity, totalPrice = newTotal))
            }
            // Refresh cart items to update display
            loadSessionItems(item.sessionId)
        } catch (e: Exception) {
            Log.e("SupabaseManager", "Failed to update item quantity", e)
        }
    }

    suspend fun removeItem(itemId: String) {
        if (!ensureClient()) return
        val http = client!!
        try {
            // Get the item first to get sessionId
            val items: List<SessionItem> = http.get("${baseUrl.trimEnd('/')}/rest/v1/session_items") {
                url { parameters.append("select", "*"); parameters.append("itemId", "eq.$itemId") }
                headers {
                    append("apikey", apiKey)
                    append("Authorization", "Bearer $apiKey")
                    append("Accept", "application/json")
                }
            }.body()
            val item = items.firstOrNull()

            // Delete the item
            http.delete("${baseUrl.trimEnd('/')}/rest/v1/session_items") {
                url { parameters.append("itemId", "eq.$itemId") }
                headers {
                    append("apikey", apiKey)
                    append("Authorization", "Bearer $apiKey")
                    append("Accept", "application/json")
                }
            }

            // Refresh cart items to update display
            item?.let { loadSessionItems(it.sessionId) }
        } catch (e: Exception) {
            Log.e("SupabaseManager", "Failed to remove item", e)
        }
    }

    suspend fun completeCheckout(
        sessionId: String,
        paymentMethod: String,
        discountAmount: Double = 0.0
    ): Result<Order> {
        if (!ensureClient()) return Result.failure(Exception("Supabase client not available"))
        val http = client!!
        return try {
            val session = _currentSession.value ?: return Result.failure(Exception("No active session"))
            val totalAmount = _cartItems.value.sumOf { it.totalPrice }
            val finalAmount = totalAmount - discountAmount

            val order = Order(
                orderId = UUID.randomUUID().toString(),
                sessionId = sessionId,
                userId = session.userId,
                totalAmount = totalAmount,
                discountAmount = discountAmount,
                finalAmount = finalAmount,
                paymentMethod = paymentMethod,
                paymentStatus = "completed",
                orderStatus = "completed"
            )

            // 1. Create the order
            http.post("${baseUrl.trimEnd('/')}/rest/v1/orders") {
                contentType(ContentType.Application.Json)
                headers {
                    append("apikey", apiKey)
                    append("Authorization", "Bearer $apiKey")
                }
                setBody(order)
            }

            // 2. Create order items (permanent record of purchased products)
            for (item in _cartItems.value) {
                // Get product details for complete record
                val products: List<Product> = http.get("${baseUrl.trimEnd('/')}/rest/v1/products") {
                    url {
                        parameters.append("select", "*")
                        parameters.append("productId", "eq.${item.productId}")
                    }
                    headers {
                        append("apikey", apiKey)
                        append("Authorization", "Bearer $apiKey")
                        append("Accept", "application/json")
                    }
                }.body()

                val product = products.firstOrNull()

                val orderItem = OrderItem(
                    orderItemId = UUID.randomUUID().toString(),
                    orderId = order.orderId,
                    productId = item.productId,
                    productName = product?.name ?: "Unknown Product",
                    barcode = item.barcode,
                    quantity = item.quantity,
                    unitPrice = item.unitPrice,
                    totalPrice = item.totalPrice,
                    category = product?.category
                )

                http.post("${baseUrl.trimEnd('/')}/rest/v1/order_items") {
                    contentType(ContentType.Application.Json)
                    headers {
                        append("apikey", apiKey)
                        append("Authorization", "Bearer $apiKey")
                    }
                    setBody(orderItem)
                }
            }

            // 3. Mark session completed
            http.patch("${baseUrl.trimEnd('/')}/rest/v1/shopping_sessions") {
                url { parameters.append("sessionId", "eq.$sessionId") }
                contentType(ContentType.Application.Json)
                headers {
                    append("apikey", apiKey)
                    append("Authorization", "Bearer $apiKey")
                }
                setBody(SessionCompletionUpdate(
                    status = "completed",
                    completedAt = System.currentTimeMillis(),
                    totalAmount = totalAmount
                ))
            }

            // 4. Free up cart
            http.patch("${baseUrl.trimEnd('/')}/rest/v1/carts") {
                url { parameters.append("cartId", "eq.${session.cartId}") }
                contentType(ContentType.Application.Json)
                headers {
                    append("apikey", apiKey)
                    append("Authorization", "Bearer $apiKey")
                }
                setBody(CartStatusUpdate(status = "available"))
            }

            _currentSession.value = null
            _cartItems.value = emptyList()

            Log.i("SupabaseManager", "✅ Checkout completed: Order ${order.orderId} with ${_cartItems.value.size} items")
            Result.success(order)
        } catch (e: Exception) {
            Log.e("SupabaseManager", "Failed to complete checkout", e)
            Result.failure(e)
        }
    }
}

// Data Models
@Serializable
data class Cart(
    val cartId: String,
    val status: String,
    val qrCodeData: String? = null,
    val storeLocation: String? = null
)

@Serializable
data class ShoppingSession(
    val sessionId: String,
    val cartId: String,
    val userId: String,
    val status: String,
    val startedAt: Long,
    val completedAt: Long? = null,
    val totalAmount: Double = 0.0
)

@Serializable
data class Product(
    val productId: String,
    val barcode: String,
    val name: String,
    val description: String? = null,
    val price: Double,
    val imageUrl: String? = null,
    val category: String? = null,
    val stockQuantity: Int = 0
)

@Serializable
data class SessionItem(
    val itemId: String,
    val sessionId: String,
    val productId: String,
    val barcode: String,
    val quantity: Int,
    val unitPrice: Double,
    val totalPrice: Double,
    val scannedBy: String = "customer"
)

@Serializable
data class Order(
    val orderId: String,
    val sessionId: String,
    val userId: String,
    val totalAmount: Double,
    val discountAmount: Double,
    val finalAmount: Double,
    val paymentMethod: String,
    val paymentStatus: String,
    val orderStatus: String
)

@Serializable
data class OrderItem(
    val orderItemId: String,
    val orderId: String,
    val productId: String,
    val productName: String,
    val barcode: String,
    val quantity: Int,
    val unitPrice: Double,
    val totalPrice: Double,
    val category: String? = null
)

@Serializable
data class User(
    val userId: String,
    val email: String,
    val name: String,
    val phone: String? = null
)

// Update request bodies
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

