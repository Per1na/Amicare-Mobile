package com.example.amicare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.amicare.ui.screens.AddPengaduanScreen
import com.example.amicare.ui.screens.HomeScreen
import com.example.amicare.ui.screens.LoginScreen
import com.example.amicare.ui.screens.SplashScreen
import com.example.amicare.ui.theme.AmicareTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AmicareTheme {
                val auth = FirebaseAuth.getInstance()
                val currentUser = auth.currentUser

                // State untuk mengatur alur layar
                // "splash" -> "login" / "home" -> "add_pengaduan"
                var currentScreen by remember { mutableStateOf("splash") }

                when (currentScreen) {
                    "splash" -> {
                        SplashScreen(
                            onTimeout = {
                                // Setelah splash selesai, cek apakah user sudah login sebelumnya
                                currentScreen = if (currentUser != null) "home" else "login"
                            }
                        )
                    }
                    "login" -> {
                        LoginScreen(
                            onLoginSuccess = {
                                currentScreen = "home"
                            }
                        )
                    }
                    "home" -> {
                        HomeScreen(
                            onNavigateToBuatPengaduan = {
                                currentScreen = "add_pengaduan"
                            },
                            onLogoutSuccess = {
                                currentScreen = "login"
                            }
                        )
                    }
                    "add_pengaduan" -> {
                        AddPengaduanScreen(
                            onBack = {
                                currentScreen = "home"
                            },
                            onSubmitSuccess = {
                                currentScreen = "home"
                            }
                        )
                    }
                }
            }
        }
    }
}