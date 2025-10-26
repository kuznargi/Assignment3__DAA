package org.example;

public class Prim {
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
        PrimMST.Result r = PrimMST.findMST(graph, metrics);
        return new Result(r.totalWeight, r.parent, r.metrics);
    }
}
