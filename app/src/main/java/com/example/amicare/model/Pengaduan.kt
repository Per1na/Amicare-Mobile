package com.example.amicare.model

import com.google.firebase.Timestamp

data class Pengaduan(
    val idPengaduan: String = "",
    val uidPelapor: String = "",
    val nimPelapor: String = "",
    val namaPelapor: String = "",
    val kategori: String = "",
    val judul: String = "",
    val lokasi: String = "",
    val deskripsi: String = "",
    val fotoBase64: String = "", // Menyimpan string gambar (opsional/fitur opsional kita)
    val status: String = "Menunggu", // Status awal: Menunggu, Diproses, Selesai, Ditolak
    val tanggapanAdmin: String = "",
    val tanggalLapor: Timestamp? = null
)