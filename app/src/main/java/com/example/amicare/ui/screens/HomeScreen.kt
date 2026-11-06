package com.example.amicare.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.amicare.model.Pengaduan
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToBuatPengaduan: () -> Unit,
    onLogoutSuccess: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser

    var daftarPengaduan by remember { mutableStateOf<List<Pengaduan>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // 0 = Tab Beranda (Menunggu & Diproses), 1 = Tab Riwayat (Selesai & Ditolak)
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    // Menyimpan laporan yang sedang diklik untuk menampilkan halaman Detail
    var selectedPengaduan by remember { mutableStateOf<Pengaduan?>(null) }

    // Mengambil data laporan secara real-time dari Firestore
    LaunchedEffect(currentUser?.uid) {
        if (currentUser != null) {
            firestore.collection("pengaduan")
                .whereEqualTo("uidPelapor", currentUser.uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        isLoading = false
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val list = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(Pengaduan::class.java)
                        }.sortedByDescending { it.tanggalLapor?.seconds ?: 0L }

                        daftarPengaduan = list
                    }
                    isLoading = false
                }
        }
    }

    // Jika ada kartu laporan yang diklik, tampilkan halaman Detail Pengaduan
    if (selectedPengaduan != null) {
        val currentDetail = daftarPengaduan.find { it.idPengaduan == selectedPengaduan!!.idPengaduan } ?: selectedPengaduan!!
        DetailPengaduanView(
            pengaduan = currentDetail,
            onBack = { selectedPengaduan = null }
        )
        return
    }

    // Memisahkan daftar laporan untuk Beranda (Aktif) dan Riwayat (Selesai / Ditolak)
    val laporanBerandaAktif = daftarPengaduan.filter {
        it.status.equals("Menunggu", ignoreCase = true) ||
                it.status.equals("Diproses", ignoreCase = true)
    }

    val laporanRiwayat = daftarPengaduan.filter {
        it.status.equals("Selesai", ignoreCase = true) ||
                it.status.equals("Ditolak", ignoreCase = true)
    }

    val listYangDitampilkan = if (selectedTabIndex == 0) laporanBerandaAktif else laporanRiwayat

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Amicare - Layanan Pengaduan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = currentUser?.email ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            auth.signOut()
                            onLogoutSuccess()
                        }
                    ) {
                        Text(
                            text = "Keluar",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        },
        floatingActionButton = {
            if (selectedTabIndex == 0) {
                ExtendedFloatingActionButton(
                    onClick = onNavigateToBuatPengaduan,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Text(
                        text = "+ Lapor Baru",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Menggunakan PrimaryTabRow sesuai standar Material 3 terbaru
            PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Text(
                            text = "Beranda (${laporanBerandaAktif.size})",
                            fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Text(
                            text = "Riwayat (${laporanRiwayat.size})",
                            fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (listYangDitampilkan.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (selectedTabIndex == 0) {
                            "Tidak ada laporan yang sedang aktif.\nKlik '+ Lapor Baru' untuk membuat pengaduan atau aspirasi."
                        } else {
                            "Belum ada riwayat laporan yang Selesai atau Ditolak."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(listYangDitampilkan) { item ->
                        PengaduanCard(
                            pengaduan = item,
                            onClick = { selectedPengaduan = item }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PengaduanCard(
    pengaduan: Pengaduan,
    onClick: () -> Unit
) {
    val warnaStatus = when (pengaduan.status.lowercase()) {
        "selesai" -> Color(0xFF2E7D32) // Hijau
        "diproses" -> Color(0xFFEF6C00) // Oranye
        "ditolak" -> Color(0xFFC62828) // Merah
        else -> Color(0xFF1565C0) // Biru (Menunggu)
    }

    val formatTanggal = remember(pengaduan.tanggalLapor) {
        val date = pengaduan.tanggalLapor?.toDate()
        if (date != null) {
            SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(date)
        } else {
            "-"
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = pengaduan.kategori,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = warnaStatus.copy(alpha = 0.15f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = pengaduan.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = warnaStatus,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = pengaduan.judul,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Lokasi: ${pengaduan.lokasi} • $formatTanggal",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = pengaduan.deskripsi,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (pengaduan.tanggapanAdmin.isNotBlank()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text(
                    text = "Tanggapan Admin: ${pengaduan.tanggapanAdmin}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = "Ketuk untuk lihat detail lengkap →",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailPengaduanView(
    pengaduan: Pengaduan,
    onBack: () -> Unit
) {
    val warnaStatus = when (pengaduan.status.lowercase()) {
        "selesai" -> Color(0xFF2E7D32)
        "diproses" -> Color(0xFFEF6C00)
        "ditolak" -> Color(0xFFC62828)
        else -> Color(0xFF1565C0)
    }

    val formatTanggal = remember(pengaduan.tanggalLapor) {
        val date = pengaduan.tanggalLapor?.toDate()
        if (date != null) {
            SimpleDateFormat("dd MMMM yyyy, HH:mm WIB", Locale.getDefault()).format(date)
        } else {
            "-"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Detail Laporan",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("< Kembali", fontWeight = FontWeight.Bold)
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Bagian Status & Tanggal
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = warnaStatus.copy(alpha = 0.12f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Status Laporan Saat Ini",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = pengaduan.status.uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = warnaStatus
                        )
                    }
                    Text(
                        text = formatTanggal,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Bagian Informasi Pelapor & Lokasi
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Informasi Laporan",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    HorizontalDivider()
                    Text(text = "Pelapor: ${pengaduan.namaPelapor} (${pengaduan.nimPelapor})", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "Kategori: ${pengaduan.kategori}", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "Lokasi: ${pengaduan.lokasi}", style = MaterialTheme.typography.bodyMedium)
                }
            }

            // Bagian Isi Pengaduan / Aspirasi Lengkap
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = pengaduan.judul,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    HorizontalDivider()
                    Text(
                        text = pengaduan.deskripsi,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Bagian Tanggapan Admin menggunakan .ifBlank
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Tanggapan Admin Kampus",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    HorizontalDivider()
                    Text(
                        text = pengaduan.tanggapanAdmin.ifBlank { "Belum ada tanggapan dari admin." },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}