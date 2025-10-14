package com.example.smartcart.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile", color = Color.White, fontWeight = FontWeight.Bold) },
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
                            Icon(Icons.Filled.ShoppingCart, contentDescription = "Cart")
                            Text("Cart", fontSize = 10.sp)
                        }
                    }
                    IconButton(onClick = { navController.navigate("profile") }) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Person, contentDescription = "Profile", tint = Color(0xFF3F51B5))
                            Text("Profile", fontSize = 10.sp, color = Color(0xFF3F51B5))
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
            // Profile Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE3F2FD)) // Light blue background for header
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    modifier = Modifier.size(80.dp),
                    shadowElevation = 8.dp
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Profile Picture",
                            tint = Color(0xFF3F51B5),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Hi, Rahul kanjariya",
                    color = Color.Black,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Welcome to SmartCart",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
            // Profile Menu Items
            Spacer(modifier = Modifier.height(16.dp))
            ProfileMenuItem(Icons.Filled.Edit, "Edit Profile") { /* Handle edit profile */ }
            ProfileMenuItem(Icons.Filled.ListAlt, "My orders") { /* Handle my orders */ }
            ProfileMenuItem(Icons.Filled.Receipt, "Billing") { /* Handle billing */ }
            ProfileMenuItem(Icons.Filled.LiveHelp, "Faq") { /* Handle FAQ */ }
        }
    }
}

@Composable
fun ProfileMenuItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = title, tint = Color.Black)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = title, fontSize = 18.sp, color = Color.Black)
        }
        Icon(imageVector = Icons.Filled.ArrowForwardIos, contentDescription = "Arrow", tint = Color.Gray, modifier = Modifier.size(16.dp))
    }
    Divider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.LightGray)
}