package org.example;

import java.util.ArrayList;

public class Kruskal {
    public static class Result {
        public int totalWeight;
        public ArrayList<Edge> mstEdges;
        public long executionTime;
        public long comparisons;
        public long unions;
        public Metrics metrics;

        Result(int totalWeight, ArrayList<Edge> mstEdges, Metrics metrics) {
            this.totalWeight = totalWeight;
            this.mstEdges = mstEdges;
            this.metrics = metrics;
            if (metrics != null) {
                this.executionTime = (long) metrics.getExecutionTimeMs();
                this.comparisons = metrics.getComparisons();
                this.unions = metrics.getUnions();
            }
        }
    }

    public static Result findMST(Graph graph) {
        Metrics m = new Metrics(graph.getId(), graph.getVertices(), graph.getEdges().size(), "Kruskal");
        return findMST(graph, m);
    }

    public static Result findMST(Graph graph, Metrics metrics) {
        KruskalMST.Result r = KruskalMST.findMST(graph, metrics);
        return new Result(r.totalWeight, r.mstEdges, r.metrics);
    }
}
