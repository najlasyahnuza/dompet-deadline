package com.dompetdeadline.service;

import com.dompetdeadline.model.Kategori;
import com.dompetdeadline.model.Transaksi;
import com.dompetdeadline.repository.KategoriRepository;
import com.dompetdeadline.repository.TransaksiRepository;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class KeuanganService {
    private final TransaksiRepository transaksiRepository;
    private final KategoriRepository kategoriRepository;

    public KeuanganService() {
        this.transaksiRepository = new TransaksiRepository();
        this.kategoriRepository = new KategoriRepository();
    }

    public KeuanganService(TransaksiRepository transaksiRepository, KategoriRepository kategoriRepository) {
        this.transaksiRepository = transaksiRepository;
        this.kategoriRepository = kategoriRepository;
    }

    public List<Transaksi> getAllTransaksi() throws SQLException {
        return transaksiRepository.findAll();
    }

    public List<Kategori> getAllKategori() throws SQLException {
        return kategoriRepository.findAll();
    }

    public void tambahTransaksi(LocalDate tanggal, long jumlah, int kategoriId, Integer tugasId, String catatan) throws SQLException {
        validasiTransaksi(tanggal, jumlah, kategoriId);
        Transaksi tr = new Transaksi(0, tanggal, jumlah, kategoriId, tugasId, catatan == null ? "" : catatan.trim());
        transaksiRepository.insert(tr);
    }

    public void editTransaksi(int id, LocalDate tanggal, long jumlah, int kategoriId, Integer tugasId, String catatan) throws SQLException {
        validasiTransaksi(tanggal, jumlah, kategoriId);
        Transaksi tr = new Transaksi(id, tanggal, jumlah, kategoriId, tugasId, catatan == null ? "" : catatan.trim());
        transaksiRepository.update(tr);
    }

    public void hapusTransaksi(int id) throws SQLException {
        transaksiRepository.delete(id);
    }

    public Map<String, Long> getPengeluaranPerKategori() throws SQLException {
        return transaksiRepository.getTotalPengeluaranPerKategori();
    }

    // Menghitung total pemasukan dari daftar transaksi
    public long hitungTotalPemasukan(List<Transaksi> daftarTransaksi) {
        long total = 0;
        for (Transaksi tr : daftarTransaksi) {
            if ("PEMASUKAN".equalsIgnoreCase(tr.getJenisKategori())) {
                total += tr.getJumlah();
            }
        }
        return total;
    }

    // Menghitung total pengeluaran dari daftar transaksi
    public long hitungTotalPengeluaran(List<Transaksi> daftarTransaksi) {
        long total = 0;
        for (Transaksi tr : daftarTransaksi) {
            if ("PENGELUARAN".equalsIgnoreCase(tr.getJenisKategori())) {
                total += tr.getJumlah();
            }
        }
        return total;
    }

    // Menghitung saldo bersih (Pemasukan - Pengeluaran)
    public long hitungSaldo(List<Transaksi> daftarTransaksi) {
        return hitungTotalPemasukan(daftarTransaksi) - hitungTotalPengeluaran(daftarTransaksi);
    }

    private void validasiTransaksi(LocalDate tanggal, long jumlah, int kategoriId) {
        if (tanggal == null) {
            throw new IllegalArgumentException("Tanggal transaksi tidak boleh kosong.");
        }
        if (jumlah <= 0) {
            throw new IllegalArgumentException("Nominal transaksi harus lebih besar dari Rp 0.");
        }
        if (kategoriId <= 0) {
            throw new IllegalArgumentException("Kategori transaksi wajib dipilih.");
        }
    }
}