package com.example.smartcart.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartcart.SupabaseManager
import kotlinx.coroutines.launch

data class DealItem(
    val barcode: String,
    val name: String,
    val price: String,
    val imageUrl: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val scope = rememberCoroutineScope()
    val currentSession by SupabaseManager.currentSession.collectAsState(initial = null)
    val snackbarHostState = remember { SnackbarHostState() }

    // State for products loaded from database
    var products by remember { mutableStateOf<List<DealItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Load products from database on screen load
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                isLoading = true
                errorMessage = null
                val result = SupabaseManager.getAllProducts()
                result.onSuccess { productList ->
                    products = productList.map { product ->
                        DealItem(
                            barcode = product.barcode,
                            name = product.name,
                            price = "Rs.${String.format("%.2f", product.price)}",
                            imageUrl = ""
                        )
                    }
                    isLoading = false
                }.onFailure { error ->
                    errorMessage = error.message ?: "Failed to load products"
                    isLoading = false
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Unexpected error"
                isLoading = false
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Hi, Rahul", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("Welcome to SmartCart", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("profile") }) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Profile",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                },
                actions = {
                    // Show cart badge if session is active
                    if (currentSession != null) {
                        Badge(
                            containerColor = Color.Red,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Active", color = Color.White, fontSize = 10.sp)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF3F51B5))
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = Color.White,
                contentColor = Color(0xFF3F51B5)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.navigate("home") }) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Home, contentDescription = "Home", tint = Color(0xFF3F51B5))
                            Text("Home", fontSize = 10.sp, color = Color(0xFF3F51B5))
                        }
                    }
                    IconButton(onClick = { /* Notification click */ }) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
                            Text("Notify", fontSize = 10.sp)
                        }
                    }
                    IconButton(onClick = { navController.navigate("scan") }) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.QrCode, contentDescription = "Scan")
                            Text("Scan", fontSize = 10.sp)
                        }
                    }
                    IconButton(onClick = { navController.navigate("cart") }) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.ShoppingCart, contentDescription = "Cart")
                            Text("Cart", fontSize = 10.sp)
                        }
                    }
                    IconButton(onClick = { navController.navigate("profile") }) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Person, contentDescription = "Profile")
                            Text("Profile", fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            // "Save extra on every order" card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Save extra on every order",
                            color = Color(0xFF3F51B5),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Scan cart QR to start shopping!",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        if (currentSession == null) {
                            TextButton(
                                onClick = { navController.navigate("scan") },
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Text("Start Shopping", color = Color(0xFF3F51B5), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Deals of the Day",
                color = Color.Black,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Show loading, error, or products
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF3F51B5))
                    }
                }
                errorMessage != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = errorMessage ?: "Error loading products",
                            color = Color.Red,
                            fontSize = 14.sp
                        )
                    }
                }
                products.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No products available",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(products) { deal ->
                            DealItemCard(
                                deal = deal,
                                currentSession = currentSession,
                                onAddToCart = { barcode ->
                                    scope.launch {
                                        if (currentSession == null) {
                                            snackbarHostState.showSnackbar(
                                                message = "Please scan cart QR code first!",
                                                duration = SnackbarDuration.Short
                                            )
                                        } else {
                                            val result = SupabaseManager.addItemToCart(
                                                barcode = barcode,
                                                sessionId = currentSession!!.sessionId
                                            )
                                            result.onSuccess {
                                                snackbarHostState.showSnackbar(
                                                    message = "Added to cart!",
                                                    duration = SnackbarDuration.Short
                                                )
                                            }.onFailure { error ->
                                                snackbarHostState.showSnackbar(
                                                    message = error.message ?: "Failed to add item",
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DealItemCard(
    deal: DealItem,
    currentSession: com.example.smartcart.ShoppingSession?,
    onAddToCart: (String) -> Unit
) {
    var isLoading by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Placeholder for product image
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.ShoppingBag,
                    contentDescription = null,
                    tint = Color.Gray
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = deal.name,
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = deal.price,
                    color = Color(0xFF3F51B5),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Add to cart button
            Button(
                onClick = {
                    isLoading = true
                    onAddToCart(deal.barcode)
                    isLoading = false
                },
                modifier = Modifier.height(40.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (currentSession != null) Color(0xFF3F51B5) else Color.Gray
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Filled.Add, contentDescription = "Add to cart", tint = Color.White)
                }
            }
        }
    }
}