package org.example;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {

        InputJSONGenerator generator = new InputJSONGenerator();
        try {
            generator.generateAndSave();
        } catch (IOException e) {
            System.err.println("Failed to generate input.json: " + e.getMessage());
            return;
        }

        JSONArray results = new JSONArray();
        try {
            String content = new String(Files.readAllBytes(Paths.get("data/input.json")));
            JSONObject jsonObject = new JSONObject(content);
            JSONArray graphs = jsonObject.getJSONArray("graphs");

            Metrics[] allMetrics = new Metrics[graphs.length() * 2]; // For Prim and Kruskal
            int metricsIndex = 0;

            for (int i = 0; i < graphs.length(); i++) {
                JSONObject gobj = graphs.getJSONObject(i);
                int id = gobj.getInt("id");
                JSONArray nodes = gobj.getJSONArray("nodes");
                JSONArray edgesLabeled = gobj.getJSONArray("edges");

                // Build label -> index map
                Map<String, Integer> idx = new HashMap<>();
                String[] labels = new String[nodes.length()];
                for (int v = 0; v < nodes.length(); v++) {
                    String label = nodes.getString(v);
                    labels[v] = label;
                    idx.put(label, v);
                }

                // Convert labeled edges to numeric for internal Graph
                JSONArray edgesNumeric = new JSONArray();
                for (int e = 0; e < edgesLabeled.length(); e++) {
                    JSONObject eobj = edgesLabeled.getJSONObject(e);
                    String from = eobj.getString("from");
                    String to = eobj.getString("to");
                    int w = eobj.getInt("weight");
                    int u = idx.get(from);
                    int v = idx.get(to);
                    edgesNumeric.put(new JSONObject().put("u", u).put("v", v).put("weight", w));
                }

                Graph graph = new Graph(id, nodes.length(), edgesNumeric);

                Metrics primMetrics = new Metrics(graph.getId(), graph.getVertices(), graph.getEdges().size(), "Prim");
                Prim.Result primResult = Prim.findMST(graph, primMetrics);
                allMetrics[metricsIndex++] = primMetrics;

                Metrics kruskalMetrics = new Metrics(graph.getId(), graph.getVertices(), graph.getEdges().size(), "Kruskal");
                Kruskal.Result kruskalResult = Kruskal.findMST(graph, kruskalMetrics);
                allMetrics[metricsIndex++] = kruskalMetrics;

                JSONObject result = new JSONObject();
                result.put("graph_id", graph.getId());
                result.put("input_stats", new JSONObject()
                        .put("vertices", graph.getVertices())
                        .put("edges", graph.getEdges().size()));

                JSONObject primJson = new JSONObject();
                primJson.put("mst_edges", getLabeledEdgeList(primResult.parent, graph, labels));
                primJson.put("total_cost", primResult.totalWeight);
                primJson.put("operations_count", primMetrics.getTotalOperations());
                primJson.put("execution_time_ms", primMetrics.getExecutionTimeMs());
                result.put("prim", primJson);

                JSONObject krJson = new JSONObject();
                krJson.put("mst_edges", toLabeledEdgeArray(kruskalResult.mstEdges, labels));
                krJson.put("total_cost", kruskalResult.totalWeight);
                krJson.put("operations_count", kruskalMetrics.getTotalOperations());
                krJson.put("execution_time_ms", kruskalMetrics.getExecutionTimeMs());
                result.put("kruskal", krJson);

                results.put(result);

                GraphVisualizer.visualize(graph, "reports/graphs/graph_" + graph.getId() + ".png");
            }

            JSONObject output = new JSONObject();
            output.put("results", results);
            try (FileWriter file = new FileWriter("data/output.json")) {
                file.write(output.toString(4));
                System.out.println("✅ Saved results to data/output.json");
            }

            Metrics.writeCsv("data/results.csv", allMetrics, false);
            System.out.println("✅ Saved results to data/results.csv");

            ReportPlotter.generatePlots(allMetrics, "reports/plots");
        } catch (IOException e) {
            System.err.println("Failed to read/process input.json: " + e.getMessage());
        }
    }

    private static JSONArray getLabeledEdgeList(int[] parent, Graph graph, String[] labels) {
        JSONArray edgeList = new JSONArray();
        for (int v = 1; v < graph.getVertices(); v++) {
            if (parent[v] != -1) {
                edgeList.put(new JSONObject()
                        .put("from", labels[parent[v]])
                        .put("to", labels[v])
                        .put("weight", getEdgeWeight(parent[v], v, graph.getEdges())));
            }
        }
        return edgeList;
    }

    private static int getEdgeWeight(int u, int v, List<Edge> edges) {
        for (Edge edge : edges) {
            if ((edge.u == u && edge.v == v) || (edge.u == v && edge.v == u)) {
                return edge.weight;
            }
        }
        return -1;
    }

    private static JSONArray toLabeledEdgeArray(java.util.List<Edge> edges, String[] labels) {
        JSONArray arr = new JSONArray();
        if (edges != null) {
            for (Edge e : edges) {
                arr.put(new JSONObject().put("from", labels[e.u]).put("to", labels[e.v]).put("weight", e.weight));
            }
        }
        return arr;
    }
}