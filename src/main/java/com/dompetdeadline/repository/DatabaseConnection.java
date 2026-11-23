package com.dompetdeadline.repository;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    // Database disimpan di folder user home (~/.dompet-deadline), bukan di dalam folder proyek
    private static final String DB_FOLDER = System.getProperty("user.home") + File.separator + ".dompet-deadline";
    private static final String DB_URL = "jdbc:sqlite:" + DB_FOLDER + File.separator + "dompet_deadline.db";

    public static Connection getConnection() throws SQLException {
        File folder = new File(DB_FOLDER);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        Connection conn = DriverManager.getConnection(DB_URL);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }

    public static void initDatabase() {
        String sqlKategori = "CREATE TABLE IF NOT EXISTS kategori (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nama TEXT NOT NULL, " +
                "jenis TEXT NOT NULL CHECK(jenis IN ('PEMASUKAN', 'PENGELUARAN'))" +
                ");";

        String sqlTugas = "CREATE TABLE IF NOT EXISTS tugas (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "judul TEXT NOT NULL, " +
                "mata_kuliah TEXT NOT NULL, " +
                "tenggat TEXT NOT NULL, " +
                "prioritas TEXT NOT NULL CHECK(prioritas IN ('TINGGI', 'SEDANG', 'RENDAH')), " +
                "selesai INTEGER NOT NULL DEFAULT 0" +
                ");";

        String sqlTransaksi = "CREATE TABLE IF NOT EXISTS transaksi (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "tanggal TEXT NOT NULL, " +
                "jumlah INTEGER NOT NULL, " +
                "kategori_id INTEGER NOT NULL, " +
                "tugas_id INTEGER, " +
                "catatan TEXT, " +
                "FOREIGN KEY (kategori_id) REFERENCES kategori(id), " +
                "FOREIGN KEY (tugas_id) REFERENCES tugas(id) ON DELETE SET NULL" +
                ");";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sqlKategori);
            stmt.execute(sqlTugas);
            stmt.execute(sqlTransaksi);
            seedDefaultKategori(conn);
        } catch (SQLException e) {
            throw new RuntimeException("Gagal menginisialisasi database SQLite: " + e.getMessage(), e);
        }
    }

    private static void seedDefaultKategori(Connection conn) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM kategori";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkSql)) {
            if (rs.next() && rs.getInt(1) > 0) {
                return;
            }
        }

        String insertSql = "INSERT INTO kategori (nama, jenis) VALUES (?, ?)";
        String[][] defaults = {
            {"Uang Saku / Kiriman", "PEMASUKAN"},
            {"Beasiswa / Honor", "PEMASUKAN"},
            {"Makan & Minum", "PENGELUARAN"},
            {"Cetak, Jilid & Alat Tulis", "PENGELUARAN"},
            {"Kuota & Internet", "PENGELUARAN"},
            {"Transportasi", "PENGELUARAN"}
        };

        try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
            for (String[] item : defaults) {
                pstmt.setString(1, item[0]);
                pstmt.setString(2, item[1]);
                pstmt.executeUpdate();
            }
        }
    }
}