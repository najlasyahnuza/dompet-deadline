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
    private final TransaksiRepository transaksiRepo;
    private final KategoriRepository kategoriRepo;

    public KeuanganService() {
        this.transaksiRepo = new TransaksiRepository();
        this.kategoriRepo = new KategoriRepository();
    }

    public KeuanganService(TransaksiRepository transaksiRepo, KategoriRepository kategoriRepo) {
        this.transaksiRepo = transaksiRepo;
        this.kategoriRepo = kategoriRepo;
    }

    public List<Kategori> getAllKategori() throws SQLException {
        return kategoriRepo.findAll();
    }

    public void tambahKategori(String nama, String jenis) throws SQLException {
        if (nama == null || nama.trim().isEmpty()) {
            throw new IllegalArgumentException("Nama kategori tidak boleh kosong. Contoh: Fotokopi & Print.");
        }
        if (jenis == null || (!jenis.equals("PEMASUKAN") && !jenis.equals("PENGELUARAN"))) {
            throw new IllegalArgumentException("Jenis kategori harus PEMASUKAN atau PENGELUARAN.");
        }
        kategoriRepo.insert(nama.trim(), jenis);
    }

    public List<Transaksi> getAllTransaksi() throws SQLException {
        return transaksiRepo.findAll();
    }

    public void tambahTransaksi(LocalDate tanggal, long jumlah, int kategoriId, Integer tugasId, String catatan) throws SQLException {
        validasiTransaksi(tanggal, jumlah, kategoriId);
        Transaksi t = new Transaksi(0, tanggal, jumlah, kategoriId, tugasId, catatan == null ? "" : catatan.trim());
        transaksiRepo.insert(t);
    }

    public void editTransaksi(int id, LocalDate tanggal, long jumlah, int kategoriId, Integer tugasId, String catatan) throws SQLException {
        validasiTransaksi(tanggal, jumlah, kategoriId);
        Transaksi t = new Transaksi(id, tanggal, jumlah, kategoriId, tugasId, catatan == null ? "" : catatan.trim());
        transaksiRepo.update(t);
    }

    public void hapusTransaksi(int id) throws SQLException {
        transaksiRepo.delete(id);
    }

    private void validasiTransaksi(LocalDate tanggal, long jumlah, int kategoriId) {
        if (tanggal == null) {
            throw new IllegalArgumentException("Tanggal transaksi wajib diisi dengan format YYYY-MM-DD.");
        }
        if (jumlah <= 0) {
            throw new IllegalArgumentException("Nominal transaksi harus lebih dari 0.");
        }
        if (kategoriId <= 0) {
            throw new IllegalArgumentException("Silakan pilih kategori transaksi terlebih dahulu.");
        }
    }

    public long hitungTotalPemasukan(List<Transaksi> daftar) {
        long total = 0;
        for (Transaksi t : daftar) {
            if ("PEMASUKAN".equalsIgnoreCase(t.getJenisKategori())) {
                total += t.getJumlah();
            }
        }
        return total;
    }

    public long hitungTotalPengeluaran(List<Transaksi> daftar) {
        long total = 0;
        for (Transaksi t : daftar) {
            if ("PENGELUARAN".equalsIgnoreCase(t.getJenisKategori())) {
                total += t.getJumlah();
            }
        }
        return total;
    }

    public long hitungSaldo(List<Transaksi> daftar) {
        return hitungTotalPemasukan(daftar) - hitungTotalPengeluaran(daftar);
    }

    public Map<String, Long> getPengeluaranPerKategori(String tahunBulan) throws SQLException {
        return transaksiRepo.getTotalPengeluaranPerKategori(tahunBulan);
    }
}