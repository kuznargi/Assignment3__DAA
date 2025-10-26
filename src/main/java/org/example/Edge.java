package org.example;

public class Edge implements Comparable<Edge> {
    public int u, v, weight;

    public Edge(int u, int v, int weight) {
        this.u = u;
        this.v = v;
        this.weight = weight;
    }

    @Override
    public int compareTo(Edge other) {
        return Integer.compare(this.weight, other.weight);
    }

    @Override
    public String toString() {
        return "{" + u + "-" + v + ": " + weight + "}";
    }
}