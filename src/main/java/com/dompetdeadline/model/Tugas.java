package com.dompetdeadline.model;

import java.time.LocalDateTime;

public class Tugas {
    private int id;
    private String judul;
    private String mataKuliah;
    private LocalDateTime tenggat;
    private String prioritas; // "TINGGI", "SEDANG", atau "RENDAH"
    private boolean selesai;
    private long totalBiaya;

    public Tugas() {
    }

    public Tugas(int id, String judul, String mataKuliah, LocalDateTime tenggat, String prioritas, boolean selesai) {
        this.id = id;
        this.judul = judul;
        this.mataKuliah = mataKuliah;
        this.tenggat = tenggat;
        this.prioritas = prioritas;
        this.selesai = selesai;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getJudul() { return judul; }
    public void setJudul(String judul) { this.judul = judul; }

    public String getMataKuliah() { return mataKuliah; }
    public void setMataKuliah(String mataKuliah) { this.mataKuliah = mataKuliah; }

    public LocalDateTime getTenggat() { return tenggat; }
    public void setTenggat(LocalDateTime tenggat) { this.tenggat = tenggat; }

    public String getPrioritas() { return prioritas; }
    public void setPrioritas(String prioritas) { this.prioritas = prioritas; }

    public boolean isSelesai() { return selesai; }
    public void setSelesai(boolean selesai) { this.selesai = selesai; }

    public long getTotalBiaya() { return totalBiaya; }
    public void setTotalBiaya(long totalBiaya) { this.totalBiaya = totalBiaya; }

    @Override
    public String toString() {
        return judul + " (" + mataKuliah + ")";
    }
}