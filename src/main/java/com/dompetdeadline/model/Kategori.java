package com.dompetdeadline.model;

public class Kategori {
    private int id;
    private String nama;
    private String jenis; // "PEMASUKAN" atau "PENGELUARAN"

    public Kategori() {
    }

    public Kategori(int id, String nama, String jenis) {
        this.id = id;
        this.nama = nama;
        this.jenis = jenis;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getJenis() {
        return jenis;
    }

    public void setJenis(String jenis) {
        this.jenis = jenis;
    }

    @Override
    public String toString() {
        return nama;
    }
}