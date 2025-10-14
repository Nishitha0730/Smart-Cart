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

data class CartItem(val id: Int, val name: String, val description: String, val price: Double, var quantity: Int, val imageUrl: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(navController: NavController) {
    val cartItems = remember { mutableStateListOf(
        CartItem(1, "Sugar free gold", "bottle of 500 pallets", 25.0, 1, ""),
        CartItem(2, "Sugar free gold", "bottle of 500 pallets", 18.0, 1, ""),
    ) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your cart", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
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
                            Icon(Icons.Filled.Home, contentDescription = "Home")
                            Text("Home", fontSize = 10.sp)
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
                            Icon(Icons.Filled.ShoppingCart, contentDescription = "Cart", tint = Color(0xFF3F51B5))
                            Text("Cart", fontSize = 10.sp, color = Color(0xFF3F51B5))
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
            Text(
                text = "${cartItems.size} Items in your cart",
                color = Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(cartItems) { item ->
                    CartItemCard(item = item, onQuantityChange = { updatedItem ->
                        val index = cartItems.indexOfFirst { it.id == updatedItem.id }
                        if (index != -1) {
                            cartItems[index] = updatedItem
                        }
                    },
                    onRemove = { itemToRemove ->
                        cartItems.remove(itemToRemove)
                    })
                }
            }
            Button(
                onClick = { navController.navigate("checkout") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Place Order", color = Color.White, fontSize = 18.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartItemCard(
    item: CartItem,
    onQuantityChange: (CartItem) -> Unit,
    onRemove: (CartItem) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Placeholder for product image
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.name,
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = item.description,
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = "Rs.${item.price}",
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { onRemove(item) }) {
                    Icon(Icons.Filled.Close, contentDescription = "Remove item", tint = Color.Gray)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { if (item.quantity > 1) onQuantityChange(item.copy(quantity = item.quantity - 1)) },
                        modifier = Modifier.size(32.dp),
                        colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFFE0E0E0))
                    ) {
                        Icon(Icons.Filled.Remove, contentDescription = "Decrease quantity", tint = Color.Black)
                    }
                    Text(
                        text = "${item.quantity}",
                        modifier = Modifier.padding(horizontal = 8.dp),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    IconButton(
                        onClick = { onQuantityChange(item.copy(quantity = item.quantity + 1)) },
                        modifier = Modifier.size(32.dp),
                        colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFFE0E0E0))
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Increase quantity", tint = Color.Black)
                    }
                }
            }
        }
    }
}