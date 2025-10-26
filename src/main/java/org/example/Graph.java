package org.example;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Graph {
    private final int id;
    private final int vertices;
    private final List<Edge> edges;

    public Graph(int id, int vertices, JSONArray edgesArray) {
        this.id = id;
        this.vertices = vertices;
        this.edges = new ArrayList<>();
        for (int i = 0; i < edgesArray.length(); i++) {
            JSONObject edgeObj = edgesArray.getJSONObject(i);
            edges.add(new Edge(edgeObj.getInt("u"), edgeObj.getInt("v"), edgeObj.getInt("weight")));
        }
    }

    public int getId() { return id; }
    public int getVertices() { return vertices; }
    public List<Edge> getEdges() { return new ArrayList<>(edges); }



}