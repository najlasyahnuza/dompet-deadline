package com.dompetdeadline.service;

import com.dompetdeadline.model.Transaksi;
import com.dompetdeadline.model.Tugas;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ServiceTest {

    @Test
    public void testPerhitunganSaldoPemasukanPengeluaran() {
        KeuanganService service = new KeuanganService(null, null);

        Transaksi t1 = new Transaksi(1, LocalDate.now(), 500000, 1, null, "Uang saku");
        t1.setJenisKategori("PEMASUKAN");

        Transaksi t2 = new Transaksi(2, LocalDate.now(), 125000, 2, 1, "Cetak laporan");
        t2.setJenisKategori("PENGELUARAN");

        Transaksi t3 = new Transaksi(3, LocalDate.now(), 75000, 3, null, "Makan siang");
        t3.setJenisKategori("PENGELUARAN");

        List<Transaksi> daftar = Arrays.asList(t1, t2, t3);

        assertEquals(500000, service.hitungTotalPemasukan(daftar));
        assertEquals(200000, service.hitungTotalPengeluaran(daftar));
        assertEquals(300000, service.hitungSaldo(daftar));
    }

    @Test
    public void testValidasiNominalTransaksiNegatifAtauNol() {
        KeuanganService service = new KeuanganService(null, null);

        Exception exNol = assertThrows(IllegalArgumentException.class, () ->
            service.tambahTransaksi(LocalDate.now(), 0, 1, null, "Tes nol")
        );
        assertTrue(exNol.getMessage().contains("0"));

        Exception exNegatif = assertThrows(IllegalArgumentException.class, () ->
            service.tambahTransaksi(LocalDate.now(), -5000, 1, null, "Tes negatif")
        );
        assertTrue(exNegatif.getMessage().contains("0"));
    }

    @Test
    public void testPenentuanTugasMendesak24Jam() {
        TugasService service = new TugasService(null);
        LocalDateTime sekarang = LocalDateTime.of(2026, 11, 26, 10, 0);

        Tugas mendesak = new Tugas(1, "Laporan PBO", "PBO", sekarang.plusHours(12), "TINGGI", false);
        Tugas masihLama = new Tugas(2, "Proyek Basis Data", "BDL", sekarang.plusDays(3), "SEDANG", false);
        Tugas sudahSelesai = new Tugas(3, "Quiz Jarkom", "Jarkom", sekarang.plusHours(4), "TINGGI", true);

        List<Tugas> hasil = service.getTugasMendesak24Jam(Arrays.asList(mendesak, masihLama, sudahSelesai), sekarang);

        assertEquals(1, hasil.size());
        assertEquals("Laporan PBO", hasil.get(0).getJudul());
    }

    @Test
    public void testStatusTerlambatTugas() {
        TugasService service = new TugasService(null);
        LocalDateTime sekarang = LocalDateTime.of(2026, 11, 26, 10, 0);

        Tugas telat = new Tugas(1, "Tugas Telat", "OS", sekarang.minusHours(2), "TINGGI", false);
        Tugas selesaiTelat = new Tugas(2, "Tugas Selesai", "OS", sekarang.minusHours(2), "TINGGI", true);

        assertTrue(service.isTerlambat(telat, sekarang));
        assertFalse(service.isTerlambat(selesaiTelat, sekarang));
    }
}