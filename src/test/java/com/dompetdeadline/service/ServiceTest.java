package com.dompetdeadline.service;

import com.dompetdeadline.model.Transaksi;
import com.dompetdeadline.model.Tugas;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ServiceTest {

    private KeuanganService keuanganService;
    private TugasService tugasService;

    @BeforeEach
    void setUp() {
        keuanganService = new KeuanganService(null, null);
        tugasService = new TugasService(null);
    }

    @Test
    void testHitungSaldoDanTotalKeuangan() {
        Transaksi t1 = new Transaksi(1, LocalDate.now(), 500000L, 1, null, "Uang saku bulanan");
        t1.setJenisKategori("PEMASUKAN");

        Transaksi t2 = new Transaksi(2, LocalDate.now(), 35000L, 4, 1, "Print dan jilid laporan");
        t2.setJenisKategori("PENGELUARAN");

        Transaksi t3 = new Transaksi(3, LocalDate.now(), 15000L, 3, null, "Makan siang");
        t3.setJenisKategori("PENGELUARAN");

        List<Transaksi> daftar = Arrays.asList(t1, t2, t3);

        assertEquals(500000L, keuanganService.hitungTotalPemasukan(daftar));
        assertEquals(50000L, keuanganService.hitungTotalPengeluaran(daftar));
        assertEquals(450000L, keuanganService.hitungSaldo(daftar));
    }

    @Test
    void testValidasiNominalTransaksiNegatifAtauNol() {
        Exception exNol = assertThrows(IllegalArgumentException.class, () -> {
            keuanganService.tambahTransaksi(LocalDate.now(), 0L, 1, null, "Tes nol");
        });
        assertTrue(exNol.getMessage().contains("lebih besar dari Rp 0"));

        Exception exNegatif = assertThrows(IllegalArgumentException.class, () -> {
            keuanganService.tambahTransaksi(LocalDate.now(), -10000L, 1, null, "Tes negatif");
        });
        assertTrue(exNegatif.getMessage().contains("lebih besar dari Rp 0"));
    }

    @Test
    void testFilterTugasMendesak24Jam() {
        LocalDateTime sekarang = LocalDateTime.of(2026, 11, 25, 12, 0);

        // Tenggat 5 jam lagi & belum selesai -> harus masuk pengingat
        Tugas mendesak = new Tugas(1, "Laporan Praktikum PBO", "PBO", sekarang.plusHours(5), "TINGGI", false);
        // Tenggat 3 jam lagi tapi sudah selesai -> tidak masuk pengingat
        Tugas sudahSelesai = new Tugas(2, "Quiz Basis Data", "BDL", sekarang.plusHours(3), "SEDANG", true);
        // Tenggat 48 jam lagi -> belum masuk 24 jam
        Tugas masihLama = new Tugas(3, "Makalah Jaringan", "Jarkom", sekarang.plusHours(48), "RENDAH", false);

        List<Tugas> hasil = tugasService.getTugasMendesak24Jam(Arrays.asList(mendesak, sudahSelesai, masihLama), sekarang);

        assertEquals(1, hasil.size());
        assertEquals("Laporan Praktikum PBO", hasil.get(0).getJudul());
    }

    @Test
    void testDeteksiTugasTerlambat() {
        LocalDateTime sekarang = LocalDateTime.of(2026, 11, 25, 12, 0);

        Tugas telat = new Tugas(1, "Tugas Kalkulus", "Kalkulus", sekarang.minusHours(2), "TINGGI", false);
        Tugas telatTapiSelesai = new Tugas(2, "Tugas Fisika", "Fisika", sekarang.minusHours(5), "SEDANG", true);

        assertTrue(tugasService.isTerlambat(telat, sekarang));
        assertFalse(tugasService.isTerlambat(telatTapiSelesai, sekarang));
    }
}