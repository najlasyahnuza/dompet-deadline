package com.dompetdeadline.repository;

import com.dompetdeadline.model.Kategori;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class KategoriRepository {

    public List<Kategori> findAll() throws SQLException {
        List<Kategori> list = new ArrayList<>();
        String sql = "SELECT id, nama, jenis FROM kategori ORDER BY jenis ASC, nama ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
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

    public List<Kategori> findByJenis(String jenis) throws SQLException {
        List<Kategori> list = new ArrayList<>();
        String sql = "SELECT id, nama, jenis FROM kategori WHERE jenis = ? ORDER BY nama ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, jenis);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Kategori(
                        rs.getInt("id"),
                        rs.getString("nama"),
                        rs.getString("jenis")
                    ));
                }
            }
        }
        return list;
    }

    public void insert(Kategori kategori) throws SQLException {
        String sql = "INSERT INTO kategori (nama, jenis) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, kategori.getNama());
            pstmt.setString(2, kategori.getJenis());
            pstmt.executeUpdate();
        }
    }
}