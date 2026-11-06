package com.example.amicare.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.ByteArrayOutputStream

object ImageUtils {

    /**
     * Mengubah Uri gambar dari Galeri Mahasiswa menjadi teks Base64
     * yang sudah dikompresi agar siap disimpan ke Cloud Firestore.
     */
    fun uriToBase64(context: Context, imageUri: Uri, maxSize: Int = 800): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) return null

            // 1. Perkecil resolusi gambar secara proporsional agar tidak melebihi batas 1 MB Firestore
            val ratio = minOf(
                maxSize.toFloat() / originalBitmap.width,
                maxSize.toFloat() / originalBitmap.height
            )
            val width = if (ratio < 1f) (originalBitmap.width * ratio).toInt() else originalBitmap.width
            val height = if (ratio < 1f) (originalBitmap.height * ratio).toInt() else originalBitmap.height

            val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, true)

            // 2. Kompres ke format JPEG (kualitas 60%)
            val outputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
            val byteArray = outputStream.toByteArray()

            // 3. Ubah byte array menjadi String Base64
            Base64.encodeToString(byteArray, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Mengubah teks Base64 dari Cloud Firestore kembali menjadi ImageBitmap
     * agar dapat ditampilkan di halaman Detail Pengaduan (Sisi Admin & Mahasiswa).
     */
    fun base64ToImageBitmap(base64String: String): ImageBitmap? {
        if (base64String.isBlank()) return null
        return try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            bitmap?.asImageBitmap()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}