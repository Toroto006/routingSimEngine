/**
 * This class is used to get the network and the cost of each edge
 */
public class NetworkCostGraph extends NetworkGraph{
    protected int[][] adjMatrix;

    public NetworkCostGraph(int numVertices) {
        super(numVertices);
        adjMatrix = new int[numVertices][numVertices];
    }

    public NetworkCostGraph(NetworkGraph networkGraph) {
        super(networkGraph.numVertices);
        adjMatrix = new int[numVertices][numVertices];
        this.edgeCosts.copy(networkGraph.edgeCosts);
        calculateNewCosts();
    }

    public int getLatencyCost(int i, int j) {
        return adjMatrix[i][j];
    }

    /**
     * This function calculates all edgeCosts of the adjMatrix anew
     */
    public void calculateNewCosts() {
        for (int i = 0; i < numVertices; i++) {
            for (int j = 0; j < numVertices; j++) {
                int val = Integer.MAX_VALUE;
                if (existsEdge(i, j)) {
                    val = edgeCosts.getEdgeCost(i, j);
                }
                adjMatrix[i][j] = val;
                adjMatrix[j][i] = val;
            }
        }
    }

    // Print the matrix
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < numVertices; i++) {
            s.append(i).append(": ");
            for (int j : adjMatrix[i]) {
                if (j == Integer.MAX_VALUE)
                    s.append("∞ ");
                else
                    s.append(j).append(" ");
            }
            s.append("\n");
        }
        return s.toString();
    }
}