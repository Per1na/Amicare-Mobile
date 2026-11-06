package com.example.amicare.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.amicare.utils.EmailSender
import com.example.amicare.model.Pengaduan
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.InputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPengaduanScreen(
    onBack: () -> Unit,
    onSubmitSuccess: () -> Unit
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser

    // State untuk data profil yang ditarik dari koleksi "Users" di Firestore
    var namaMahasiswa by remember { mutableStateOf("Mengambil data dari database...") }
    var nimMahasiswa by remember { mutableStateOf("...") }
    var prodiMahasiswa by remember { mutableStateOf("...") }
    var isLoadingProfile by remember { mutableStateOf(true) }

    // State form input pengaduan
    var kategori by remember { mutableStateOf("Fasilitas Kelas / Lab") }
    var judul by remember { mutableStateOf("") }
    var lokasi by remember { mutableStateOf("") }
    var deskripsi by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    // State untuk Bukti Foto (URI & Base64)
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var fotoBase64 by remember { mutableStateOf("") }

    // Launcher untuk memilih gambar dari galeri HP
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                if (bytes != null) {
                    // Konversi file gambar ke string Base64 agar ringan disimpan ke Firestore
                    fotoBase64 = Base64.encodeToString(bytes, Base64.DEFAULT)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal memproses gambar: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val daftarKategori = listOf(
        "Fasilitas Kelas / Lab",
        "Akademik & Pengajaran",
        "Pelayanan Prodi",
        "Sarana Umum Kampus",
        "Lainnya"
    )
    var expandedDropdown by remember { mutableStateOf(false) }

    // Mengambil data dari koleksi "Users" di Cloud Firestore berdasarkan UID
    LaunchedEffect(currentUser?.uid) {
        if (currentUser != null) {
            firestore.collection("Users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        namaMahasiswa = document.getString("nama") ?: "Nama belum diatur"
                        nimMahasiswa = document.getString("nim") ?: "NIM belum diatur"
                        prodiMahasiswa = document.getString("prodi") ?: "Prodi belum diatur"
                    } else {
                        namaMahasiswa = "Data Profil Belum Ada di Database"
                        nimMahasiswa = "Isi data di koleksi 'Users' Firebase"
                        prodiMahasiswa = "-"
                    }
                    isLoadingProfile = false
                }
                .addOnFailureListener { e ->
                    namaMahasiswa = "Gagal memuat database"
                    nimMahasiswa = e.localizedMessage ?: "Error"
                    isLoadingProfile = false
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Buat Laporan Pengaduan",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Batal", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Tampilan Kartu Identitas dari Database Firestore
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Data Mahasiswa (Dari Database Firestore):",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Nama: $namaMahasiswa",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "NIM: $nimMahasiswa",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Prodi: $prodiMahasiswa",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Pilihan Kategori (Dropdown)
            ExposedDropdownMenuBox(
                expanded = expandedDropdown,
                onExpandedChange = { expandedDropdown = !expandedDropdown }
            ) {
                OutlinedTextField(
                    value = kategori,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Kategori Laporan") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown)
                    },
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedDropdown,
                    onDismissRequest = { expandedDropdown = false }
                ) {
                    daftarKategori.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                kategori = item
                                expandedDropdown = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = judul,
                onValueChange = { judul = it },
                label = { Text("Judul Pengaduan (Singkat & Jelas)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = lokasi,
                onValueChange = { lokasi = it },
                label = { Text("Lokasi (Cth: Lab Mobile / Gedung 4 Lt. 2)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = deskripsi,
                onValueChange = { deskripsi = it },
                label = { Text("Deskripsi Detail Pengaduan / Aspirasi") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                maxLines = 6
            )

            // Bagian Tombol Pilih Bukti Foto & Pratinjau
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Lampiran Bukti Foto (Opsional):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedButton(
                        onClick = { launcher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (imageUri == null) "Pilih Foto dari Galeri" else "Ganti Foto Lampiran")
                    }

                    // Menampilkan preview foto jika sudah dipilih
                    if (imageUri != null) {
                        val inputStream = context.contentResolver.openInputStream(imageUri!!)
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        if (bitmap != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .padding(top = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Bukti Foto",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (nimMahasiswa.contains("Belum") || nimMahasiswa.contains("...")) {
                        Toast.makeText(context, "Data profil belum siap dari database Firestore!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (judul.isBlank() || lokasi.isBlank() || deskripsi.isBlank()) {
                        Toast.makeText(context, "Judul, Lokasi, dan Deskripsi tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (currentUser == null) {
                        Toast.makeText(context, "Sesi tidak valid, silakan login ulang", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isSubmitting = true
                    val docRef = firestore.collection("pengaduan").document()

                    val judulBersih = judul.trim()
                    val lokasiBersih = lokasi.trim()
                    val deskripsiBersih = deskripsi.trim()

                    val pengaduanBaru = Pengaduan(
                        idPengaduan = docRef.id,
                        uidPelapor = currentUser.uid,
                        nimPelapor = nimMahasiswa,
                        namaPelapor = namaMahasiswa,
                        kategori = kategori,
                        judul = judulBersih,
                        lokasi = lokasiBersih,
                        deskripsi = deskripsiBersih,
                        fotoBase64 = fotoBase64, // Menyimpan string Base64 foto
                        status = "Menunggu",
                        tanggapanAdmin = "",
                        tanggalLapor = Timestamp.now()
                    )

                    docRef.set(pengaduanBaru)
                        .addOnSuccessListener {
                            // Mengirim Push Email otomatis ke Admin Kampus di latar belakang
                            EmailSender.kirimNotifikasiKeAdmin(
                                namaPelapor = namaMahasiswa,
                                nimPelapor = nimMahasiswa,
                                prodiPelapor = prodiMahasiswa,
                                kategori = kategori,
                                judul = judulBersih,
                                lokasi = lokasiBersih,
                                deskripsi = deskripsiBersih
                            )

                            isSubmitting = false
                            Toast.makeText(context, "Laporan berhasil dikirim!", Toast.LENGTH_SHORT).show()
                            onSubmitSuccess()
                        }
                        .addOnFailureListener { e ->
                            isSubmitting = false
                            Toast.makeText(context, "Gagal mengirim: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isSubmitting && !isLoadingProfile
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("KIRIM PENGADUAN", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}