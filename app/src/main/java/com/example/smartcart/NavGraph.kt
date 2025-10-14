package com.example.smartcart

import androidx.compose.runtime.Composable
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
        composable("splash") { SplashScreen(navController) }
        composable("signIn") { SignInScreen(navController) }
        composable("signUp") { SignUpScreen(navController) }
        composable("scan") { ScanScreen(navController) }
        composable("scanner") { ScannerScreen(navController) }
        composable("success") { SuccessScreen(navController) }
        composable("home") { HomeScreen(navController) }
        composable("cart") { CartScreen(navController) }
        composable("checkout") { CheckoutScreen(navController) }
        composable("thankyou") { ThankYouScreen(navController) }
        composable("profile") { ProfileScreen(navController) }
    }
}