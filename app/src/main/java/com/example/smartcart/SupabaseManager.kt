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
    val currentSession: Flow<ShoppingSession?> = _currentSession

    // Cart items in current session
    private val _cartItems = MutableStateFlow<List<SessionItem>>(emptyList())
    val cartItems: Flow<List<SessionItem>> = _cartItems


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

            // Insert session
            http.post("${baseUrl.trimEnd('/')}/rest/v1/shopping_sessions") {
                headers {
                    append("apikey", apiKey)
                    append("Authorization", "Bearer $apiKey")
                    append("Accept", "application/json")
                    append("Content-Type", "application/json")
                    append("Prefer", "return=representation")
                }
                setBody(session)
            }

            // Mark cart as in use
            http.patch("${baseUrl.trimEnd('/')}/rest/v1/carts") {
                url { parameters.append("cartId", "eq.$cartId") }
                headers {
                    append("apikey", apiKey)
                    append("Authorization", "Bearer $apiKey")
                    append("Accept", "application/json")
                    append("Content-Type", "application/json")
                    append("Prefer", "return=representation")
                }
                setBody(mapOf("status" to "in_use"))
            }

            _currentSession.value = session

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
                val item = existing.first()
                val newQuantity = item.quantity + 1
                val newTotal = newQuantity * item.unitPrice
                // update quantity
                http.patch("${baseUrl.trimEnd('/')}/rest/v1/session_items") {
                    url { parameters.append("itemId", "eq.${item.itemId}") }
                    headers {
                        append("apikey", apiKey)
                        append("Authorization", "Bearer $apiKey")
                        append("Accept", "application/json")
                        append("Content-Type", "application/json")
                    }
                    setBody(mapOf("quantity" to newQuantity, "totalPrice" to newTotal))
                }
                val updated = item.copy(quantity = newQuantity, totalPrice = newTotal)
                Result.success(updated)
            } else {
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
                    headers {
                        append("apikey", apiKey)
                        append("Authorization", "Bearer $apiKey")
                        append("Accept", "application/json")
                        append("Content-Type", "application/json")
                    }
                    setBody(newItem)
                }
                Result.success(newItem)
            }
        } catch (e: Exception) {
            Log.e("SupabaseManager", "Failed to add item to cart", e)
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
                url { parameters.append("itemId", "eq.$itemId") }
                headers {
                    append("apikey", apiKey)
                    append("Authorization", "Bearer $apiKey")
                    append("Accept", "application/json")
                    append("Content-Type", "application/json")
                }
                setBody(mapOf("quantity" to newQuantity, "totalPrice" to newTotal))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun removeItem(itemId: String) {
        if (!ensureClient()) return
        val http = client!!
        try {
            http.delete("${baseUrl.trimEnd('/')}/rest/v1/session_items") {
                url { parameters.append("itemId", "eq.$itemId") }
                headers {
                    append("apikey", apiKey)
                    append("Authorization", "Bearer $apiKey")
                    append("Accept", "application/json")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
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

            http.post("${baseUrl.trimEnd('/')}/rest/v1/orders") {
                headers {
                    append("apikey", apiKey)
                    append("Authorization", "Bearer $apiKey")
                    append("Accept", "application/json")
                    append("Content-Type", "application/json")
                }
                setBody(order)
            }

            // mark session completed
            http.patch("${baseUrl.trimEnd('/')}/rest/v1/shopping_sessions") {
                url { parameters.append("sessionId", "eq.$sessionId") }
                headers {
                    append("apikey", apiKey)
                    append("Authorization", "Bearer $apiKey")
                    append("Accept", "application/json")
                    append("Content-Type", "application/json")
                }
                setBody(mapOf("status" to "completed", "completedAt" to System.currentTimeMillis(), "totalAmount" to totalAmount))
            }

            // free up cart
            http.patch("${baseUrl.trimEnd('/')}/rest/v1/carts") {
                // find cart id
                url { parameters.append("cartId", "eq.${session.cartId}") }
                headers {
                    append("apikey", apiKey)
                    append("Authorization", "Bearer $apiKey")
                    append("Accept", "application/json")
                    append("Content-Type", "application/json")
                }
                setBody(mapOf("status" to "available"))
            }

            _currentSession.value = null
            _cartItems.value = emptyList()

            Result.success(order)
        } catch (e: Exception) {
            e.printStackTrace()
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

