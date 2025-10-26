package org.example;

import java.util.ArrayList;

public class KruskalMST {
    public static class Result {
        public int totalWeight;
        public ArrayList<Edge> mstEdges;
        public Metrics metrics;

        Result(int totalWeight, ArrayList<Edge> mstEdges, Metrics metrics) {
            this.totalWeight = totalWeight;
            this.mstEdges = mstEdges;
            this.metrics = metrics;
        }
    }

    private static int find(int[] parent, int i, Metrics metrics) {
        metrics.incrementComparison();
        if (parent[i] == -1) return i;
        return find(parent, parent[i], metrics);
    }

    private static void union(int[] parent, int x, int y, Metrics metrics) {
        int xRoot = find(parent, x, metrics);
        int yRoot = find(parent, y, metrics);
        if (xRoot != yRoot) {
            parent[xRoot] = yRoot;
            metrics.incrementUnion();
        }
    }

    public static Result findMST(Graph graph, Metrics metrics) {
        metrics.startTimer();
        int vertices = graph.getVertices();
        int[] parent = new int[vertices];
        java.util.Arrays.fill(parent, -1);
        ArrayList<Edge> edges = new ArrayList<>(graph.getEdges());
        java.util.Collections.sort(edges);

        int totalWeight = 0;
        ArrayList<Edge> mstEdges = new ArrayList<>();
        for (Edge edge : edges) {
            int x = find(parent, edge.u, metrics);
            int y = find(parent, edge.v, metrics);
            if (x != y) {
                union(parent, x, y, metrics);
                totalWeight += edge.weight;
                mstEdges.add(edge);
                if (mstEdges.size() == vertices - 1) break;
            }
        }

        metrics.stopTimer();
        metrics.setTotalCost(totalWeight);
        return new Result(totalWeight, mstEdges, metrics);
    }
}