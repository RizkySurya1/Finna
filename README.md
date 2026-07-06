# Finna — Aplikasi Pencatat Keuangan Pribadi

Aplikasi Android native (Kotlin) untuk mencatat pemasukan/pengeluaran, melihat analitik, dan mengatur anggaran bulanan.

## Tech Stack
- Kotlin + View Binding
- Navigation Component (single-Activity, 4 fragment via Bottom Navigation)
- Room Database (local persistence)
- ViewModel + LiveData + Kotlin Flow + Coroutines
- MPAndroidChart (pie chart & bar chart)
- Material 3 Components

## Cara Membuka Project
1. Buka **Android Studio** (lihat versi minimum di bawah).
2. Pilih **Open** (bukan "New Project"), arahkan ke folder `Finna` ini.
3. Tunggu proses **Gradle Sync** selesai (pertama kali akan mengunduh dependency, perlu koneksi internet).
4. Klik **Run ▶** dengan target emulator/device Android **API 24 (Android 7.0)** ke atas.

## Struktur Project
```
app/src/main/java/com/example/finna/
├── FinnApp.kt                  # Application class, inisialisasi Room DB
├── MainActivity.kt             # Single activity + bottom navigation host
├── data/
│   ├── AppDatabase.kt
│   ├── model/                  # Entity: Transaction, Budget
│   └── dao/                    # TransactionDao, BudgetDao
├── ui/
│   ├── home/                   # Beranda: saldo, ringkasan, pie chart, transaksi terbaru
│   ├── transactions/           # Daftar semua transaksi
│   ├── analytics/              # Bar chart 6 bulan + pie chart kategori
│   ├── budget/                 # Progress anggaran per kategori
│   └── dialog/                 # Bottom sheet tambah/edit transaksi & anggaran
├── adapter/                    # RecyclerView adapters
└── util/Category.kt            # Master data kategori + formatter Rupiah
```

## Catatan
- Semua data tersimpan lokal di device (Room/SQLite) — tidak ada backend/API.
- Ikon launcher & ikon navigasi sudah dibuatkan sebagai vector drawable bawaan supaya project langsung bisa di-build tanpa langkah manual "Vector Asset".
