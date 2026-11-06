package com.example.amicare.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.amicare.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onTimeout: () -> Unit
) {
    // Deteksi apakah sedang dibuka di Android Studio Preview agar garis layout tidak bergeser
    val isPreview = LocalInspectionMode.current
    var startAnimation by remember { mutableStateOf(isPreview) }

    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "FadeAnimation"
    )

    val scaleAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.85f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "ScaleAnimation"
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(2500)
        onTimeout()
    }

    // Palet warna Light Modern & Identitas AMIKOM
    val backgroundLavender = Color(0xFFF8F6FC)
    val primaryPurple = Color(0xFF700070)
    val tekheletPurple = Color(0xFF4A1B9D)
    val textMutedColor = Color(0xFF6E6B7B)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundLavender)
            .padding(24.dp)
    ) {
        // 1. Bagian Tengah (Logo Resmi AMICARE, Tagline, dan Deskripsi)
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .alpha(alphaAnim),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Menggunakan width (bukan size persegi) agar tinggi kotak pas mengikuti rasio logo (349:128)
            Image(
                painter = painterResource(id = R.drawable.logo_amicare),
                contentDescription = "Logo AMICARE",
                modifier = Modifier
                    .width(200.dp)
                    .scale(scaleAnim),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Your Voice, Our Concern",
                style = MaterialTheme.typography.titleMedium,
                color = tekheletPurple,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "A Platform for Student Feedback and Aspirations",
                style = MaterialTheme.typography.bodySmall,
                color = textMutedColor,
                textAlign = TextAlign.Center
            )
        }

        // 2. Bagian Bawah (Loading Indicator & Identitas Pengembang)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
                .alpha(alphaAnim),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                color = primaryPurple,
                strokeWidth = 2.5.dp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Developed by D3 Manajemen Informatika",
                style = MaterialTheme.typography.labelSmall,
                color = textMutedColor.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SplashScreenPreview() {
    SplashScreen(onTimeout = {})
}