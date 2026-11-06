package com.example.amicare.utils

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

object EmailSender {

    // 1. Ganti dengan alamat Gmail tempat Anda membuat App Password
    private const val EMAIL_PENGIRIM_SISTEM = "rclashofclans04@gmail.com"

    // 2. Ganti dengan 16 huruf App Password "Amicare" dari Google
    private const val APP_PASSWORD_GOOGLE = "auux swaa xqev etmw"

    // 3. Ganti dengan alamat email Admin penerima notifikasi (bisa disamakan dengan email di atas untuk uji coba)
    private const val EMAIL_ADMIN_KAMPUS = "rclashofclans04@gmail.com"

    fun kirimNotifikasiKeAdmin(
        namaPelapor: String,
        nimPelapor: String,
        prodiPelapor: String,
        kategori: String,
        judul: String,
        lokasi: String,
        deskripsi: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val props = Properties().apply {
                    put("mail.smtp.host", "smtp.gmail.com")
                    put("mail.smtp.socketFactory.port", "465")
                    put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
                    put("mail.smtp.auth", "true")
                    put("mail.smtp.port", "465")
                }

                val session = Session.getInstance(props, object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication(
                            EMAIL_PENGIRIM_SISTEM,
                            APP_PASSWORD_GOOGLE.replace(
                                " ",
                                ""
                            ) // Otomatis menghapus spasi pada 16 digit kode
                        )
                    }
                })

                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress(EMAIL_PENGIRIM_SISTEM, "Amicare - Notifikasi Sistem"))
                    setRecipients(Message.RecipientType.TO, InternetAddress.parse(EMAIL_ADMIN_KAMPUS))
                    subject = "[Laporan Baru Amicare] $kategori - $judul"
                    setText(
                        """
                        Halo Admin Kampus,
                        
                        Sistem Amicare baru saja menerima laporan pengaduan / aspirasi baru dari mahasiswa dengan rincian sebagai berikut:
                        
                        DATA PELAPOR:
                        • Nama Lengkap : $namaPelapor
                        • NIM          : $nimPelapor
                        • Program Studi: $prodiPelapor
                        
                        DETAIL LAPORAN:
                        • Kategori     : $kategori
                        • Lokasi       : $lokasi
                        • Judul        : $judul
                        
                        ISI DESKRIPSI:
                        "$deskripsi"
                        
                        --------------------------------------------------
                        Status laporan saat ini: MENUNGGU.
                        Silakan buka dashboard Firebase Console (koleksi 'pengaduan') untuk memproses laporan dan memberikan tanggapan kepada mahasiswa.
                        """.trimIndent()
                    )
                }

                Transport.send(message)
                Log.d("EmailSender", "Push email notifikasi berhasil dikirim ke Admin!")
            } catch (e: Exception) {
                Log.e("EmailSender", "Gagal mengirim push email: ${e.localizedMessage}", e)
            }
        }
    }
}