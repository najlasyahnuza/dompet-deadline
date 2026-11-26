package com.dompetdeadline.repository;

import com.dompetdeadline.model.Kategori;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class KategoriRepository {
    public List<Kategori> findAll() throws SQLException {
        List<Kategori> list = new ArrayList<>();
        String sql = "SELECT id, nama, jenis FROM kategori ORDER BY jenis ASC, nama ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Kategori(
                    rs.getInt("id"),
                    rs.getString("nama"),
                    rs.getString("jenis")
                ));
            }
        }
        return list;
    }

    public void insert(String nama, String jenis) throws SQLException {
        String sql = "INSERT INTO kategori (nama, jenis) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nama);
            ps.setString(2, jenis);
            ps.executeUpdate();
        }
    }
}