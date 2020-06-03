import java.util.HashMap;
import java.util.Map;

public class EdgeCosts {
    private final Map<String, Edge> edges;

    public EdgeCosts() {
        edges = new HashMap<>();
    }

    private String createKey(int i, int j) {
        if (i < j)
            return i + "" + j;
        return j + "" + i;
    }

    /**
     * Function to add edges
     * @param i from node
     * @param j to node
     * @param c the CostFct of this edge, given the number of agents on it
     */
    public void addEdge(int i, int j, CostFct c) {
        edges.put(createKey(i, j), new Edge(c));
    }

    /**
     * Calculates the latency cost of the identified edge calculate using the amount of agents on it
     * @param i from node
     * @param j to node
     * @return cost calculated
     */
    public int getEdgeCost(int i, int j) {
        return edges.get(createKey(i, j)).getCost();
    }

    public boolean contains(int i, int j) {
        return edges.containsKey(createKey(i, j));
    }

    public void copy(EdgeCosts edgeCosts) {
        this.edges.putAll(edgeCosts.edges);
    }

    public void addAgent(int i, int j) {
        edges.get(createKey(i, j)).addAgent();
    }
}