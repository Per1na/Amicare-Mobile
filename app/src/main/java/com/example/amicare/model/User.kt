package com.example.amicare.model

data class User(
    val uid: String = "",
    val nim: String = "",
    val nama: String = "",
    val email: String = "",
    val prodi: String = "",
    val role: String = "mahasiswa" // Pilihan: "mahasiswa" atau "admin"
)