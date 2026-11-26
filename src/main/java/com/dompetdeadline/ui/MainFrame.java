package com.dompetdeadline.ui;

import com.dompetdeadline.model.Kategori;
import com.dompetdeadline.model.Transaksi;
import com.dompetdeadline.model.Tugas;
import com.dompetdeadline.service.KeuanganService;
import com.dompetdeadline.service.TugasService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class MainFrame extends JFrame {
    // Palet Warna Soft Pink & Rose Berry
    private static final Color BG_BLUSH = new Color(253, 242, 248);       // #FDF2F8
    private static final Color PINK_CARD_BORDER = new Color(249, 168, 212); // #F9A8D4
    private static final Color PINK_PRIMARY = new Color(219, 39, 119);     // #DB2777
    private static final Color PINK_DEEP = new Color(157, 23, 77);         // #9D174D
    private static final Color PINK_SOFT_BTN = new Color(252, 231, 243);   // #FCE7F3
    private static final Color PINK_ZEBRA = new Color(255, 247, 251);      // #FFF7FB
    private static final Color TEXT_DARK = new Color(80, 7, 36);           // #500724

    private final KeuanganService keuanganService = new KeuanganService();
    private final TugasService tugasService = new TugasService();
    private final NumberFormat rupiahFormat = NumberFormat.getCurrencyInstance(Locale.of("id", "ID"));
    private final DateTimeFormatter dtfTenggat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private JLabel lblSaldo;
    private JLabel lblArusKas;
    private JLabel lblBiayaTugasAktif;
    private JTextArea txtTenggatTerdekat;
    private JComboBox<String> cbBulanGrafik;
    private GrafikPengeluaranPanel grafikPanel;

    private JTable tblTransaksi;
    private DefaultTableModel modelTransaksi;
    private JTextField txtCariTransaksi;
    private List<Transaksi> cacheTransaksi = new ArrayList<>();
    private List<Transaksi> viewTransaksi = new ArrayList<>();

    private JTable tblTugas;
    private DefaultTableModel modelTugas;
    private JTextField txtCariTugas;
    private List<Tugas> cacheTugas = new ArrayList<>();
    private List<Tugas> viewTugas = new ArrayList<>();

    private final Set<Integer> sudahDinotifikasi = new HashSet<>();
    private javax.swing.Timer timerPengingat;
    private TrayIcon trayIcon;

    public MainFrame() {
        setTitle("Dompet Deadline - Manajemen Keuangan & Tugas Mahasiswa");
        setSize(1260, 760);
        setMinimumSize(new Dimension(1040, 650));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        rupiahFormat.setMaximumFractionDigits(0);
        initSystemTray();
        initComponents();
        muatSemuaData();
        mulaiTimerPengingat();
    }

    private void initSystemTray() {
        if (!SystemTray.isSupported()) return;
        try {
            SystemTray tray = SystemTray.getSystemTray();
            BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();
            g2.setColor(PINK_PRIMARY);
            g2.fillOval(1, 1, 14, 14);
            g2.setColor(Color.WHITE);
            g2.drawString("D", 4, 13);
            g2.dispose();

            trayIcon = new TrayIcon(img, "Dompet Deadline");
            trayIcon.setImageAutoSize(true);
            tray.add(trayIcon);
        } catch (Exception ignored) {
            trayIcon = null;
        }
    }

    private void mulaiTimerPengingat() {
        cekPengingatTenggat();
        timerPengingat = new javax.swing.Timer(60_000, e -> cekPengingatTenggat());
        timerPengingat.start();
    }

    private void cekPengingatTenggat() {
        List<Tugas> mendesak = tugasService.getTugasMendesak24Jam(cacheTugas, LocalDateTime.now());
        List<Tugas> baruMendesak = new ArrayList<>();
        for (Tugas t : mendesak) {
            if (!sudahDinotifikasi.contains(t.getId())) {
                sudahDinotifikasi.add(t.getId());
                baruMendesak.add(t);
            }
        }
        if (baruMendesak.isEmpty()) return;

        StringBuilder sb = new StringBuilder("Tenggat tugas dalam <= 24 jam:\n");
        for (Tugas t : baruMendesak) {
            sb.append("- ").append(t.getJudul())
              .append(" (").append(t.getMataKuliah()).append(") : ")
              .append(t.getTenggat().format(dtfTenggat)).append("\n");
        }

        if (trayIcon != null) {
            trayIcon.displayMessage("Pengingat Dompet Deadline", sb.toString(), TrayIcon.MessageType.WARNING);
        } else {
            JOptionPane.showMessageDialog(this, sb.toString(), "Pengingat Tenggat Mendesak", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(12, 14, 14, 14));
        root.setBackground(BG_BLUSH);

        // === HEADER BANNER PINK ===
        JPanel banner = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, PINK_DEEP, getWidth(), 0, PINK_PRIMARY);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
            }
        };
        banner.setOpaque(false);
        banner.setBorder(new EmptyBorder(10, 16, 10, 16));

        JLabel lblAppTitle = new JLabel("Dompet Deadline");
        lblAppTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblAppTitle.setForeground(Color.WHITE);

        JLabel lblSubtitle = new JLabel("Pengelola Keuangan Harian & Tenggat Tugas Kuliah");
        lblSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblSubtitle.setForeground(new Color(252, 231, 243));

        banner.add(lblAppTitle, BorderLayout.WEST);
        banner.add(lblSubtitle, BorderLayout.EAST);

        // === PANEL ATAS: Dashboard Ringkasan + 3 Tenggat Terdekat + Grafik ===
        JPanel topPanel = new JPanel(new BorderLayout(12, 12));
        topPanel.setOpaque(false);

        JPanel cardsPanel = new JPanel(new GridLayout(3, 1, 8, 8));
        cardsPanel.setOpaque(false);
        cardsPanel.setPreferredSize(new Dimension(305, 210));

        lblSaldo = new JLabel("Rp 0");
        lblArusKas = new JLabel("+Rp 0 / -Rp 0");
        lblBiayaTugasAktif = new JLabel("Rp 0");

        cardsPanel.add(buatKartuRingkasan("Sisa Uang (Saldo Bersih)", lblSaldo, PINK_PRIMARY));
        cardsPanel.add(buatKartuRingkasan("Pemasukan / Pengeluaran", lblArusKas, new Color(190, 24, 93)));
        cardsPanel.add(buatKartuRingkasan("Total Biaya Tugas Aktif", lblBiayaTugasAktif, new Color(225, 29, 72)));

        // Kartu 3 Tenggat Terdekat
        JPanel cardTenggat = new JPanel(new BorderLayout(4, 6));
        cardTenggat.setBackground(Color.WHITE);
        cardTenggat.setPreferredSize(new Dimension(305, 210));
        cardTenggat.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(PINK_CARD_BORDER, 1, true),
            new EmptyBorder(10, 12, 10, 12)
        ));
        JLabel lblJudulTenggat = new JLabel("3 Tenggat Terdekat (Belum Selesai)");
        lblJudulTenggat.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblJudulTenggat.setForeground(PINK_DEEP);

        txtTenggatTerdekat = new JTextArea("Tidak ada tugas aktif.");
        txtTenggatTerdekat.setEditable(false);
        txtTenggatTerdekat.setLineWrap(true);
        txtTenggatTerdekat.setWrapStyleWord(true);
        txtTenggatTerdekat.setFont(new Font("SansSerif", Font.PLAIN, 12));
        txtTenggatTerdekat.setBackground(Color.WHITE);
        txtTenggatTerdekat.setForeground(TEXT_DARK);

        cardTenggat.add(lblJudulTenggat, BorderLayout.NORTH);
        cardTenggat.add(txtTenggatTerdekat, BorderLayout.CENTER);

        JPanel leftDash = new JPanel(new BorderLayout(10, 10));
        leftDash.setOpaque(false);
        leftDash.add(cardsPanel, BorderLayout.WEST);
        leftDash.add(cardTenggat, BorderLayout.CENTER);

        // Panel Grafik dengan Pemilih Bulan
        JPanel chartContainer = new JPanel(new BorderLayout(4, 4));
        chartContainer.setBackground(Color.WHITE);
        chartContainer.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(PINK_CARD_BORDER, 1, true),
            new EmptyBorder(8, 12, 8, 12)
        ));

        JPanel chartHeader = new JPanel(new BorderLayout());
        chartHeader.setOpaque(false);
        JLabel lblChartTitle = new JLabel("Grafik Pengeluaran per Kategori");
        lblChartTitle.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblChartTitle.setForeground(PINK_DEEP);

        JPanel bulanPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        bulanPanel.setOpaque(false);
        JLabel lblBulan = new JLabel("Bulan:");
        lblBulan.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblBulan.setForeground(TEXT_DARK);

        cbBulanGrafik = new JComboBox<>();
        cbBulanGrafik.addItem("SEMUA");
        cbBulanGrafik.addItem(YearMonth.now().toString());
        cbBulanGrafik.addItem(YearMonth.now().minusMonths(1).toString());
        cbBulanGrafik.addActionListener(e -> refreshGrafik());

        bulanPanel.add(lblBulan);
        bulanPanel.add(cbBulanGrafik);
        chartHeader.add(lblChartTitle, BorderLayout.WEST);
        chartHeader.add(bulanPanel, BorderLayout.EAST);

        grafikPanel = new GrafikPengeluaranPanel();
        chartContainer.add(chartHeader, BorderLayout.NORTH);
        chartContainer.add(grafikPanel, BorderLayout.CENTER);

        topPanel.add(leftDash, BorderLayout.WEST);
        topPanel.add(chartContainer, BorderLayout.CENTER);

        JPanel northContainer = new JPanel(new BorderLayout(10, 10));
        northContainer.setOpaque(false);
        northContainer.add(banner, BorderLayout.NORTH);
        northContainer.add(topPanel, BorderLayout.CENTER);

        // === PANEL TENGAH: Split Pane Transaksi (Kiri) & Tugas (Kanan) ===
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buatPanelTransaksi(), buatPanelTugas());
        splitPane.setResizeWeight(0.50);
        splitPane.setDividerSize(8);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);

        root.add(northContainer, BorderLayout.NORTH);
        root.add(splitPane, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel buatKartuRingkasan(String judul, JLabel labelNilai, Color aksen) {
        JPanel card = new JPanel(new BorderLayout(4, 2));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createCompoundBorder(
                new LineBorder(PINK_CARD_BORDER, 1, true),
                BorderFactory.createMatteBorder(0, 6, 0, 0, aksen)
            ),
            new EmptyBorder(8, 12, 8, 12)
        ));

        JLabel lblJudul = new JLabel(judul);
        lblJudul.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblJudul.setForeground(new Color(157, 23, 77));

        labelNilai.setFont(new Font("SansSerif", Font.BOLD, 15));
        labelNilai.setForeground(TEXT_DARK);

        card.add(lblJudul, BorderLayout.NORTH);
        card.add(labelNilai, BorderLayout.CENTER);
        return card;
    }

    private JButton buatTombolPink(String teks, boolean utama) {
        JButton btn = new JButton(teks) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (utama) {
                    g2.setColor(getModel().isPressed() ? PINK_DEEP : PINK_PRIMARY);
                } else {
                    g2.setColor(getModel().isPressed() ? PINK_CARD_BORDER : PINK_SOFT_BTN);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        if (utama) {
            btn.setForeground(Color.WHITE);
            btn.setBorder(new EmptyBorder(6, 12, 6, 12));
        } else {
            btn.setForeground(PINK_DEEP);
            btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(PINK_CARD_BORDER, 1, true),
                new EmptyBorder(5, 10, 5, 10)
            ));
        }
        return btn;
    }

    private void percantikTabel(JTable table) {
        table.setRowHeight(27);
        table.setShowGrid(true);
        table.setGridColor(new Color(252, 231, 243));
        table.setSelectionBackground(new Color(244, 114, 182));
        table.setSelectionForeground(Color.WHITE);
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JTableHeader header = table.getTableHeader();
        header.setBackground(PINK_SOFT_BTN);
        header.setForeground(PINK_DEEP);
        header.setFont(new Font("SansSerif", Font.BOLD, 12));
    }

    private JPanel buatPanelTransaksi() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(PINK_CARD_BORDER, 1, true),
            new EmptyBorder(10, 12, 10, 12)
        ));

        JPanel header = new JPanel(new BorderLayout(0, 8));
        header.setOpaque(false);
        JLabel title = new JLabel("Catatan Keuangan (Klik Header untuk Urutkan)");
        title.setFont(new Font("SansSerif", Font.BOLD, 14));
        title.setForeground(PINK_DEEP);

        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        btnGroup.setOpaque(false);
        JButton btnTambah = buatTombolPink("+ Transaksi", true);
        JButton btnKategori = buatTombolPink("+ Kategori", false);
        JButton btnEdit = buatTombolPink("Ubah", false);
        JButton btnHapus = buatTombolPink("Hapus", false);
        JButton btnCsv = buatTombolPink("Ekspor CSV", false);

        txtCariTransaksi = new JTextField(10);
        txtCariTransaksi.setToolTipText("Cari kategori, catatan, atau nama tugas...");
        txtCariTransaksi.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { renderTabelTransaksi(); }
            @Override public void removeUpdate(DocumentEvent e) { renderTabelTransaksi(); }
            @Override public void changedUpdate(DocumentEvent e) { renderTabelTransaksi(); }
        });

        btnTambah.addActionListener(e -> bukaDialogTransaksi(null));
        btnKategori.addActionListener(e -> bukaDialogKategori());
        btnEdit.addActionListener(e -> {
            int viewRow = tblTransaksi.getSelectedRow();
            if (viewRow < 0) {
                JOptionPane.showMessageDialog(this, "Pilih baris transaksi yang ingin diubah.");
                return;
            }
            int modelRow = tblTransaksi.convertRowIndexToModel(viewRow);
            bukaDialogTransaksi(viewTransaksi.get(modelRow));
        });
        btnHapus.addActionListener(e -> hapusTransaksiTerpilih());
        btnCsv.addActionListener(e -> eksporTransaksiCsv());

        JLabel lblCari = new JLabel("Cari:");
        lblCari.setForeground(PINK_DEEP);

        btnGroup.add(btnTambah);
        btnGroup.add(btnKategori);
        btnGroup.add(btnEdit);
        btnGroup.add(btnHapus);
        btnGroup.add(btnCsv);
        btnGroup.add(lblCari);
        btnGroup.add(txtCariTransaksi);

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
        percantikTabel(tblTransaksi);
        tblTransaksi.setAutoCreateRowSorter(true);
        tblTransaksi.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tblTransaksi.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : PINK_ZEBRA);
                    c.setForeground(TEXT_DARK);
                }
                return c;
            }
        });

        panel.add(header, BorderLayout.NORTH);
        panel.add(new JScrollPane(tblTransaksi), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buatPanelTugas() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(PINK_CARD_BORDER, 1, true),
            new EmptyBorder(10, 12, 10, 12)
        ));

        JPanel header = new JPanel(new BorderLayout(0, 8));
        header.setOpaque(false);
        JLabel title = new JLabel("Daftar Tugas & Tenggat Kuliah");
        title.setFont(new Font("SansSerif", Font.BOLD, 14));
        title.setForeground(PINK_DEEP);

        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        btnGroup.setOpaque(false);
        JButton btnTambah = buatTombolPink("+ Tugas", true);
        JButton btnEdit = buatTombolPink("Ubah", false);
        JButton btnSelesai = buatTombolPink("Tandai Selesai", false);
        JButton btnHapus = buatTombolPink("Hapus", false);

        txtCariTugas = new JTextField(11);
        txtCariTugas.setToolTipText("Cari judul tugas, matkul, atau prioritas...");
        txtCariTugas.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { renderTabelTugas(); }
            @Override public void removeUpdate(DocumentEvent e) { renderTabelTugas(); }
            @Override public void changedUpdate(DocumentEvent e) { renderTabelTugas(); }
        });

        btnTambah.addActionListener(e -> bukaDialogTugas(null));
        btnEdit.addActionListener(e -> {
            int viewRow = tblTugas.getSelectedRow();
            if (viewRow < 0) {
                JOptionPane.showMessageDialog(this, "Pilih baris tugas yang ingin diubah.");
                return;
            }
            int modelRow = tblTugas.convertRowIndexToModel(viewRow);
            bukaDialogTugas(viewTugas.get(modelRow));
        });
        btnSelesai.addActionListener(e -> toggleSelesaiTugas());
        btnHapus.addActionListener(e -> hapusTugasTerpilih());

        JLabel lblCari = new JLabel("Cari:");
        lblCari.setForeground(PINK_DEEP);

        btnGroup.add(btnTambah);
        btnGroup.add(btnEdit);
        btnGroup.add(btnSelesai);
        btnGroup.add(btnHapus);
        btnGroup.add(lblCari);
        btnGroup.add(txtCariTugas);

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
        percantikTabel(tblTugas);
        tblTugas.setAutoCreateRowSorter(true);
        tblTugas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tblTugas.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                int modelRow = table.convertRowIndexToModel(row);
                if (!isSelected && modelRow < viewTugas.size()) {
                    Tugas t = viewTugas.get(modelRow);
                    if (tugasService.isTerlambat(t, LocalDateTime.now())) {
                        c.setBackground(new Color(255, 228, 230));
                        c.setForeground(new Color(159, 18, 57));
                    } else if (t.isSelesai()) {
                        c.setBackground(new Color(240, 253, 244));
                        c.setForeground(new Color(22, 101, 52));
                    } else {
                        c.setBackground(row % 2 == 0 ? Color.WHITE : PINK_ZEBRA);
                        c.setForeground(TEXT_DARK);
                    }
                }
                return c;
            }
        });

        panel.add(header, BorderLayout.NORTH);
        panel.add(new JScrollPane(tblTugas), BorderLayout.CENTER);
        return panel;
    }

    private void renderTabelTransaksi() {
        String keyword = txtCariTransaksi != null ? txtCariTransaksi.getText().trim().toLowerCase() : "";
        viewTransaksi = new ArrayList<>();
        modelTransaksi.setRowCount(0);

        for (Transaksi tr : cacheTransaksi) {
            String tugas = tr.getJudulTugas() != null ? tr.getJudulTugas() : "-";
            String catatan = tr.getCatatan() != null ? tr.getCatatan() : "";
            boolean cocok = keyword.isEmpty()
                || tr.getNamaKategori().toLowerCase().contains(keyword)
                || tr.getJenisKategori().toLowerCase().contains(keyword)
                || tugas.toLowerCase().contains(keyword)
                || catatan.toLowerCase().contains(keyword);

            if (cocok) {
                viewTransaksi.add(tr);
                modelTransaksi.addRow(new Object[]{
                    tr.getTanggal().toString(),
                    tr.getNamaKategori(),
                    tr.getJenisKategori(),
                    rupiahFormat.format(tr.getJumlah()),
                    tugas,
                    catatan
                });
            }
        }
    }

    private void renderTabelTugas() {
        String keyword = txtCariTugas != null ? txtCariTugas.getText().trim().toLowerCase() : "";
        viewTugas = new ArrayList<>();
        modelTugas.setRowCount(0);

        for (Tugas t : cacheTugas) {
            String status = t.isSelesai() ? "Selesai" : (tugasService.isTerlambat(t, LocalDateTime.now()) ? "TERLAMBAT" : "Aktif");
            boolean cocok = keyword.isEmpty()
                || t.getJudul().toLowerCase().contains(keyword)
                || t.getMataKuliah().toLowerCase().contains(keyword)
                || t.getPrioritas().toLowerCase().contains(keyword)
                || status.toLowerCase().contains(keyword);

            if (cocok) {
                viewTugas.add(t);
                modelTugas.addRow(new Object[]{
                    status,
                    t.getJudul(),
                    t.getMataKuliah(),
                    t.getTenggat().format(dtfTenggat),
                    t.getPrioritas(),
                    rupiahFormat.format(t.getTotalBiaya())
                });
            }
        }
    }

    private void refreshGrafik() {
        try {
            String bulan = cbBulanGrafik != null ? (String) cbBulanGrafik.getSelectedItem() : "SEMUA";
            grafikPanel.setData(keuanganService.getPengeluaranPerKategori(bulan));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal memuat grafik: " + ex.getMessage());
        }
    }

    private void muatSemuaData() {
        try {
            cacheTransaksi = keuanganService.getAllTransaksi();
            cacheTugas = tugasService.getAllTugas();

            renderTabelTransaksi();
            renderTabelTugas();

            long pemasukan = keuanganService.hitungTotalPemasukan(cacheTransaksi);
            long pengeluaran = keuanganService.hitungTotalPengeluaran(cacheTransaksi);
            long saldo = keuanganService.hitungSaldo(cacheTransaksi);

            lblSaldo.setText(rupiahFormat.format(saldo));
            lblArusKas.setText("+" + rupiahFormat.format(pemasukan) + " / -" + rupiahFormat.format(pengeluaran));

            long totalBiayaAktif = 0;
            List<Tugas> aktif = new ArrayList<>();
            for (Tugas t : cacheTugas) {
                if (!t.isSelesai()) {
                    totalBiayaAktif += t.getTotalBiaya();
                    aktif.add(t);
                }
            }
            lblBiayaTugasAktif.setText(rupiahFormat.format(totalBiayaAktif));

            aktif.sort(Comparator.comparing(Tugas::getTenggat));
            if (aktif.isEmpty()) {
                txtTenggatTerdekat.setText("Semua tugas sudah selesai!");
            } else {
                StringBuilder sb = new StringBuilder();
                int batas = Math.min(3, aktif.size());
                for (int i = 0; i < batas; i++) {
                    Tugas t = aktif.get(i);
                    sb.append(i + 1).append(". ").append(t.getJudul())
                      .append("\n   ").append(t.getMataKuliah())
                      .append(" | ").append(t.getTenggat().format(dtfTenggat)).append("\n");
                }
                txtTenggatTerdekat.setText(sb.toString().trim());
            }

            refreshGrafik();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void bukaDialogKategori() {
        JDialog dialog = new JDialog(this, "Tambah Kategori Baru", true);
        dialog.setSize(360, 180);
        dialog.setLocationRelativeTo(this);

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(new EmptyBorder(14, 16, 14, 16));
        content.setBackground(BG_BLUSH);

        JPanel form = new JPanel(new GridLayout(2, 2, 8, 8));
        form.setOpaque(false);
        JTextField txtNama = new JTextField();
        JComboBox<String> cbJenis = new JComboBox<>(new String[]{"PENGELUARAN", "PEMASUKAN"});

        form.add(new JLabel("Nama Kategori:"));
        form.add(txtNama);
        form.add(new JLabel("Jenis Kategori:"));
        form.add(cbJenis);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton btnBatal = buatTombolPink("Batal", false);
        JButton btnSimpan = buatTombolPink("Simpan", true);

        btnBatal.addActionListener(e -> dialog.dispose());
        btnSimpan.addActionListener(e -> {
            try {
                keuanganService.tambahKategori(txtNama.getText(), (String) cbJenis.getSelectedItem());
                JOptionPane.showMessageDialog(dialog, "Kategori berhasil disimpan!");
                dialog.dispose();
                muatSemuaData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Validasi Input", JOptionPane.WARNING_MESSAGE);
            }
        });

        actions.add(btnBatal);
        actions.add(btnSimpan);

        content.add(form, BorderLayout.CENTER);
        content.add(actions, BorderLayout.SOUTH);
        dialog.setContentPane(content);
        dialog.getRootPane().setDefaultButton(btnSimpan);
        dialog.setVisible(true);
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

            JDialog dialog = new JDialog(this, existing == null ? "Tambah Transaksi Baru" : "Ubah Transaksi", true);
            dialog.setSize(420, 270);
            dialog.setLocationRelativeTo(this);

            JPanel content = new JPanel(new BorderLayout(10, 12));
            content.setBorder(new EmptyBorder(14, 16, 14, 16));
            content.setBackground(BG_BLUSH);

            JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));
            form.setOpaque(false);
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

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            actions.setOpaque(false);
            JButton btnBatal = buatTombolPink("Batal", false);
            JButton btnSimpan = buatTombolPink("Simpan", true);

            btnBatal.addActionListener(e -> dialog.dispose());
            btnSimpan.addActionListener(e -> {
                try {
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
                    JOptionPane.showMessageDialog(dialog, "Transaksi berhasil disimpan!");
                    dialog.dispose();
                    muatSemuaData();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "Nominal harus berupa angka bulat tanpa huruf (contoh: 25000).", "Input Tidak Valid", JOptionPane.WARNING_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Kesalahan input (pastikan format tanggal YYYY-MM-DD): " + ex.getMessage(), "Validasi", JOptionPane.WARNING_MESSAGE);
                }
            });

            actions.add(btnBatal);
            actions.add(btnSimpan);

            content.add(form, BorderLayout.CENTER);
            content.add(actions, BorderLayout.SOUTH);
            dialog.setContentPane(content);
            dialog.getRootPane().setDefaultButton(btnSimpan);
            dialog.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal membuka form transaksi: " + ex.getMessage());
        }
    }

    private void hapusTransaksiTerpilih() {
        int viewRow = tblTransaksi.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Pilih baris transaksi yang ingin dihapus.");
            return;
        }
        int konfirmasi = JOptionPane.showConfirmDialog(this, "Yakin ingin menghapus transaksi terpilih?", "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
        if (konfirmasi == JOptionPane.YES_OPTION) {
            try {
                int modelRow = tblTransaksi.convertRowIndexToModel(viewRow);
                keuanganService.hapusTransaksi(viewTransaksi.get(modelRow).getId());
                muatSemuaData();
                JOptionPane.showMessageDialog(this, "Transaksi berhasil dihapus.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Gagal menghapus: " + ex.getMessage());
            }
        }
    }

    private void bukaDialogTugas(Tugas existing) {
        JTextField txtJudul = new JTextField(existing != null ? existing.getJudul() : "");
        JTextField txtMatkul = new JTextField(existing != null ? existing.getMataKuliah() : "");
        JTextField txtTenggat = new JTextField(existing != null ? existing.getTenggat().format(dtfTenggat) : LocalDateTime.now().plusDays(1).format(dtfTenggat));
        JComboBox<String> cbPrioritas = new JComboBox<>(new String[]{"TINGGI", "SEDANG", "RENDAH"});
        if (existing != null) cbPrioritas.setSelectedItem(existing.getPrioritas());

        JDialog dialog = new JDialog(this, existing == null ? "Tambah Tugas Baru" : "Ubah Tugas", true);
        dialog.setSize(420, 240);
        dialog.setLocationRelativeTo(this);

        JPanel content = new JPanel(new BorderLayout(10, 12));
        content.setBorder(new EmptyBorder(14, 16, 14, 16));
        content.setBackground(BG_BLUSH);

        JPanel form = new JPanel(new GridLayout(4, 2, 8, 8));
        form.setOpaque(false);
        form.add(new JLabel("Judul Tugas:"));
        form.add(txtJudul);
        form.add(new JLabel("Mata Kuliah:"));
        form.add(txtMatkul);
        form.add(new JLabel("Tenggat (YYYY-MM-DD HH:mm):"));
        form.add(txtTenggat);
        form.add(new JLabel("Prioritas:"));
        form.add(cbPrioritas);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton btnBatal = buatTombolPink("Batal", false);
        JButton btnSimpan = buatTombolPink("Simpan", true);

        btnBatal.addActionListener(e -> dialog.dispose());
        btnSimpan.addActionListener(e -> {
            try {
                LocalDateTime tenggat = LocalDateTime.parse(txtTenggat.getText().trim(), dtfTenggat);
                String prioritas = (String) cbPrioritas.getSelectedItem();
                if (existing == null) {
                    tugasService.tambahTugas(txtJudul.getText(), txtMatkul.getText(), tenggat, prioritas);
                } else {
                    tugasService.editTugas(existing.getId(), txtJudul.getText(), txtMatkul.getText(), tenggat, prioritas, existing.isSelesai());
                }
                JOptionPane.showMessageDialog(dialog, "Data tugas berhasil disimpan!");
                dialog.dispose();
                muatSemuaData();
                cekPengingatTenggat();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Pastikan judul terisi dan tenggat berformat YYYY-MM-DD HH:mm (contoh: 2026-12-01 23:59).", "Validasi Input", JOptionPane.WARNING_MESSAGE);
            }
        });

        actions.add(btnBatal);
        actions.add(btnSimpan);

        content.add(form, BorderLayout.CENTER);
        content.add(actions, BorderLayout.SOUTH);
        dialog.setContentPane(content);
        dialog.getRootPane().setDefaultButton(btnSimpan);
        dialog.setVisible(true);
    }

    private void toggleSelesaiTugas() {
        int viewRow = tblTugas.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Pilih tugas yang ingin diubah statusnya.");
            return;
        }
        try {
            int modelRow = tblTugas.convertRowIndexToModel(viewRow);
            Tugas t = viewTugas.get(modelRow);
            tugasService.ubahStatusSelesai(t.getId(), !t.isSelesai());
            muatSemuaData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal mengubah status: " + ex.getMessage());
        }
    }

    private void hapusTugasTerpilih() {
        int viewRow = tblTugas.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Pilih tugas yang ingin dihapus.");
            return;
        }
        int konfirmasi = JOptionPane.showConfirmDialog(this, "Yakin ingin menghapus tugas terpilih? Transaksi terkait tidak akan ikut terhapus.", "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
        if (konfirmasi == JOptionPane.YES_OPTION) {
            try {
                int modelRow = tblTugas.convertRowIndexToModel(viewRow);
                tugasService.hapusTugas(viewTugas.get(modelRow).getId());
                muatSemuaData();
                JOptionPane.showMessageDialog(this, "Tugas berhasil dihapus.");
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
                JOptionPane.showMessageDialog(this, "Laporan CSV berhasil disimpan ke:\n" + file.getAbsolutePath());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Gagal mengekspor CSV: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}