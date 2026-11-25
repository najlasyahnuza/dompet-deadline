package com.dompetdeadline.service;

import com.dompetdeadline.model.Tugas;
import com.dompetdeadline.repository.TugasRepository;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TugasService {
    private final TugasRepository tugasRepository;

    public TugasService() {
        this.tugasRepository = new TugasRepository();
    }

    public TugasService(TugasRepository tugasRepository) {
        this.tugasRepository = tugasRepository;
    }

    public List<Tugas> getAllTugas() throws SQLException {
        return tugasRepository.findAll();
    }

    public void tambahTugas(String judul, String mataKuliah, LocalDateTime tenggat, String prioritas) throws SQLException {
        validasiTugas(judul, mataKuliah, tenggat, prioritas);
        Tugas tugas = new Tugas(0, judul.trim(), mataKuliah.trim(), tenggat, prioritas, false);
        tugasRepository.insert(tugas);
    }

    public void editTugas(int id, String judul, String mataKuliah, LocalDateTime tenggat, String prioritas, boolean selesai) throws SQLException {
        validasiTugas(judul, mataKuliah, tenggat, prioritas);
        Tugas tugas = new Tugas(id, judul.trim(), mataKuliah.trim(), tenggat, prioritas, selesai);
        tugasRepository.update(tugas);
    }

    public void ubahStatusSelesai(int id, boolean selesai) throws SQLException {
        tugasRepository.updateStatusSelesai(id, selesai);
    }

    public void hapusTugas(int id) throws SQLException {
        tugasRepository.delete(id);
    }

    // Mengambil daftar tugas belum selesai yang tenggatnya kurang dari atau sama dengan 24 jam ke depan
    public List<Tugas> getTugasMendesak24Jam(List<Tugas> daftarTugas, LocalDateTime waktuSekarang) {
        List<Tugas> mendesak = new ArrayList<>();
        for (Tugas t : daftarTugas) {
            if (!t.isSelesai() && t.getTenggat() != null) {
                long selisihMenit = Duration.between(waktuSekarang, t.getTenggat()).toMinutes();
                if (selisihMenit >= 0 && selisihMenit <= 24 * 60) {
                    mendesak.add(t);
                }
            }
        }
        return mendesak;
    }

    // Mengecek apakah sebuah tugas sudah lewat tenggat dan belum selesai
    public boolean isTerlambat(Tugas tugas, LocalDateTime waktuSekarang) {
        if (tugas == null || tugas.isSelesai() || tugas.getTenggat() == null) {
            return false;
        }
        return tugas.getTenggat().isBefore(waktuSekarang);
    }

    private void validasiTugas(String judul, String mataKuliah, LocalDateTime tenggat, String prioritas) {
        if (judul == null || judul.trim().isEmpty()) {
            throw new IllegalArgumentException("Judul tugas tidak boleh kosong.");
        }
        if (mataKuliah == null || mataKuliah.trim().isEmpty()) {
            throw new IllegalArgumentException("Mata kuliah tidak boleh kosong.");
        }
        if (tenggat == null) {
            throw new IllegalArgumentException("Waktu tenggat harus diisi.");
        }
        if (prioritas == null || (!prioritas.equals("TINGGI") && !prioritas.equals("SEDANG") && !prioritas.equals("RENDAH"))) {
            throw new IllegalArgumentException("Prioritas tugas tidak valid.");
        }
    }
}