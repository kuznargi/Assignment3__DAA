package org.example;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class ReportPlotter {
    public static void generatePlots(Metrics[] metricsArray, String outDir) {
        List<Metrics> prim = new ArrayList<>();
        List<Metrics> kruskal = new ArrayList<>();
        for (Metrics m : metricsArray) {
            if (m == null) continue;
            String a = m.getAlgorithmName();
            if (a != null) {
                String al = a.toLowerCase();
                if (al.contains("prim")) {
                    prim.add(m);
                } else if (al.contains("kruskal")) {
                    kruskal.add(m);
                }
            }
        }
        Comparator<Metrics> byVThenId = Comparator.comparingInt(Metrics::getVertices).thenComparingInt(Metrics::getGraphId);
        prim.sort(byVThenId);
        kruskal.sort(byVThenId);

        File dir = new File(outDir);
        if (!dir.exists()) dir.mkdirs();

        double[][] primTime = toXY(prim, true);
        double[][] krTime = toXY(kruskal, true);
        double[][] primOps = toXY(prim, false);
        double[][] krOps = toXY(kruskal, false);

        drawLineChart(Arrays.asList(
                new Series("Prim", primTime, new Color(0x2E,0x7D,0x32)),
                new Series("Kruskal", krTime, new Color(0xC6,0x28,0x28))
        ), "Vertices", "Execution Time (ms)", 1800, 1000, new File(dir, "time_compare.png"));

        drawLineChart(Arrays.asList(new Series("Prim", primTime, new Color(0x2E,0x7D,0x32))),
                "Vertices", "Execution Time (ms)", 1600, 900, new File(dir, "time_prim.png"));

        drawLineChart(Arrays.asList(new Series("Kruskal", krTime, new Color(0xC6,0x28,0x28))),
                "Vertices", "Execution Time (ms)", 1600, 900, new File(dir, "time_kruskal.png"));

        drawLineChart(Arrays.asList(
                new Series("Prim ops", primOps, new Color(0x15,0x65,0xC0)),
                new Series("Kruskal ops", krOps, new Color(0xFB,0x8C,0x00))
        ), "Vertices", "Operations (comparisons+unions+pq)", 1800, 1000, new File(dir, "ops_compare.png"));
    }

    private static double[][] toXY(List<Metrics> list, boolean time) {
        double[][] xy = new double[2][list.size()];
        for (int i = 0; i < list.size(); i++) {
            Metrics m = list.get(i);
            xy[0][i] = m.getVertices();
            xy[1][i] = time ? m.getExecutionTimeMs() : m.getTotalOperations();
        }
        return xy;
    }

    private static class Series {
        final String name; final double[][] xy; final Color color;
        Series(String name, double[][] xy, Color color) { this.name = name; this.xy = xy; this.color = color; }
    }

    private static void drawLineChart(List<Series> seriesList, String xLabel, String yLabel, int width, int height, File outFile) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0,0,width,height);

        int marginLeft = 100, marginRight = 40, marginTop = 60, marginBottom = 100;
        int plotW = width - marginLeft - marginRight;
        int plotH = height - marginTop - marginBottom;
        int x0 = marginLeft, y0 = height - marginBottom;

        double minX = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
        for (Series s : seriesList) {
            for (int i = 0; i < s.xy[0].length; i++) {
                minX = Math.min(minX, s.xy[0][i]); maxX = Math.max(maxX, s.xy[0][i]);
                minY = Math.min(minY, s.xy[1][i]); maxY = Math.max(maxY, s.xy[1][i]);
            }
        }
        if (!Double.isFinite(minX) || !Double.isFinite(minY)) {
            g.dispose();
            try { ImageIO.write(img, "png", outFile); } catch (IOException ignored) {}
            return;
        }
        if (minY == maxY) { maxY = minY + 1; }
        if (minX == maxX) { maxX = minX + 1; }
        double padY = (maxY - minY) * 0.08; if (padY == 0) padY = 1;
        double padX = (maxX - minX) * 0.05; if (padX == 0) padX = 1;
        minY -= padY; maxY += padY; minX -= padX; maxX += padX;

        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(2f));
        g.drawLine(x0, y0, x0, y0 - plotH); // Y axis
        g.drawLine(x0, y0, x0 + plotW, y0); // X axis

        g.setFont(new Font("SansSerif", Font.PLAIN, 14));
        int ticks = 10;
        for (int i = 0; i <= ticks; i++) {
            int x = x0 + (int) Math.round(plotW * i / (double) ticks);
            int y = y0 - (int) Math.round(plotH * i / (double) ticks);
            g.setColor(new Color(0xDD,0xDD,0xDD));
            g.drawLine(x, y0, x, y0 - plotH);
            g.drawLine(x0, y, x0 + plotW, y);
            g.setColor(Color.DARK_GRAY);
            double xv = minX + (maxX - minX) * i / (double) ticks;
            double yv = minY + (maxY - minY) * i / (double) ticks;
            g.drawString(String.format("%.0f", xv), x - 15, y0 + 25);
            g.drawString(formatY(yv), x0 - 80, y + 5);
        }

        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        g.drawString(xLabel, x0 + plotW/2 - 40, height - 50);
        g.rotate(-Math.PI/2);
        g.drawString(yLabel, -(y0 - plotH/2 + 20), 40);
        g.rotate(Math.PI/2);

        g.setStroke(new BasicStroke(2.0f));
        for (Series s : seriesList) {
            g.setColor(s.color);
            for (int i = 1; i < s.xy[0].length; i++) {
                int x1 = x0 + (int) Math.round((s.xy[0][i-1] - minX) / (maxX - minX) * plotW);
                int y1 = y0 - (int) Math.round((s.xy[1][i-1] - minY) / (maxY - minY) * plotH);
                int x2 = x0 + (int) Math.round((s.xy[0][i] - minX) / (maxX - minX) * plotW);
                int y2 = y0 - (int) Math.round((s.xy[1][i] - minY) / (maxY - minY) * plotH);
                g.drawLine(x1, y1, x2, y2);
            }

            for (int i = 0; i < s.xy[0].length; i++) {
                int x = x0 + (int) Math.round((s.xy[0][i] - minX) / (maxX - minX) * plotW);
                int y = y0 - (int) Math.round((s.xy[1][i] - minY) / (maxY - minY) * plotH);
                g.fillOval(x-3, y-3, 6, 6);
            }
        }

        int legendX = x0 + 20;
        int legendY = marginTop - 20;
        g.setFont(new Font("SansSerif", Font.PLAIN, 16));
        for (Series s : seriesList) {
            g.setColor(s.color);
            g.fillRect(legendX, legendY - 12, 16, 10);
            g.setColor(Color.DARK_GRAY);
            g.drawString(s.name, legendX + 24, legendY - 2);
            legendX += 160;
        }

        g.dispose();
        try {
            ImageIO.write(img, "png", outFile);
            System.out.println("[ReportPlotter] Saved plot: " + outFile.getPath());
        } catch (IOException e) {
            System.err.println("[ReportPlotter] Failed to save plot: " + outFile.getPath() + ", " + e.getMessage());
        }
    }

    private static String formatY(double v) {
        if (v >= 1_000_000) return String.format("%.1fM", v/1_000_000.0);
        if (v >= 1_000) return String.format("%.1fk", v/1_000.0);
        return String.format("%.2f", v);
    }
}
