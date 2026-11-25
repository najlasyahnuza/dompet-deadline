package com.dompetdeadline.ui;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class GrafikPengeluaranPanel extends JPanel {
    private Map<String, Long> dataPengeluaran = new LinkedHashMap<>();
    private final Color[] warnaBatang = {
        new Color(59, 130, 246),
        new Color(16, 185, 129),
        new Color(245, 158, 11),
        new Color(239, 68, 68),
        new Color(139, 92, 246)
    };

    public GrafikPengeluaranPanel() {
        setPreferredSize(new Dimension(420, 190));
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

        int width = getWidth();
        int height = getHeight();

        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        g2.setColor(new Color(30, 41, 59));
        g2.drawString("Grafik Pengeluaran per Kategori", 14, 22);

        if (dataPengeluaran.isEmpty()) {
            g2.setFont(new Font("SansSerif", Font.ITALIC, 12));
            g2.setColor(Color.GRAY);
            g2.drawString("Belum ada data pengeluaran.", 14, height / 2);
            return;
        }

        long maxVal = 0;
        for (long val : dataPengeluaran.values()) {
            if (val > maxVal) maxVal = val;
        }
        if (maxVal == 0) maxVal = 1;

        int padLeft = 18;
        int padRight = 18;
        int padTop = 42;
        int padBottom = 38;
        int chartWidth = width - padLeft - padRight;
        int chartHeight = height - padTop - padBottom;

        g2.setColor(new Color(226, 232, 240));
        g2.drawLine(padLeft, padTop + chartHeight, width - padRight, padTop + chartHeight);

        int count = dataPengeluaran.size();
        int slotWidth = chartWidth / Math.max(count, 1);
        int barWidth = Math.min(48, Math.max(20, slotWidth - 20));
        NumberFormat nf = NumberFormat.getInstance(new Locale("id", "ID"));

        int idx = 0;
        FontMetrics fm = g2.getFontMetrics(new Font("SansSerif", Font.PLAIN, 10));

        for (Map.Entry<String, Long> entry : dataPengeluaran.entrySet()) {
            String kategori = entry.getKey();
            long nilai = entry.getValue();

            int barHeight = (int) ((double) nilai / maxVal * (chartHeight - 15));
            int x = padLeft + idx * slotWidth + (slotWidth - barWidth) / 2;
            int y = padTop + chartHeight - barHeight;

            g2.setColor(warnaBatang[idx % warnaBatang.length]);
            g2.fillRoundRect(x, y, barWidth, barHeight, 6, 6);

            // Label nominal di atas batang
            g2.setFont(new Font("SansSerif", Font.BOLD, 10));
            g2.setColor(new Color(51, 65, 85));
            String labelNilai = nilai > 0 ? "Rp" + nf.format(nilai) : "0";
            int nilaiWidth = fm.stringWidth(labelNilai);
            g2.drawString(labelNilai, x + (barWidth - nilaiWidth) / 2, Math.max(padTop - 4, y - 4));

            // Label nama kategori di bawah batang (dipersingkat jika terlalu panjang)
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            String labelKat = kategori.length() > 11 ? kategori.substring(0, 10) + ".." : kategori;
            int katWidth = fm.stringWidth(labelKat);
            g2.drawString(labelKat, x + (barWidth - katWidth) / 2, padTop + chartHeight + 15);

            idx++;
        }
    }
}