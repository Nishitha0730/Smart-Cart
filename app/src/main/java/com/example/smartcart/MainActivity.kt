package com.example.smartcart

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.smartcart.ui.theme.SmartCartTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // SupabaseManager.init("https://kwqbznubuonnfeskmnxu.supabase.co", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imt3cWJ6bnVidW9ubmZlc2ttbnh1Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjAzMzcyNDksImV4cCI6MjA3NTkxMzI0OX0.KczJOQl3yqZmnq67LPBU6h7byLPmFIZIQPecXSS1D2w")
        setContent {
            SmartCartTheme {
                NavGraph()
            }
        }
    }
}