package com.example.smartcart

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.smartcart.ui.theme.SmartCartTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : ComponentActivity() {
    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var scanLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        // Set a global uncaught exception handler so we can capture crashes to a file for diagnosis
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("UncaughtException", "Uncaught exception in thread ${thread.name}", throwable)
            try {
                val file = File(filesDir, "crash_log.txt")
                file.appendText("Thread: ${thread.name}\n")
                file.appendText(Log.getStackTraceString(throwable) + "\n\n")
            } catch (e: Exception) {
                Log.e("UncaughtException", "Failed to write crash log", e)
            }

            // Optionally show a toast to inform the user (must run on main looper)
            try {
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(this, "App crashed — log saved to crash_log.txt", Toast.LENGTH_LONG).show()
                }
            } catch (ignored: Exception) {
            }

            // Terminate the process to avoid undefined state
            android.os.Process.killProcess(android.os.Process.myPid())
            System.exit(2)
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Supabase client asynchronously at startup to avoid blocking
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    SupabaseManager.init()
                }
                if (!SupabaseManager.isAvailable()) {
                    Log.e("MainActivity", "⚠️ Supabase client NOT available - check credentials")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@MainActivity,
                            "⚠️ Supabase not configured properly",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Log.i("MainActivity", "✅ Supabase initialized and ready")
                    // Don't show toast - only show errors to avoid confusion
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "❌ Exception while initializing Supabase", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Failed to initialize Supabase: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        // Register permission launcher
        cameraPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                launchScanner()
            } else {
                Log.w("QrScanner", "Camera permission denied")
            }
        }

        // Register scanner launcher
        scanLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            try {
                if (result.resultCode == Activity.RESULT_OK) {
                    val scanText = result.data?.getStringExtra("SCAN_RESULT")
                    if (!scanText.isNullOrEmpty()) {
                        processQr(scanText)
                    } else {
                        Log.w("QrScanner", "Empty scan result")
                    }
                } else {
                    Log.i("QrScanner", "Scan cancelled or failed: code=${result.resultCode}")
                }
            } catch (e: Exception) {
                Log.e("QrScanner", "Error handling scan result", e)
            }
        }

        setContent {
            SmartCartTheme {
                NavGraph()
            }
        }
    }

    /**
     * Call this when you want to start scanning (e.g. from your signup flow).
     * It ensures camera permission is granted before launching the scanner.
     */
    fun ensureCameraPermissionAndScan() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED -> {
                launchScanner()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                // Optionally show UI rationale before requesting
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun launchScanner() {
        // Example: trying to use external ZXing app. Replace with your in-app scanner Intent if you use one.
        val intent = Intent("com.google.zxing.client.android.SCAN")
        try {
            scanLauncher.launch(intent)
        } catch (e: ActivityNotFoundException) {
            Log.e("QrScanner", "Scanner app not found", e)
            // Optional: fallback or prompt to install scanner
        } catch (e: Exception) {
            Log.e("QrScanner", "Failed to start scanner intent", e)
        }
    }

    private fun processQr(text: String) {
        // Handle the scanned QR safely: parse, navigate, or call network on a coroutine
        Log.i("QrScanner", "Scanned: $text")

        // Check if Supabase is available before processing
        if (!SupabaseManager.isAvailable()) {
            Log.e("QrScanner", "Supabase client not available, cannot process QR code")
            Toast.makeText(this, "Service not ready. Please wait and try again.", Toast.LENGTH_LONG).show()
            return
        }

        // Process QR code asynchronously to avoid blocking the UI
        lifecycleScope.launch {
            try {
                // Parse the scanned text (assuming it contains cart ID)
                val cartId = text.trim()

                // For demo purposes, you might want to navigate to the scanner screen
                // or directly start a shopping session
                // TODO: integrate with your NavGraph / signup logic

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "QR Code scanned: $cartId",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e("QrScanner", "Error processing QR code", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Failed to process QR code: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}