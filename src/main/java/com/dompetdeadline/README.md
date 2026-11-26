# Dompet Deadline

Aplikasi desktop berbasis Java Swing dan SQLite untuk membantu mahasiswa mengelola pemasukan/pengeluaran harian sekaligus memantau daftar tugas kuliah beserta biaya yang terkait dengan setiap tugas.

## Identitas Pengembang
- **Nama:** Najla Putri Syahnuza
- **NPM:** 250810701100115
- **Program Studi:** Informatika, FMIPA Universitas Syiah Kuala (USK)

## Fitur Utama
- **Manajemen Transaksi Keuangan (CRUD):** Pencatatan pemasukan dan pengeluaran berdasarkan kategori, dilengkapi kalkulasi otomatis Saldo Bersih, Total Pemasukan, dan Total Pengeluaran.
- **Manajemen Tugas & Tenggat Kuliah (CRUD):** Pencatatan tugas berdasarkan mata kuliah, waktu tenggat, tingkat prioritas (`TINGGI`, `SEDANG`, `RENDAH`), status penyelesaian, serta kalkulasi total biaya pengeluaran per tugas.
- **Pengingat Tenggat Mendesak & Penanda Terlambat:** Peringatan otomatis saat aplikasi dibuka untuk tugas belum selesai dengan tenggat $\le 24$ jam, serta penyorotan baris merah untuk tugas yang melewati tenggat.
- **Visualisasi Grafik Pengeluaran:** Grafik batang kustom berbasis Java2D (`Graphics2D`) untuk membandingkan total pengeluaran per kategori secara real-time.
- **Ekspor Laporan CSV:** Fitur ekspor seluruh riwayat transaksi ke format `.csv` melalui `JFileChooser`.

## Arsitektur & Struktur Paket
Proyek ini menggunakan pola arsitektur berlapis (*Layered Architecture*):
```text
src/
├── main/java/com/dompetdeadline/
│   ├── Main.java                  # Entry point aplikasi
│   ├── model/                     # Entitas domain (Kategori, Tugas, Transaksi)
│   ├── repository/                # Akses database SQLite (JDBC & PreparedStatement)
│   ├── service/                   # Logika bisnis, validasi input, & kalkulasi
│   └── ui/                        # Antarmuka pengguna Java Swing & grafik Java2D
└── test/java/com/dompetdeadline/
    └── service/ServiceTest.java   # Unit test JUnit 5 untuk lapisan service