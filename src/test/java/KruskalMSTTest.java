import org.example.Graph;
import org.example.Edge;
import org.example.Kruskal;
import org.example.InputJSONGenerator;
import org.example.Metrics;
import org.example.Prim;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class KruskalMSTTest {
    private static Graph testGraph;

    @BeforeAll
    static void setUp() throws IOException {
        Path inputPath = Paths.get("data/input.json");
        if (!Files.exists(inputPath)) {
            new InputJSONGenerator().generateAndSave();
        }
        String content = new String(Files.readAllBytes(inputPath));
        JSONObject jsonObject = new JSONObject(content);
        JSONArray graphs = jsonObject.getJSONArray("graphs");
        testGraph = new Graph(graphs.getJSONObject(0).getInt("id"), graphs.getJSONObject(0).getInt("vertices"),
                graphs.getJSONObject(0).getJSONArray("edges"));
    }

    @Test
    void testKruskalMSTCorrectness() {
        Kruskal.Result result = Kruskal.findMST(testGraph);
        assertEquals(testGraph.getVertices() - 1, result.mstEdges.size(),
                "MST should have V-1 edges");
        assertTrue(result.totalWeight > 0, "MST weight should be positive");
        assertTrue(isAcyclic(result.mstEdges, testGraph.getVertices()),
                "MST should be acyclic");
        assertTrue(isConnected(result.mstEdges, testGraph.getVertices()),
                "MST should connect all vertices");
    }

    @Test
    void testKruskalPerformance() {
        Kruskal.Result result = Kruskal.findMST(testGraph);
        assertTrue(result.executionTime >= 0, "Execution time should be non-negative");
        assertTrue(result.comparisons >= 0 && result.unions >= 0,
                "Operation counts should be non-negative");
    }

    @Test
    void testKruskalEqualsPrimOnDatasetSamples() throws IOException {
        JSONArray graphs = new JSONObject(new String(Files.readAllBytes(Paths.get("data/input.json")))).getJSONArray("graphs");
        int[] idx = {0, 6, 15, 25};
        for (int i : idx) {
            JSONObject g = graphs.getJSONObject(i);
            Graph graph = new Graph(g.getInt("id"), g.getInt("vertices"), g.getJSONArray("edges"));

            Metrics mPrim = new Metrics(graph.getId(), graph.getVertices(), graph.getEdges().size(), "Prim");
            Prim.Result rPrim = Prim.findMST(graph, mPrim);

            Kruskal.Result rK = Kruskal.findMST(graph);
            assertEquals(rPrim.totalWeight, rK.totalWeight, "Prim and Kruskal must match on graph id=" + graph.getId());
            assertTrue(rK.executionTime >= 0);
        }
    }

    @Test
    void testKruskalDeterministicK4() {
        int V = 4;
        JSONArray edges = new JSONArray()
                .put(new JSONObject().put("u",0).put("v",1).put("weight",1))
                .put(new JSONObject().put("u",0).put("v",2).put("weight",4))
                .put(new JSONObject().put("u",0).put("v",3).put("weight",3))
                .put(new JSONObject().put("u",1).put("v",2).put("weight",2))
                .put(new JSONObject().put("u",1).put("v",3).put("weight",5))
                .put(new JSONObject().put("u",2).put("v",3).put("weight",6));
        Graph g = new Graph(1000, V, edges);
        Kruskal.Result r = Kruskal.findMST(g);
        assertEquals(V - 1, r.mstEdges.size());
        assertEquals(6, r.totalWeight);
        assertTrue(isAcyclic(r.mstEdges, V));
        assertTrue(isConnected(r.mstEdges, V));
    }

    @Test
    void testKruskalAcyclicAndEdgeCountOnSamples() throws IOException {
        JSONArray graphs = new JSONObject(new String(Files.readAllBytes(Paths.get("data/input.json")))).getJSONArray("graphs");
        int[] idx = {1, 7, 16, 26};
        for (int i : idx) {
            JSONObject g = graphs.getJSONObject(i);
            Graph graph = new Graph(g.getInt("id"), g.getInt("vertices"), g.getJSONArray("edges"));
            Kruskal.Result res = Kruskal.findMST(graph);
            assertEquals(graph.getVertices() - 1, res.mstEdges.size());
            assertTrue(isAcyclic(res.mstEdges, graph.getVertices()));
            assertTrue(isConnected(res.mstEdges, graph.getVertices()));
        }
    }

    @Test
    void testMSTWeightComparisonWithPrim() throws IOException {
        JSONArray graphs = new JSONObject(new String(Files.readAllBytes(Paths.get("data/input.json")))).getJSONArray("graphs");
        int[] idx = {0, 6, 15, 25};
        for (int i : idx) {
            JSONObject g = graphs.getJSONObject(i);
            Graph graph = new Graph(g.getInt("id"), g.getInt("vertices"), g.getJSONArray("edges"));
            Metrics mPrim = new Metrics(graph.getId(), graph.getVertices(), graph.getEdges().size(), "Prim");
            Prim.Result rPrim = Prim.findMST(graph, mPrim);
            Kruskal.Result rK = Kruskal.findMST(graph);
            assertEquals(rPrim.totalWeight, rK.totalWeight, "Kruskal vs Prim MST weights must match for id=" + graph.getId());
        }
    }

    @Test
    void testConnectedGraph() {
        Kruskal.Result result = Kruskal.findMST(testGraph);
        assertEquals(testGraph.getVertices() - 1, result.mstEdges.size(), "Connected graph MST must have V-1 edges");
        assertTrue(isAcyclic(result.mstEdges, testGraph.getVertices()));
        assertTrue(isConnected(result.mstEdges, testGraph.getVertices()));
    }

    @Test
    void testDisconnectedGraph() {
        int V = 6;
        JSONArray edges = new JSONArray()
                .put(new JSONObject().put("u",0).put("v",1).put("weight",1))
                .put(new JSONObject().put("u",1).put("v",2).put("weight",1))
                .put(new JSONObject().put("u",3).put("v",4).put("weight",1))
                .put(new JSONObject().put("u",4).put("v",5).put("weight",1));
        Graph g = new Graph(4001, V, edges);
        Kruskal.Result r = Kruskal.findMST(g);
        assertTrue(r.mstEdges.size() < V - 1, "Disconnected graph cannot yield V-1 edges");
        assertFalse(isConnected(r.mstEdges, V), "Disconnected graph should not become connected by Kruskal");
        assertTrue(isAcyclic(r.mstEdges, V), "Forest must be acyclic");
    }

    @Test
    void testCyclePrevention() {
        Kruskal.Result r = Kruskal.findMST(testGraph);
        assertTrue(isAcyclic(r.mstEdges, testGraph.getVertices()), "MST should be acyclic");
    }

    @Test
    void testReproducibility() {
        int V = 5;
        JSONArray edges = new JSONArray()
                .put(new JSONObject().put("u",0).put("v",1).put("weight",2))
                .put(new JSONObject().put("u",0).put("v",2).put("weight",3))
                .put(new JSONObject().put("u",1).put("v",2).put("weight",1))
                .put(new JSONObject().put("u",1).put("v",3).put("weight",4))
                .put(new JSONObject().put("u",2).put("v",4).put("weight",5))
                .put(new JSONObject().put("u",3).put("v",4).put("weight",6));
        Graph g = new Graph(5001, V, edges);
        Kruskal.Result r1 = Kruskal.findMST(g);
        Kruskal.Result r2 = Kruskal.findMST(g);
        assertEquals(r1.totalWeight, r2.totalWeight, "Total weight should be stable for deterministic input");
        // Also compare number of edges and their sum of weights for robustness
        assertEquals(r1.mstEdges.size(), r2.mstEdges.size());
    }

    private boolean isAcyclic(ArrayList<Edge> mstEdges, int vertices) {
        int[] parent = new int[vertices];
        Arrays.fill(parent, -1);
        for (Edge edge : mstEdges) {
            int x = find(parent, edge.u);
            int y = find(parent, edge.v);
            if (x == y) return false;
            parent[x] = y;
        }
        return true;
    }

    private int find(int[] parent, int i) {
        if (parent[i] == -1) return i;
        return find(parent, parent[i]);
    }

    private boolean isConnected(ArrayList<Edge> mstEdges, int vertices) {
        boolean[] visited = new boolean[vertices];
        if (mstEdges.isEmpty()) return true; // Handle empty graph
        dfs(mstEdges.get(0).u, mstEdges, visited);
        for (boolean v : visited) if (!v) return false;
        return true;
    }

    private void dfs(int v, ArrayList<Edge> edges, boolean[] visited) {
        visited[v] = true;
        for (Edge edge : edges) {
            if (edge.u == v && !visited[edge.v]) dfs(edge.v, edges, visited);
            else if (edge.v == v && !visited[edge.u]) dfs(edge.u, edges, visited);
        }
    }
}