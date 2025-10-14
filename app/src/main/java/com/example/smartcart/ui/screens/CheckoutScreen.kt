package com.example.smartcart.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(navController: NavController) {
    var selectedPaymentMethod by remember { mutableStateOf("PayHere") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout", color = Color.White, fontWeight = FontWeight.Bold) },
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
            // Payment Summary Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Payment Summary",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    PaymentSummaryRow("Order Total", "228.80", Color.Black)
                    PaymentSummaryRow("Items Discount", "- 28.80", Color.Red)
                    PaymentSummaryRow("Coupon Discount", "-15.80", Color.Red)
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    PaymentSummaryRow("Total", "Rs.185.00", Color.Black, FontWeight.Bold)
                }
            }

            // Payment Method Section
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Payment method",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column {
                    PaymentMethodItem(
                        icon = Icons.Filled.CreditCard, // Placeholder for PayHere icon
                        methodName = "PayHere",
                        isSelected = selectedPaymentMethod == "PayHere",
                        onSelected = { selectedPaymentMethod = "PayHere" }
                    )
                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                    PaymentMethodItem(
                        icon = Icons.Filled.Payments, // Placeholder for Pay at cashier icon
                        methodName = "Pay at cashier",
                        isSelected = selectedPaymentMethod == "Pay at cashier",
                        onSelected = { selectedPaymentMethod = "Pay at cashier" }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { navController.navigate("thankyou") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Pay Now", color = Color.White, fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun PaymentSummaryRow(label: String, value: String, valueColor: Color, fontWeight: FontWeight = FontWeight.Normal) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 16.sp, color = Color.Gray)
        Text(text = value, fontSize = 16.sp, color = valueColor, fontWeight = fontWeight)
    }
    Spacer(modifier = Modifier.height(4.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodItem(icon: androidx.compose.ui.graphics.vector.ImageVector, methodName: String, isSelected: Boolean, onSelected: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelected() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = methodName, tint = Color.Black)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = methodName, fontSize = 16.sp, color = Color.Black)
        }
        RadioButton(
            selected = isSelected,
            onClick = { onSelected() },
            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF3F51B5))
        )
    }
}