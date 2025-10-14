package com.example.smartcart.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

data class DealItem(val name: String, val price: String, val imageUrl: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val deals = listOf(
        DealItem("Fresh milk", "220 widget", ""),
        DealItem("Cooking Oil", "220 widget", ""),
        DealItem("Washing Powder", "220 widget", ""),
        DealItem("Fresh milk", "220 widget", ""),
        DealItem("Cooking Oil", "220 widget", ""),
        DealItem("Washing Powder", "220 widget", ""),
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Hi, Rahul", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("Welcome to SmartCart", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { /* Handle profile icon click */ }) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Profile",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
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
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)) // Light blue background
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
                            text = "Etiam mollis metus non faucibus.",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    // Placeholder for image on the right
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
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(deals) { deal ->
                    DealItemCard(deal = deal)
                }
            }
        }
    }
}

@Composable
fun DealItemCard(deal: DealItem) {
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
                    .background(Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            )
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
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
            Button(
                onClick = { /* Add to cart */ },
                modifier = Modifier.height(40.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add to cart", tint = Color.White)
            }
        }
    }
}
