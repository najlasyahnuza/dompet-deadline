package com.dompetdeadline.repository;

import com.dompetdeadline.model.Tugas;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TugasRepository {

    // Mengambil semua tugas beserta total pengeluaran yang terkait dengan masing-masing tugas
    public List<Tugas> findAll() throws SQLException {
        List<Tugas> list = new ArrayList<>();
        String sql = "SELECT t.id, t.judul, t.mata_kuliah, t.tenggat, t.prioritas, t.selesai, " +
                     "COALESCE(SUM(CASE WHEN k.jenis = 'PENGELUARAN' THEN tr.jumlah ELSE 0 END), 0) AS total_biaya " +
                     "FROM tugas t " +
                     "LEFT JOIN transaksi tr ON t.id = tr.tugas_id " +
                     "LEFT JOIN kategori k ON tr.kategori_id = k.id " +
                     "GROUP BY t.id, t.judul, t.mata_kuliah, t.tenggat, t.prioritas, t.selesai " +
                     "ORDER BY t.selesai ASC, t.tenggat ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Tugas tugas = new Tugas(
                    rs.getInt("id"),
                    rs.getString("judul"),
                    rs.getString("mata_kuliah"),
                    LocalDateTime.parse(rs.getString("tenggat")),
                    rs.getString("prioritas"),
                    rs.getInt("selesai") == 1
                );
                tugas.setTotalBiaya(rs.getLong("total_biaya"));
                list.add(tugas);
            }
        }
        return list;
    }

    // Menambah tugas baru
    public void insert(Tugas tugas) throws SQLException {
        String sql = "INSERT INTO tugas (judul, mata_kuliah, tenggat, prioritas, selesai) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tugas.getJudul());
            pstmt.setString(2, tugas.getMataKuliah());
            pstmt.setString(3, tugas.getTenggat().toString());
            pstmt.setString(4, tugas.getPrioritas());
            pstmt.setInt(5, tugas.isSelesai() ? 1 : 0);
            pstmt.executeUpdate();
        }
    }

    // Memperbarui data tugas yang sudah ada
    public void update(Tugas tugas) throws SQLException {
        String sql = "UPDATE tugas SET judul = ?, mata_kuliah = ?, tenggat = ?, prioritas = ?, selesai = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tugas.getJudul());
            pstmt.setString(2, tugas.getMataKuliah());
            pstmt.setString(3, tugas.getTenggat().toString());
            pstmt.setString(4, tugas.getPrioritas());
            pstmt.setInt(5, tugas.isSelesai() ? 1 : 0);
            pstmt.setInt(6, tugas.getId());
            pstmt.executeUpdate();
        }
    }

    // Menandai tugas selesai / belum selesai secara cepat (checkbox)
    public void updateStatusSelesai(int id, boolean selesai) throws SQLException {
        String sql = "UPDATE tugas SET selesai = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, selesai ? 1 : 0);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        }
    }

    // Menghapus tugas (kolom tugas_id di transaksi terkait otomatis menjadi NULL karena ON DELETE SET NULL)
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM tugas WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }
}   