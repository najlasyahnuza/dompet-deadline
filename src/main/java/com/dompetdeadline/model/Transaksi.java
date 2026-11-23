package com.dompetdeadline.model;

import java.time.LocalDate;

public class Transaksi {
    // Atribut inti sesuai kolom tabel 'transaksi' di database
    private int id;
    private LocalDate tanggal;
    private long jumlah; // Wajib bilangan bulat Rupiah (long), bukan double
    private int kategoriId;
    private Integer tugasId; // Pakai Integer (objek), bukan int, karena boleh null jika tidak terkait tugas
    private String catatan;

    // Atribut tambahan hasil JOIN tabel kategori & tugas (untuk ditampilkan di tabel dan ekspor CSV)
    private String namaKategori;
    private String jenisKategori;
    private String judulTugas;

    public Transaksi() {
    }

    public Transaksi(int id, LocalDate tanggal, long jumlah, int kategoriId, Integer tugasId, String catatan) {
        this.id = id;
        this.tanggal = tanggal;
        this.jumlah = jumlah;
        this.kategoriId = kategoriId;
        this.tugasId = tugasId;
        this.catatan = catatan;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getTanggal() {
        return tanggal;
    }

    public void setTanggal(LocalDate tanggal) {
        this.tanggal = tanggal;
    }

    public long getJumlah() {
        return jumlah;
    }

    public void setJumlah(long jumlah) {
        this.jumlah = jumlah;
    }

    public int getKategoriId() {
        return kategoriId;
    }

    public void setKategoriId(int kategoriId) {
        this.kategoriId = kategoriId;
    }

    public Integer getTugasId() {
        return tugasId;
    }

    public void setTugasId(Integer tugasId) {
        this.tugasId = tugasId;
    }

    public String getCatatan() {
        return catatan;
    }

    public void setCatatan(String catatan) {
        this.catatan = catatan;
    }

    public String getNamaKategori() {
        return namaKategori;
    }

    public void setNamaKategori(String namaKategori) {
        this.namaKategori = namaKategori;
    }

    public String getJenisKategori() {
        return jenisKategori;
    }

    public void setJenisKategori(String jenisKategori) {
        this.jenisKategori = jenisKategori;
    }

    public String getJudulTugas() {
        return judulTugas;
    }

    public void setJudulTugas(String judulTugas) {
        this.judulTugas = judulTugas;
    }
}