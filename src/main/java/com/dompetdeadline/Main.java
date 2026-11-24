package com.dompetdeadline;

import com.dompetdeadline.model.Kategori;
import com.dompetdeadline.repository.DatabaseConnection;
import com.dompetdeadline.repository.KategoriRepository;

public class Main {
    public static void main(String[] args) {
        try {
            DatabaseConnection.initDatabase();
            KategoriRepository repo = new KategoriRepository();
            System.out.println("Koneksi SQLite berhasil! Daftar kategori awal:");
            for (Kategori k : repo.findAll()) {
                System.out.println("- " + k.getNama() + " [" + k.getJenis() + "]");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}