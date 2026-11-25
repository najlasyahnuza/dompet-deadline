package com.dompetdeadline.ui;

import com.dompetdeadline.model.Kategori;
import com.dompetdeadline.model.Transaksi;
import com.dompetdeadline.model.Tugas;
import com.dompetdeadline.service.KeuanganService;
import com.dompetdeadline.service.TugasService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainFrame extends JFrame {
    private final KeuanganService keuanganService = new KeuanganService();
    private final TugasService tugasService = new TugasService();
    private final NumberFormat rupiahFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
    private final DateTimeFormatter dtfTenggat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private JLabel lblSaldo;
    private JLabel lblPemasukan;
    private JLabel lblPengeluaran;
    private GrafikPengeluaranPanel grafikPanel;

    private JTable tblTransaksi;
    private DefaultTableModel modelTransaksi;
    private List<Transaksi> cacheTransaksi = new ArrayList<>();

    private JTable tblTugas;
    private DefaultTableModel modelTugas;
    private List<Tugas> cacheTugas = new ArrayList<>();

    public MainFrame() {
        setTitle("Dompet Deadline - Manajemen Keuangan & Tugas Mahasiswa");
        setSize(1180, 720);
        setMinimumSize(new Dimension(980, 620));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        rupiahFormat.setMaximumFractionDigits(0);
        initComponents();
        muatSemuaData(true);
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(14, 14, 14, 14));
        root.setBackground(new Color(241, 245, 249));

        // === PANEL ATAS: Kartu Ringkasan + Grafik ===
        JPanel topPanel = new JPanel(new BorderLayout(12, 12));
        topPanel.setOpaque(false);

        JPanel cardsPanel = new JPanel(new GridLayout(3, 1, 8, 8));
        cardsPanel.setOpaque(false);
        cardsPanel.setPreferredSize(new Dimension(320, 190));

        lblSaldo = new JLabel("Rp 0");
        lblPemasukan = new JLabel("Rp 0");
        lblPengeluaran = new JLabel("Rp 0");

        cardsPanel.add(buatKartuRingkasan("Saldo Bersih Saat Ini", lblSaldo, new Color(30, 64, 175)));
        cardsPanel.add(buatKartuRingkasan("Total Pemasukan", lblPemasukan, new Color(21, 128, 61)));
        cardsPanel.add(buatKartuRingkasan("Total Pengeluaran", lblPengeluaran, new Color(185, 28, 28)));

        grafikPanel = new GrafikPengeluaranPanel();
        grafikPanel.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));

        topPanel.add(cardsPanel, BorderLayout.WEST);
        topPanel.add(grafikPanel, BorderLayout.CENTER);

        // === PANEL TENGAH: Split Pane Transaksi (Kiri) & Tugas (Kanan) ===
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buatPanelTransaksi(), buatPanelTugas());
        splitPane.setResizeWeight(0.50);
        splitPane.setDividerSize(8);
        splitPane.setBorder(null);

        root.add(topPanel, BorderLayout.NORTH);
        root.add(splitPane, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel buatKartuRingkasan(String judul, JLabel labelNilai, Color aksen) {
        JPanel card = new JPanel(new BorderLayout(4, 2));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 5, 0, 0, aksen),
            new EmptyBorder(8, 12, 8, 12)
        ));

        JLabel lblJudul = new JLabel(judul);
        lblJudul.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblJudul.setForeground(new Color(100, 116, 139));

        labelNilai.setFont(new Font("SansSerif", Font.BOLD, 17));
        labelNilai.setForeground(aksen);

        card.add(lblJudul, BorderLayout.NORTH);
        card.add(labelNilai, BorderLayout.CENTER);
        return card;
    }

    private JPanel buatPanelTransaksi() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225)),
            new EmptyBorder(10, 10, 10, 10)
        ));

        JPanel header = new JPanel(new BorderLayout(0, 6));
        header.setOpaque(false);
        JLabel title = new JLabel("Catatan Keuangan (Pemasukan & Pengeluaran)");
        title.setFont(new Font("SansSerif", Font.BOLD, 14));

        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btnGroup.setOpaque(false);
        JButton btnTambah = new JButton("+ Transaksi");
        JButton btnEdit = new JButton("Edit");
        JButton btnHapus = new JButton("Hapus");
        JButton btnCsv = new JButton("Ekspor CSV");

        btnTambah.addActionListener(e -> bukaDialogTransaksi(null));
        btnEdit.addActionListener(e -> {
            int row = tblTransaksi.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Pilih baris transaksi yang ingin diubah.");
                return;
            }
            bukaDialogTransaksi(cacheTransaksi.get(row));
        });
        btnHapus.addActionListener(e -> hapusTransaksiTerpilih());
        btnCsv.addActionListener(e -> eksporTransaksiCsv());

        btnGroup.add(btnTambah);
        btnGroup.add(btnEdit);
        btnGroup.add(btnHapus);
        btnGroup.add(btnCsv);

        header.add(title, BorderLayout.NORTH);
        header.add(btnGroup, BorderLayout.SOUTH);

        String[] kolom = {"Tanggal", "Kategori", "Jenis", "Nominal", "Terkait Tugas", "Catatan"};
        modelTransaksi = new DefaultTableModel(kolom, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblTransaksi = new JTable(modelTransaksi);
        tblTransaksi.setRowHeight(25);
        tblTransaksi.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        panel.add(header, BorderLayout.NORTH);
        panel.add(new JScrollPane(tblTransaksi), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buatPanelTugas() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225)),
            new EmptyBorder(10, 10, 10, 10)
        ));

        JPanel header = new JPanel(new BorderLayout(0, 6));
        header.setOpaque(false);
        JLabel title = new JLabel("Daftar Tugas & Tenggat Kuliah");
        title.setFont(new Font("SansSerif", Font.BOLD, 14));

        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btnGroup.setOpaque(false);
        JButton btnTambah = new JButton("+ Tugas");
        JButton btnEdit = new JButton("Edit");
        JButton btnSelesai = new JButton("Toggle Selesai");
        JButton btnHapus = new JButton("Hapus");

        btnTambah.addActionListener(e -> bukaDialogTugas(null));
        btnEdit.addActionListener(e -> {
            int row = tblTugas.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Pilih baris tugas yang ingin diubah.");
                return;
            }
            bukaDialogTugas(cacheTugas.get(row));
        });
        btnSelesai.addActionListener(e -> toggleSelesaiTugas());
        btnHapus.addActionListener(e -> hapusTugasTerpilih());

        btnGroup.add(btnTambah);
        btnGroup.add(btnEdit);
        btnGroup.add(btnSelesai);
        btnGroup.add(btnHapus);

        header.add(title, BorderLayout.NORTH);
        header.add(btnGroup, BorderLayout.SOUTH);

        String[] kolom = {"Status", "Judul Tugas", "Mata Kuliah", "Tenggat", "Prioritas", "Biaya Tugas"};
        modelTugas = new DefaultTableModel(kolom, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblTugas = new JTable(modelTugas);
        tblTugas.setRowHeight(25);
        tblTugas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tblTugas.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected && row < cacheTugas.size()) {
                    Tugas t = cacheTugas.get(row);
                    if (tugasService.isTerlambat(t, LocalDateTime.now())) {
                        c.setBackground(new Color(254, 226, 226));
                        c.setForeground(new Color(153, 27, 27));
                    } else if (t.isSelesai()) {
                        c.setBackground(new Color(240, 253, 244));
                        c.setForeground(new Color(22, 101, 52));
                    } else {
                        c.setBackground(Color.WHITE);
                        c.setForeground(Color.BLACK);
                    }
                }
                return c;
            }
        });

        panel.add(header, BorderLayout.NORTH);
        panel.add(new JScrollPane(tblTugas), BorderLayout.CENTER);
        return panel;
    }

    private void muatSemuaData(boolean cekPengingat) {
        try {
            cacheTransaksi = keuanganService.getAllTransaksi();
            cacheTugas = tugasService.getAllTugas();

            modelTransaksi.setRowCount(0);
            for (Transaksi tr : cacheTransaksi) {
                modelTransaksi.addRow(new Object[]{
                    tr.getTanggal().toString(),
                    tr.getNamaKategori(),
                    tr.getJenisKategori(),
                    rupiahFormat.format(tr.getJumlah()),
                    tr.getJudulTugas() != null ? tr.getJudulTugas() : "-",
                    tr.getCatatan()
                });
            }

            modelTugas.setRowCount(0);
            for (Tugas t : cacheTugas) {
                String status = t.isSelesai() ? "Selesai" : (tugasService.isTerlambat(t, LocalDateTime.now()) ? "TERLAMBAT" : "Aktif");
                modelTugas.addRow(new Object[]{
                    status,
                    t.getJudul(),
                    t.getMataKuliah(),
                    t.getTenggat().format(dtfTenggat),
                    t.getPrioritas(),
                    rupiahFormat.format(t.getTotalBiaya())
                });
            }

            long pemasukan = keuanganService.hitungTotalPemasukan(cacheTransaksi);
            long pengeluaran = keuanganService.hitungTotalPengeluaran(cacheTransaksi);
            long saldo = keuanganService.hitungSaldo(cacheTransaksi);

            lblPemasukan.setText(rupiahFormat.format(pemasukan));
            lblPengeluaran.setText(rupiahFormat.format(pengeluaran));
            lblSaldo.setText(rupiahFormat.format(saldo));

            grafikPanel.setData(keuanganService.getPengeluaranPerKategori());

            if (cekPengingat) {
                List<Tugas> mendesak = tugasService.getTugasMendesak24Jam(cacheTugas, LocalDateTime.now());
                if (!mendesak.isEmpty()) {
                    StringBuilder sb = new StringBuilder("Perhatian! Tugas berikut memiliki tenggat dalam <= 24 jam:\n\n");
                    for (Tugas t : mendesak) {
                        sb.append("- ").append(t.getJudul())
                          .append(" (").append(t.getMataKuliah()).append(") -> ")
                          .append(t.getTenggat().format(dtfTenggat)).append("\n");
                    }
                    JOptionPane.showMessageDialog(this, sb.toString(), "Pengingat Tenggat Mendesak", JOptionPane.WARNING_MESSAGE);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void bukaDialogTransaksi(Transaksi existing) {
        try {
            List<Kategori> daftarKategori = keuanganService.getAllKategori();
            JComboBox<Kategori> cbKategori = new JComboBox<>(daftarKategori.toArray(new Kategori[0]));

            JComboBox<Object> cbTugas = new JComboBox<>();
            cbTugas.addItem("-- Tidak Terkait Tugas --");
            for (Tugas t : cacheTugas) {
                cbTugas.addItem(t);
            }

            JTextField txtTanggal = new JTextField(existing != null ? existing.getTanggal().toString() : LocalDate.now().toString());
            JTextField txtJumlah = new JTextField(existing != null ? String.valueOf(existing.getJumlah()) : "");
            JTextField txtCatatan = new JTextField(existing != null ? existing.getCatatan() : "");

            if (existing != null) {
                for (int i = 0; i < cbKategori.getItemCount(); i++) {
                    if (cbKategori.getItemAt(i).getId() == existing.getKategoriId()) {
                        cbKategori.setSelectedIndex(i);
                        break;
                    }
                }
                if (existing.getTugasId() != null) {
                    for (int i = 1; i < cbTugas.getItemCount(); i++) {
                        Tugas t = (Tugas) cbTugas.getItemAt(i);
                        if (t.getId() == existing.getTugasId()) {
                            cbTugas.setSelectedIndex(i);
                            break;
                        }
                    }
                }
            }

            JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
            form.add(new JLabel("Tanggal (YYYY-MM-DD):"));
            form.add(txtTanggal);
            form.add(new JLabel("Kategori:"));
            form.add(cbKategori);
            form.add(new JLabel("Nominal (Rp):"));
            form.add(txtJumlah);
            form.add(new JLabel("Terkait Tugas (Opsional):"));
            form.add(cbTugas);
            form.add(new JLabel("Catatan:"));
            form.add(txtCatatan);

            int res = JOptionPane.showConfirmDialog(this, form,
                existing == null ? "Tambah Transaksi Baru" : "Edit Transaksi",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (res == JOptionPane.OK_OPTION) {
                LocalDate tanggal = LocalDate.parse(txtTanggal.getText().trim());
                long jumlah = Long.parseLong(txtJumlah.getText().trim().replace(".", "").replace(",", ""));
                Kategori kat = (Kategori) cbKategori.getSelectedItem();
                Object dipilihTugas = cbTugas.getSelectedItem();
                Integer tugasId = (dipilihTugas instanceof Tugas) ? ((Tugas) dipilihTugas).getId() : null;

                if (existing == null) {
                    keuanganService.tambahTransaksi(tanggal, jumlah, kat.getId(), tugasId, txtCatatan.getText());
                } else {
                    keuanganService.editTransaksi(existing.getId(), tanggal, jumlah, kat.getId(), tugasId, txtCatatan.getText());
                }
                muatSemuaData(false);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Nominal harus berupa angka bulat yang valid.", "Input Tidak Valid", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Kesalahan input: " + ex.getMessage(), "Validasi", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void hapusTransaksiTerpilih() {
        int row = tblTransaksi.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Pilih baris transaksi yang ingin dihapus.");
            return;
        }
        int konfirmasi = JOptionPane.showConfirmDialog(this, "Hapus transaksi terpilih?", "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
        if (konfirmasi == JOptionPane.YES_OPTION) {
            try {
                keuanganService.hapusTransaksi(cacheTransaksi.get(row).getId());
                muatSemuaData(false);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Gagal menghapus: " + ex.getMessage());
            }
        }
    }

    private void bukaDialogTugas(Tugas existing) {
        try {
            JTextField txtJudul = new JTextField(existing != null ? existing.getJudul() : "");
            JTextField txtMatkul = new JTextField(existing != null ? existing.getMataKuliah() : "");
            JTextField txtTenggat = new JTextField(existing != null ? existing.getTenggat().format(dtfTenggat) : LocalDateTime.now().plusDays(1).format(dtfTenggat));
            JComboBox<String> cbPrioritas = new JComboBox<>(new String[]{"TINGGI", "SEDANG", "RENDAH"});
            if (existing != null) cbPrioritas.setSelectedItem(existing.getPrioritas());

            JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
            form.add(new JLabel("Judul Tugas:"));
            form.add(txtJudul);
            form.add(new JLabel("Mata Kuliah:"));
            form.add(txtMatkul);
            form.add(new JLabel("Tenggat (YYYY-MM-DD HH:mm):"));
            form.add(txtTenggat);
            form.add(new JLabel("Prioritas:"));
            form.add(cbPrioritas);

            int res = JOptionPane.showConfirmDialog(this, form,
                existing == null ? "Tambah Tugas Baru" : "Edit Tugas",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (res == JOptionPane.OK_OPTION) {
                LocalDateTime tenggat = LocalDateTime.parse(txtTenggat.getText().trim(), dtfTenggat);
                String prioritas = (String) cbPrioritas.getSelectedItem();
                if (existing == null) {
                    tugasService.tambahTugas(txtJudul.getText(), txtMatkul.getText(), tenggat, prioritas);
                } else {
                    tugasService.editTugas(existing.getId(), txtJudul.getText(), txtMatkul.getText(), tenggat, prioritas, existing.isSelesai());
                }
                muatSemuaData(false);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Format input tidak valid (pastikan tenggat YYYY-MM-DD HH:mm): " + ex.getMessage(), "Validasi", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void toggleSelesaiTugas() {
        int row = tblTugas.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Pilih tugas yang ingin diubah statusnya.");
            return;
        }
        try {
            Tugas t = cacheTugas.get(row);
            tugasService.ubahStatusSelesai(t.getId(), !t.isSelesai());
            muatSemuaData(false);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal mengubah status: " + ex.getMessage());
        }
    }

    private void hapusTugasTerpilih() {
        int row = tblTugas.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Pilih tugas yang ingin dihapus.");
            return;
        }
        int konfirmasi = JOptionPane.showConfirmDialog(this, "Hapus tugas terpilih? Transaksi terkait tidak akan ikut terhapus.", "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
        if (konfirmasi == JOptionPane.YES_OPTION) {
            try {
                tugasService.hapusTugas(cacheTugas.get(row).getId());
                muatSemuaData(false);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Gagal menghapus: " + ex.getMessage());
            }
        }
    }

    private void eksporTransaksiCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Simpan Laporan Keuangan (CSV)");
        chooser.setSelectedFile(new File("laporan_dompet_deadline.csv"));

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
                pw.println("ID,Tanggal,Kategori,Jenis,Nominal,Terkait_Tugas,Catatan");
                for (Transaksi tr : cacheTransaksi) {
                    String tugas = tr.getJudulTugas() != null ? tr.getJudulTugas().replace(",", " ") : "";
                    String catatan = tr.getCatatan() != null ? tr.getCatatan().replace(",", " ") : "";
                    pw.printf("%d,%s,%s,%s,%d,%s,%s%n",
                        tr.getId(),
                        tr.getTanggal(),
                        tr.getNamaKategori().replace(",", " "),
                        tr.getJenisKategori(),
                        tr.getJumlah(),
                        tugas,
                        catatan
                    );
                }
                JOptionPane.showMessageDialog(this, "Berhasil diekspor ke:\n" + file.getAbsolutePath());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Gagal mengekspor CSV: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}