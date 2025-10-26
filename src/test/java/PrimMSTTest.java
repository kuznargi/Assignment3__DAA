import org.example.Graph;
import org.example.Metrics;
import org.example.InputJSONGenerator;
import org.example.Prim;
import org.example.Kruskal;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class PrimMSTTest {
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
        JSONObject g0 = graphs.getJSONObject(0);
        int id = g0.getInt("id");
        JSONArray edgesNumeric;
        int vertices;
        if (g0.has("nodes")) {
            JSONArray nodes = g0.getJSONArray("nodes");
            vertices = nodes.length();
            java.util.Map<String, Integer> map = new java.util.HashMap<>();
            for (int i = 0; i < nodes.length(); i++) map.put(nodes.getString(i), i);
            JSONArray edgesL = g0.getJSONArray("edges");
            edgesNumeric = new JSONArray();
            for (int i = 0; i < edgesL.length(); i++) {
                JSONObject e = edgesL.getJSONObject(i);
                edgesNumeric.put(new JSONObject()
                        .put("u", map.get(e.getString("from")))
                        .put("v", map.get(e.getString("to")))
                        .put("weight", e.getInt("weight")));
            }
        } else {
            vertices = g0.getInt("vertices");
            edgesNumeric = g0.getJSONArray("edges");
        }
        testGraph = new Graph(id, vertices, edgesNumeric);
    }

    @Test
    void testPrimMSTCorrectness() {
        Metrics metrics = new Metrics(testGraph.getId(), testGraph.getVertices(), testGraph.getEdges().size(), "Prim");
        Prim.Result result = Prim.findMST(testGraph, metrics);
        assertEquals(testGraph.getVertices() - 1, countEdges(result.parent), "MST should have V-1 edges");
        assertTrue(result.totalWeight > 0, "MST weight should be positive");
        assertTrue(isConnected(result.parent, testGraph.getVertices()), "MST should connect all vertices");
    }

    @Test
    void testPrimPerformance() {
        Metrics metrics = new Metrics(testGraph.getId(), testGraph.getVertices(), testGraph.getEdges().size(), "Prim");
        Prim.Result result = Prim.findMST(testGraph, metrics);
        assertTrue(metrics.getExecutionTimeMs() >= 0, "Execution time should be non-negative");
        assertTrue(metrics.getTotalOperations() >= 0, "Operation counts should be non-negative");
    }

    @Test
    void testPrimEqualsKruskalOnDatasetSamples() throws IOException {
        JSONArray graphs = new JSONObject(new String(Files.readAllBytes(Paths.get("data/input.json")))).getJSONArray("graphs");
        int[] idx = {0, 6, 15, 25}; // one from each size class
        for (int i : idx) {
            JSONObject g = graphs.getJSONObject(i);
            Graph graph = new Graph(g.getInt("id"), g.getInt("vertices"), g.getJSONArray("edges"));
            Metrics mPrim = new Metrics(graph.getId(), graph.getVertices(), graph.getEdges().size(), "Prim");
            Prim.Result rPrim = Prim.findMST(graph, mPrim);
            // Kruskal overload with metrics
            Metrics mK = new Metrics(graph.getId(), graph.getVertices(), graph.getEdges().size(), "Kruskal");
            Kruskal.Result rK = Kruskal.findMST(graph, mK);
            assertEquals(rK.totalWeight, rPrim.totalWeight, "Prim and Kruskal must have same MST weight for graph id=" + graph.getId());
            assertTrue(mPrim.getExecutionTimeMs() >= 0 && mK.getExecutionTimeMs() >= 0, "Non-negative times");
        }
    }

    @Test
    void testPrimParentArrayValidityOnSample() {
        Metrics metrics = new Metrics(testGraph.getId(), testGraph.getVertices(), testGraph.getEdges().size(), "Prim");
        Prim.Result result = Prim.findMST(testGraph, metrics);
        int V = testGraph.getVertices();
        int[] parent = result.parent;
        assertEquals(V, parent.length, "Parent array length must equal number of vertices");
        for (int v = 0; v < V; v++) {
            int p = parent[v];
            assertTrue(p >= -1 && p < V, "Parent in range [-1, V)");
            assertNotEquals(v, p, "No self-parenting");
        }
    }

    @Test
    void testPrimDeterministicK4() {
        int V = 4;
        JSONArray edges = new JSONArray()
                .put(new JSONObject().put("u",0).put("v",1).put("weight",1))
                .put(new JSONObject().put("u",0).put("v",2).put("weight",4))
                .put(new JSONObject().put("u",0).put("v",3).put("weight",3))
                .put(new JSONObject().put("u",1).put("v",2).put("weight",2))
                .put(new JSONObject().put("u",1).put("v",3).put("weight",5))
                .put(new JSONObject().put("u",2).put("v",3).put("weight",6));
        Graph g = new Graph(999, V, edges);
        Metrics m = new Metrics(g.getId(), g.getVertices(), g.getEdges().size(), "Prim");
        Prim.Result r = Prim.findMST(g, m);
        assertEquals(V - 1, countEdges(r.parent));
        assertEquals(6, r.totalWeight);
        assertTrue(m.getExecutionTimeMs() >= 0);
    }


    private int countEdges(int[] parent) {
        int edges = 0;
        for (int p : parent) if (p != -1) edges++;
        return edges;
    }

    private boolean isConnected(int[] parent, int vertices) {
        boolean[] visited = new boolean[vertices];
        dfs(0, parent, visited);
        for (boolean v : visited) if (!v) return false;
        return true;
    }

    private void dfs(int v, int[] parent, boolean[] visited) {
        visited[v] = true;
        for (int u = 0; u < parent.length; u++) {
            if (parent[u] == v && !visited[u]) dfs(u, parent, visited);
        }
    }

    @Test
    void testMSTWeightComparisonWithKruskal() throws IOException {
        JSONArray graphs = new JSONObject(new String(Files.readAllBytes(Paths.get("data/input.json")))).getJSONArray("graphs");
        int[] idx = {0, 6, 15, 25};
        for (int i : idx) {
            JSONObject g = graphs.getJSONObject(i);
            Graph graph = new Graph(g.getInt("id"), g.getInt("vertices"), g.getJSONArray("edges"));
            Metrics mPrim = new Metrics(graph.getId(), graph.getVertices(), graph.getEdges().size(), "Prim");
            Prim.Result rPrim = Prim.findMST(graph, mPrim);
            Metrics mK = new Metrics(graph.getId(), graph.getVertices(), graph.getEdges().size(), "Kruskal");
            Kruskal.Result rK = Kruskal.findMST(graph, mK);
            assertEquals(rK.totalWeight, rPrim.totalWeight, "Prim vs Kruskal MST weights must match for id=" + graph.getId());
        }
    }

    @Test
    void testConnectedGraph() {
        Metrics metrics = new Metrics(testGraph.getId(), testGraph.getVertices(), testGraph.getEdges().size(), "Prim");
        Prim.Result result = Prim.findMST(testGraph, metrics);
        assertEquals(testGraph.getVertices() - 1, countEdges(result.parent), "Connected graph MST must have V-1 edges");
        assertTrue(isConnected(result.parent, testGraph.getVertices()), "Resulting tree should connect all vertices");
    }

    @Test
    void testDisconnectedGraph() {
        int V = 6;
        JSONArray edges = new JSONArray()
                .put(new JSONObject().put("u",0).put("v",1).put("weight",1))
                .put(new JSONObject().put("u",1).put("v",2).put("weight",1))
                .put(new JSONObject().put("u",3).put("v",4).put("weight",1))
                .put(new JSONObject().put("u",4).put("v",5).put("weight",1));
        Graph g = new Graph(2001, V, edges);
        Metrics m = new Metrics(g.getId(), g.getVertices(), g.getEdges().size(), "Prim");
        Prim.Result r = Prim.findMST(g, m);
        int edgeCount = countEdges(r.parent);
        assertTrue(edgeCount < V - 1, "Disconnected graph cannot yield V-1 edges");
        assertFalse(isConnected(r.parent, V), "Disconnected graph should not become connected by Prim");
    }

    @Test
    void testCyclePrevention() {
        Metrics m = new Metrics(testGraph.getId(), testGraph.getVertices(), testGraph.getEdges().size(), "Prim");
        Prim.Result r = Prim.findMST(testGraph, m);
        int V = testGraph.getVertices();
        int[] uf = new int[V];
        java.util.Arrays.fill(uf, -1);
        for (int v = 0; v < V; v++) {
            int p = r.parent[v];
            if (p == -1) continue;
            int rx = ufFind(uf, v);
            int ry = ufFind(uf, p);
            assertNotEquals(rx, ry, "Cycle detected in parent edges");
            uf[rx] = ry;
        }
    }

    private int ufFind(int[] parentUF, int i) {
        if (parentUF[i] == -1) return i;
        return ufFind(parentUF, parentUF[i]);
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
        Graph g = new Graph(3001, V, edges);
        Metrics m1 = new Metrics(g.getId(), g.getVertices(), g.getEdges().size(), "Prim");
        Prim.Result r1 = Prim.findMST(g, m1);
        Metrics m2 = new Metrics(g.getId(), g.getVertices(), g.getEdges().size(), "Prim");
        Prim.Result r2 = Prim.findMST(g, m2);
        assertEquals(r1.totalWeight, r2.totalWeight, "Total weight should be stable");
        assertArrayEquals(r1.parent, r2.parent, "Parent arrays should be stable for deterministic input");
    }
}
