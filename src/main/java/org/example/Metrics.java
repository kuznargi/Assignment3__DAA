package org.example;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

public class Metrics {
    private long comparisons = 0;
    private long unions = 0;
    private long pqOperations = 0;
    private long startTime;
    private double executionTimeMs = 0.0;
    private final int graphId;
    private final int vertices;
    private final int edges;
    private final String algorithmName;
    private int totalCost;

    public Metrics(int graphId, int vertices, int edges, String algorithmName) {
        this.graphId = graphId;
        this.vertices = vertices;
        this.edges = edges;
        this.algorithmName = algorithmName;
    }

    public void startTimer() {
        startTime = System.nanoTime();
    }

    public void stopTimer() {
        long endTime = System.nanoTime();
        long deltaNs = endTime - startTime;
        executionTimeMs = deltaNs / 1_000_000.0;
        if (executionTimeMs < 0) executionTimeMs = 0.0;
        if (deltaNs > 0 && executionTimeMs == 0.0) {
            executionTimeMs = 0.001;
        }
    }

    public void incrementComparison() {
        comparisons++;
    }

    public void incrementUnion() {
        unions++;
    }

    public void incrementPQOperation() {
        pqOperations++;
    }

    public void setTotalCost(int totalCost) {
        this.totalCost = totalCost;
    }

    public long getComparisons() {
        return comparisons;
    }

    public long getUnions() {
        return unions;
    }

//    public long getPQOperations() {
//        return pqOperations;
//    }

    public long getTotalOperations() {
        return comparisons + unions + pqOperations;
    }

    public double getExecutionTimeMs() {
        return executionTimeMs;
    }

    public int getGraphId() {
        return graphId;
    }

    public int getVertices() {
        return vertices;
    }

    public int getEdges() {
        return edges;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public int getTotalCost() {
        return totalCost;
    }

//    public void reset() {
//        comparisons = 0;
//        unions = 0;
//        pqOperations = 0;
//        executionTimeMs = 0.0;
//        totalCost = 0;
//    }

    @Override
    public String toString() {
        return "Metrics{graphId=" + graphId + ", vertices=" + vertices + ", edges=" + edges +
                ", algorithm=" + algorithmName + ", totalCost=" + totalCost +
                ", comparisons=" + comparisons + ", unions=" + unions +
                ", pqOperations=" + pqOperations + ", totalOps=" + getTotalOperations() +
                ", timeMs=" + String.format(Locale.US, "%.3f", executionTimeMs) + "}";
    }

    public static void writeCsv(String filePath, Metrics[] metricsArray, boolean append) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, append))) {
            if (!append) {
                writer.write("graph_id,vertices,edges,algorithm,total_cost,operations_count,execution_time_ms\n");
            }
            for (Metrics metrics : metricsArray) {
                String[] row = {
                        String.valueOf(metrics.getGraphId()),
                        String.valueOf(metrics.getVertices()),
                        String.valueOf(metrics.getEdges()),
                        metrics.getAlgorithmName(),
                        String.valueOf(metrics.getTotalCost()),
                        String.valueOf(metrics.getTotalOperations()),
                        String.format(Locale.US, "%.3f", metrics.getExecutionTimeMs())
                };
                writer.write(String.join(",", row) + "\n");
            }
        } catch (IOException e) {
            System.err.println("Error writing to CSV: " + e.getMessage());
            throw e;
        }
    }
}