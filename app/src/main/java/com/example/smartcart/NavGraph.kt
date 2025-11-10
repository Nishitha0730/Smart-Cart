package com.example.smartcart

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.smartcart.ui.screens.CartScreen
import com.example.smartcart.ui.screens.CheckoutScreen
import com.example.smartcart.ui.screens.HomeScreen
import com.example.smartcart.ui.screens.ProfileScreen
import com.example.smartcart.ui.screens.ScanScreen
import com.example.smartcart.ui.screens.ScannerScreen
import com.example.smartcart.ui.screens.SignInScreen
import com.example.smartcart.ui.screens.SignUpScreen
import com.example.smartcart.ui.screens.SplashScreen
import com.example.smartcart.ui.screens.SuccessScreen
import com.example.smartcart.ui.screens.ThankYouScreen

@Composable
fun NavGraph(startDestination: String = "splash") {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = startDestination) {
        composable("splash") { safeComposable(navController) { SplashScreen(navController) } }
        composable("signIn") { safeComposable(navController) { SignInScreen(navController) } }
        composable("signUp") { safeComposable(navController) { SignUpScreen(navController) } }
        composable("scan") { safeComposable(navController) { ScanScreen(navController) } }
        composable("scanner") { safeComposable(navController) { ScannerScreen(navController) } }
        composable("success") { safeComposable(navController) { SuccessScreen(navController) } }
        composable("home") { safeComposable(navController) { HomeScreen(navController) } }
        composable("cart") { safeComposable(navController) { CartScreen(navController) } }
        composable("checkout") { safeComposable(navController) { CheckoutScreen(navController) } }
        composable("thankyou") { safeComposable(navController) { ThankYouScreen(navController) } }
        composable("profile") { safeComposable(navController) { ProfileScreen(navController) } }
    }
}

@Composable
private fun safeComposable(navController: androidx.navigation.NavController, content: @Composable () -> Unit) {
    val result = runCatching {
        content()
    }
    result.onFailure { throwable ->
        Log.e("NavGraph", "Screen threw an exception", throwable)
        ErrorScreen(navController, throwable)
    }
}

@Composable
private fun ErrorScreen(navController: androidx.navigation.NavController, throwable: Throwable) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Something went wrong in this screen.")
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = throwable.localizedMessage ?: "Unknown error")
        }
    }
}