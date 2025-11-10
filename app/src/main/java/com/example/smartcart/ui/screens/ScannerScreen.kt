// ScannerScreen.kt
package com.example.smartcart.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.smartcart.SupabaseManager
import kotlinx.coroutines.launch

@Composable
fun ScannerScreen(navController: NavController) {
    val scope = rememberCoroutineScope()
    var isScanning by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // For testing: Use a specific cart ID that exists in your Supabase database
    // You should create a cart with this ID in your Supabase 'carts' table
    val testCartId = "CART_002"  // Using CART_002 since CART_001 is in use

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF3F51B5))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Scan Cart QR Code",
                    color = Color.White,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(60.dp))

            // QR Scanner view placeholder
            Card(
                modifier = Modifier.size(300.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (isScanning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(60.dp),
                            color = Color(0xFF3F51B5)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.QrCodeScanner,
                            contentDescription = "QR Code Scanner",
                            tint = Color(0xFF3F51B5),
                            modifier = Modifier.size(120.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Point camera at cart QR code",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )

            // Show error if any
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.2f))
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }


            Spacer(modifier = Modifier.weight(1f))

            // Simulate scan button (in real app, this would be automatic)
            Button(
                onClick = {
                    errorMessage = null

                    // Check if Supabase is available
                    if (!SupabaseManager.isAvailable()) {
                        errorMessage = "Supabase not connected. Please wait and try again."
                        return@Button
                    }

                    // Start scanning and create session
                    scope.launch {
                        isScanning = true
                        errorMessage = null
                        try {
                            // Use the test cart ID (in real app, this comes from QR code scan)
                            val scannedCartId = testCartId
                            val userId = "user_${System.currentTimeMillis()}"

                            val result = SupabaseManager.startShoppingSession(scannedCartId, userId)

                            result.onSuccess { session ->
                                // Navigate to success screen
                                navController.navigate("success") {
                                    popUpTo("scanner") { inclusive = true }
                                }
                            }.onFailure { error ->
                                errorMessage = error.message ?: "Failed to start session"
                            }
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "Unexpected error during scan"
                        } finally {
                            isScanning = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(8.dp),
                enabled = !isScanning
            ) {
                if (isScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color(0xFF3F51B5),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.QrCodeScanner,
                        contentDescription = "Scan QR Icon",
                        tint = Color(0xFF3F51B5),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SCAN QR CODE", color = Color(0xFF3F51B5), fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Show test cart ID
            Text(
                text = "Testing with Cart: $testCartId",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp
            )

            Text(
                text = "Make sure this cart exists in your Supabase 'carts' table",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 10.sp
            )
        }
    }
}
