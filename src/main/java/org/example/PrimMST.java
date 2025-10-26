package org.example;

public class PrimMST {
    public static class Result {
        public int totalWeight;
        public int[] parent;
        public Metrics metrics;

        Result(int totalWeight, int[] parent, Metrics metrics) {
            this.totalWeight = totalWeight;
            this.parent = parent.clone();
            this.metrics = metrics;
        }
    }

    public static Result findMST(Graph graph, Metrics metrics) {
        metrics.startTimer();
        int vertices = graph.getVertices();
        boolean[] visited = new boolean[vertices];
        int[] minWeight = new int[vertices];
        int[] parent = new int[vertices];
        java.util.PriorityQueue<Edge> pq = new java.util.PriorityQueue<>();

        java.util.Arrays.fill(minWeight, Integer.MAX_VALUE);
        java.util.Arrays.fill(parent, -1);
        minWeight[0] = 0;
        pq.offer(new Edge(-1, 0, 0)); // Start from vertex 0

        int totalWeight = 0;
        while (!pq.isEmpty()) {
            Edge edge = pq.poll();
            metrics.incrementPQOperation();
            int u = edge.v;

            if (visited[u]) continue;
            visited[u] = true;
            totalWeight += edge.weight;

            for (Edge e : graph.getEdges()) {
                int v = (e.u == u) ? e.v : (e.v == u) ? e.u : -1;
                if (v != -1 && !visited[v]) {
                    metrics.incrementComparison();
                    if (e.weight < minWeight[v]) {
                        minWeight[v] = e.weight;
                        parent[v] = u;
                        pq.offer(new Edge(u, v, e.weight));
                        metrics.incrementPQOperation();
                    }
                }
            }
        }

        metrics.stopTimer();
        metrics.setTotalCost(totalWeight);
        return new Result(totalWeight, parent, metrics);
    }
}