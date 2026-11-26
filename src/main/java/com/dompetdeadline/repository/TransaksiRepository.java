package com.dompetdeadline.repository;

import com.dompetdeadline.model.Transaksi;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TransaksiRepository {
    public List<Transaksi> findAll() throws SQLException {
        List<Transaksi> list = new ArrayList<>();
        String sql = "SELECT tr.id, tr.tanggal, tr.jumlah, tr.kategori_id, k.nama AS nama_kategori, " +
                     "k.jenis AS jenis_kategori, tr.tugas_id, tg.judul AS judul_tugas, tr.catatan " +
                     "FROM transaksi tr " +
                     "JOIN kategori k ON tr.kategori_id = k.id " +
                     "LEFT JOIN tugas tg ON tr.tugas_id = tg.id " +
                     "ORDER BY tr.tanggal DESC, tr.id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int tugasIdRaw = rs.getInt("tugas_id");
                Integer tugasId = rs.wasNull() ? null : tugasIdRaw;

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

    public void insert(Transaksi tr) throws SQLException {
        String sql = "INSERT INTO transaksi (tanggal, jumlah, kategori_id, tugas_id, catatan) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tr.getTanggal().toString());
            ps.setLong(2, tr.getJumlah());
            ps.setInt(3, tr.getKategoriId());
            if (tr.getTugasId() == null) {
                ps.setNull(4, Types.INTEGER);
            } else {
                ps.setInt(4, tr.getTugasId());
            }
            ps.setString(5, tr.getCatatan());
            ps.executeUpdate();
        }
    }

    public void update(Transaksi tr) throws SQLException {
        String sql = "UPDATE transaksi SET tanggal = ?, jumlah = ?, kategori_id = ?, tugas_id = ?, catatan = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tr.getTanggal().toString());
            ps.setLong(2, tr.getJumlah());
            ps.setInt(3, tr.getKategoriId());
            if (tr.getTugasId() == null) {
                ps.setNull(4, Types.INTEGER);
            } else {
                ps.setInt(4, tr.getTugasId());
            }
            ps.setString(5, tr.getCatatan());
            ps.setInt(6, tr.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM transaksi WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public Map<String, Long> getTotalPengeluaranPerKategori(String tahunBulan) throws SQLException {
        Map<String, Long> hasil = new LinkedHashMap<>();
        boolean filterBulan = tahunBulan != null && !tahunBulan.equals("SEMUA");

        String sql = "SELECT k.nama, COALESCE(SUM(tr.jumlah), 0) AS total " +
                     "FROM kategori k " +
                     "LEFT JOIN transaksi tr ON k.id = tr.kategori_id " +
                     (filterBulan ? "AND strftime('%Y-%m', tr.tanggal) = ? " : "") +
                     "WHERE k.jenis = 'PENGELUARAN' " +
                     "GROUP BY k.id, k.nama " +
                     "ORDER BY total DESC, k.nama ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (filterBulan) {
                ps.setString(1, tahunBulan);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    hasil.put(rs.getString("nama"), rs.getLong("total"));
                }
            }
        }
        return hasil;
    }
}