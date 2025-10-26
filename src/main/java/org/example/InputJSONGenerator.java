package org.example;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class InputJSONGenerator {
    private final JSONArray graphs;
    private final Random random;

    public InputJSONGenerator() {
        this.graphs = new JSONArray();
        this.random = new Random();
    }

    private static String labelForIndex(int idx) {
        StringBuilder sb = new StringBuilder();
        int n = idx;
        do {
            int rem = n % 26;
            sb.append((char)('A' + rem));
            n = n / 26 - 1;
        } while (n >= 0);
        return sb.reverse().toString();
    }

    private JSONObject generateRandomGraph(int id, int vertexCount) {
        JSONArray nodes = new JSONArray();
        for (int i = 0; i < vertexCount; i++) {
            nodes.put(labelForIndex(i));
        }

        boolean[][] edgeMatrix = new boolean[vertexCount][vertexCount];
        JSONArray edges = new JSONArray();
        for (int v = 1; v < vertexCount; v++) {
            int u = random.nextInt(v);
            int weight = 1500 + random.nextInt(2501);
            edgeMatrix[u][v] = edgeMatrix[v][u] = true;
            edges.put(new JSONObject()
                    .put("from", labelForIndex(u))
                    .put("to", labelForIndex(v))
                    .put("weight", weight));
        }

        int maxPossibleEdges = vertexCount * (vertexCount - 1) / 2;
        int targetEdges = Math.min((int) (maxPossibleEdges * 0.5), maxPossibleEdges);

        while (edges.length() < targetEdges) {
            int u = random.nextInt(vertexCount);
            int v = random.nextInt(vertexCount);
            if (u != v && !edgeMatrix[u][v]) {
                int weight = 1500 + random.nextInt(2501);
                edges.put(new JSONObject()
                        .put("from", labelForIndex(u))
                        .put("to", labelForIndex(v))
                        .put("weight", weight));
                edgeMatrix[u][v] = edgeMatrix[v][u] = true;
            }
        }

        JSONObject graph = new JSONObject();
        graph.put("id", id);
        graph.put("nodes", nodes);
        graph.put("edges", edges);
        return graph;
    }

    public void generateAndSave() throws IOException {
        int idCounter = 1;

        int[] smallNodes = {5, 10, 15, 20, 25, 30};
        for (int nodes : smallNodes) {
            graphs.put(generateRandomGraph(idCounter++, nodes));
        }

        int[] mediumNodes = {50, 100, 150, 200, 250, 300, 350, 400, 450};
        for (int nodes : mediumNodes) {
            graphs.put(generateRandomGraph(idCounter++, nodes));
        }

        int[] largeNodes = {500, 550, 600, 650, 700, 750, 800, 850, 900, 1000};
        for (int nodes : largeNodes) {
            graphs.put(generateRandomGraph(idCounter++, nodes));
        }

        int[] extraLargeNodes = {1300, 1500, 2000};
        for (int nodes : extraLargeNodes) {
            graphs.put(generateRandomGraph(idCounter++, nodes));
        }

        JSONObject output = new JSONObject();
        output.put("graphs", graphs);

        File dir = new File("data");
        if (!dir.exists()) dir.mkdirs();

        try (FileWriter file = new FileWriter("data/input.json")) {
            file.write(output.toString(4));
        }

        System.out.println("Saved " + graphs.length() + " graphs to data/input.json");
    }

    public static void main(String[] args) {
        InputJSONGenerator generator = new InputJSONGenerator();
        try {
            generator.generateAndSave();
        } catch (IOException e) {
            System.err.println("Failed: " + e.getMessage());
        }
    }
}