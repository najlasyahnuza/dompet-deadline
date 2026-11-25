package com.dompetdeadline.repository;

import com.dompetdeadline.model.Transaksi;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TransaksiRepository {

    // Mengambil semua transaksi lengkap dengan nama kategori, jenis, dan judul tugas (JOIN)
    public List<Transaksi> findAll() throws SQLException {
        List<Transaksi> list = new ArrayList<>();
        String sql = "SELECT tr.id, tr.tanggal, tr.jumlah, tr.kategori_id, tr.tugas_id, tr.catatan, " +
                     "k.nama AS nama_kategori, k.jenis AS jenis_kategori, t.judul AS judul_tugas " +
                     "FROM transaksi tr " +
                     "JOIN kategori k ON tr.kategori_id = k.id " +
                     "LEFT JOIN tugas t ON tr.tugas_id = t.id " +
                     "ORDER BY tr.tanggal DESC, tr.id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int rawTugasId = rs.getInt("tugas_id");
                Integer tugasId = rs.wasNull() ? null : rawTugasId;

                Transaksi tr = new Transaksi(
                    rs.getInt("id"),
                    LocalDate.parse(rs.getString("tanggal")),
                    rs.getLong("jumlah"),
                    rs.getInt("kategori_id"),
                    tugasId,
                    rs.getString("catatan")
                );
                tr.setNamaKategori(rs.getString("nama_kategori"));
                tr.setJenisKategori(rs.getString("jenis_kategori"));
                tr.setJudulTugas(rs.getString("judul_tugas"));
                list.add(tr);
            }
        }
        return list;
    }

    // Menambah transaksi baru (menangani tugas_id null jika tidak terhubung ke tugas)
    public void insert(Transaksi tr) throws SQLException {
        String sql = "INSERT INTO transaksi (tanggal, jumlah, kategori_id, tugas_id, catatan) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tr.getTanggal().toString());
            pstmt.setLong(2, tr.getJumlah());
            pstmt.setInt(3, tr.getKategoriId());
            if (tr.getTugasId() == null) {
                pstmt.setNull(4, Types.INTEGER);
            } else {
                pstmt.setInt(4, tr.getTugasId());
            }
            pstmt.setString(5, tr.getCatatan());
            pstmt.executeUpdate();
        }
    }

    // Memperbarui data transaksi
    public void update(Transaksi tr) throws SQLException {
        String sql = "UPDATE transaksi SET tanggal = ?, jumlah = ?, kategori_id = ?, tugas_id = ?, catatan = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tr.getTanggal().toString());
            pstmt.setLong(2, tr.getJumlah());
            pstmt.setInt(3, tr.getKategoriId());
            if (tr.getTugasId() == null) {
                pstmt.setNull(4, Types.INTEGER);
            } else {
                pstmt.setInt(4, tr.getTugasId());
            }
            pstmt.setString(5, tr.getCatatan());
            pstmt.setInt(6, tr.getId());
            pstmt.executeUpdate();
        }
    }

    // Menghapus transaksi berdasarkan ID
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM transaksi WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    // Menghitung total pengeluaran per kategori untuk grafik batang di Dashboard
    public Map<String, Long> getTotalPengeluaranPerKategori() throws SQLException {
        Map<String, Long> data = new LinkedHashMap<>();
        String sql = "SELECT k.nama, COALESCE(SUM(tr.jumlah), 0) AS total " +
                     "FROM kategori k " +
                     "LEFT JOIN transaksi tr ON k.id = tr.kategori_id " +
                     "WHERE k.jenis = 'PENGELUARAN' " +
                     "GROUP BY k.id, k.nama " +
                     "ORDER BY total DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                data.put(rs.getString("nama"), rs.getLong("total"));
            }
        }
        return data;
    }
}