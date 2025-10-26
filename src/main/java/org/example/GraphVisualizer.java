package org.example;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;

public class GraphVisualizer {
//    public static void visualizeAll(List<Graph> graphs) {
//        for (Graph g : graphs) {
//            String path = "reports/graphs/graph_" + g.getId() + ".png";
//            visualize(g, path);
//        }
//    }


    public static void visualize(Graph graph, String outputPath) {
        int V = graph.getVertices();
        int height = (V >= 700) ? 2200 : 1600;
        int margin = 80;

        BufferedImage image = new BufferedImage(height, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, height, height);


        double cx = height / 2.0;
        double cy = height / 2.0;
        double radius = Math.min(height, height) / 2.0 - margin;
        double[] xs = new double[V];
        double[] ys = new double[V];
        for (int i = 0; i < V; i++) {
            double angle = 2 * Math.PI * i / V;
            xs[i] = cx + radius * Math.cos(angle);
            ys[i] = cy + radius * Math.sin(angle);
        }

        int maxEdgesToDraw = (V >= 700) ? 30000 : 200000;
        List<Edge> edges = graph.getEdges();
        int E = edges.size();
        int step = Math.max(1, E / Math.max(1, maxEdgesToDraw));
        g2.setColor(new Color(0x88, 0x88, 0x88, 120));
        g2.setStroke(new BasicStroke(1f));
        for (int i = 0; i < E; i += step) {
            Edge e = edges.get(i);
            int x1 = (int) Math.round(xs[e.u]);
            int y1 = (int) Math.round(ys[e.v]);
            int x2 = (int) Math.round(xs[e.v]);
            int y2 = (int) Math.round(ys[e.v]);
            y1 = (int) Math.round(ys[e.u]);
            g2.drawLine(x1, y1, x2, y2);
        }


        int nodeSize = (V >= 700) ? 6 : 10;
        g2.setColor(new Color(0x33, 0x66, 0xCC));
        for (int i = 0; i < V; i++) {
            int x = (int) Math.round(xs[i]) - nodeSize / 2;
            int y = (int) Math.round(ys[i]) - nodeSize / 2;
            g2.fillOval(x, y, nodeSize, nodeSize);
        }


        g2.setColor(Color.BLACK);
        g2.drawString("Graph ID: " + graph.getId() + " | V=" + V + " E=" + E, 20, 30);

        g2.dispose();


        File out = new File(outputPath);
        File parent = out.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();
        try {
            ImageIO.write(image, "png", out);
            System.out.println("[GraphVisualizer] Saved " + outputPath);
        } catch (IOException ex) {
            System.err.println("[GraphVisualizer] Failed to save " + outputPath + ": " + ex.getMessage());
        }
    }
}