package com.dompetdeadline.ui;

import javax.swing.JPanel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class GrafikPengeluaranPanel extends JPanel {
    private Map<String, Long> dataPengeluaran = new LinkedHashMap<>();
    private final Color[] paletPink = {
        new Color(219, 39, 119),  // Rose 600
        new Color(244, 63, 94),   // Pink-Red 500
        new Color(236, 72, 153),  // Pink 500
        new Color(190, 24, 93),   // Deep Berry 700
        new Color(244, 114, 182), // Soft Pink 400
        new Color(162, 28, 175)   // Fuchsia-Plum
    };

    public GrafikPengeluaranPanel() {
        setPreferredSize(new Dimension(460, 210));
        setBackground(Color.WHITE);
    }

    public void setData(Map<String, Long> data) {
        this.dataPengeluaran = data != null ? data : new LinkedHashMap<>();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        if (dataPengeluaran.isEmpty()) {
            g2.setFont(new Font("SansSerif", Font.ITALIC, 12));
            g2.setColor(new Color(157, 23, 77));
            g2.drawString("Belum ada kategori pengeluaran.", 20, height / 2);
            return;
        }

        long maxVal = 0;
        for (long val : dataPengeluaran.values()) {
            if (val > maxVal) maxVal = val;
        }
        if (maxVal == 0) maxVal = 1;

        int padLeft = 64;
        int padRight = 20;
        int padTop = 26;
        int padBottom = 38;
        int chartWidth = width - padLeft - padRight;
        int chartHeight = height - padTop - padBottom;

        NumberFormat nf = NumberFormat.getInstance(Locale.of("id", "ID"));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        FontMetrics fm = g2.getFontMetrics();

        // Garis kisi horizontal & label sumbu Y (0, 50%, 100%)
        for (int step = 0; step <= 2; step++) {
            int yGrid = padTop + chartHeight - (step * chartHeight / 2);
            long nilaiGrid = (maxVal * step) / 2;

            g2.setColor(step == 0 ? new Color(244, 114, 182) : new Color(252, 231, 243));
            g2.drawLine(padLeft, yGrid, width - padRight, yGrid);

            String labelY = "Rp" + nf.format(nilaiGrid);
            int labelYWidth = fm.stringWidth(labelY);
            g2.setColor(new Color(131, 24, 67));
            g2.drawString(labelY, padLeft - labelYWidth - 8, yGrid + 4);
        }

        // Sumbu vertikal Y
        g2.setColor(new Color(244, 114, 182));
        g2.drawLine(padLeft, padTop, padLeft, padTop + chartHeight);

        int count = dataPengeluaran.size();
        int slotWidth = chartWidth / Math.max(count, 1);
        int barWidth = Math.min(44, Math.max(18, slotWidth - 18));

        int idx = 0;
        for (Map.Entry<String, Long> entry : dataPengeluaran.entrySet()) {
            String kategori = entry.getKey();
            long nilai = entry.getValue();

            int barHeight = nilai == 0 ? 3 : Math.max(6, (int) ((double) nilai / maxVal * (chartHeight - 14)));
            int x = padLeft + idx * slotWidth + (slotWidth - barWidth) / 2;
            int y = padTop + chartHeight - barHeight;

            Color cUtama = paletPink[idx % paletPink.length];
            Color cAtas = cUtama.brighter();
            GradientPaint gp = new GradientPaint(x, y, cAtas, x, y + barHeight, cUtama);
            g2.setPaint(gp);
            g2.fillRoundRect(x, y, barWidth, barHeight, 8, 8);

            // Label nominal di atas batang
            g2.setFont(new Font("SansSerif", Font.BOLD, 10));
            g2.setColor(new Color(131, 24, 67));
            String labelNilai = nilai > 0 ? "Rp" + nf.format(nilai) : "Rp0";
            int nilaiWidth = fm.stringWidth(labelNilai);
            g2.drawString(labelNilai, x + (barWidth - nilaiWidth) / 2, Math.max(14, y - 5));

            // Label kategori di bawah sumbu X (dipotong rapi agar tidak bertumpuk)
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            g2.setColor(new Color(80, 7, 36));
            int maxChars = Math.max(6, slotWidth / 6);
            String labelKat = kategori.length() > maxChars ? kategori.substring(0, maxChars - 1) + ".." : kategori;
            int katWidth = fm.stringWidth(labelKat);
            g2.drawString(labelKat, x + (barWidth - katWidth) / 2, padTop + chartHeight + 16);

            idx++;
        }
    }
}