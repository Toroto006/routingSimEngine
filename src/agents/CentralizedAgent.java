package agents;

import simEngine.Edge;
import simEngine.EdgeCosts;
import simEngine.NetworkCostGraph;
import simEngine.SimConfig;

import java.util.*;

public class CentralizedAgent implements NetworkAgent {
    static HashMap<Integer, LinkedList<Integer>> solutions;
    static private int totAgents;
    static private EdgeCosts ec;
    static private HashMap<Integer[], Double> distributionCost = new HashMap<>();
    static private ArrayList<LinkedList<Integer>> uniquePaths;
    static private int nrUniquePaths;

    boolean[] visitedDFS;
    boolean[] visitedPath;
    // list of paths (LinkedList) for each node
    ArrayList<ArrayList<LinkedList<Integer>>> pathsToDest;

    @Override
    public LinkedList<Integer> agentDecide(NetworkCostGraph ncg, EdgeCosts ec1, int decidedAgents, int totalAgents) {
        // either calculate solution and use it or just use pre-calculated solution
        if (decidedAgents == 0 || solutions.isEmpty()) {
            totAgents= totalAgents;
            ec = ec1;
            solutions = new HashMap<>(totalAgents);
            solutions = completeSolution(ncg);
        }
        return solutions.get(decidedAgents);
    }

    /**
     * This will calculate the solution for all agents by finding the optimal paths and corresponding loads.
     *
     * @param ncg the adj. matrix of the current latency cost for the network to decide upon
     * @return Map of agent nr. to an optimal path. Multiple agents may refer to same path.
     */
    public HashMap<Integer, LinkedList<Integer>> completeSolution(NetworkCostGraph ncg) {
        uniquePaths = getUniquePaths(ncg);
        nrUniquePaths = uniquePaths.size();

        // calculate all distribution costs
        recDistributionCost(0, new Integer[nrUniquePaths], totAgents);

        // find minimal value of all distribution costs
        double min = Integer.MAX_VALUE;
        for (Map.Entry<Integer[], Double> current : distributionCost.entrySet()) {
            min = Math.min(min, current.getValue());
        }

        // save all distributions that create a minimal value in list
        ArrayList<Integer[]> bestDistributions = new ArrayList<>();
        for (Map.Entry<Integer[], Double> current : distributionCost.entrySet()) {
            if (current.getValue() == min) {
                bestDistributions.add(current.getKey());
            }
        }

        // do not know how to handle multiple minimal distributions
        // for now just choosing first one
        Integer[] bestDistribution = bestDistributions.get(0);

        HashMap<Integer, LinkedList<Integer>> solutionsPerAgent = new HashMap<>(totAgents);
        int agentsum = 0;
        for (int i = 0; i < bestDistribution.length; i++) {
            int agents = bestDistribution[i];
            for (int j = 0; j < agents; j++) {
                solutionsPerAgent.put(agentsum+j, uniquePaths.get(i));
            }
            agentsum += agents;
        }

        return solutionsPerAgent;
    }

    /**
     * Recursive function to calculate all distributions over unique paths and save cost of each
     * distribution-path-combination in DP table.
     */
    public void recDistributionCost(int current, Integer[] distribution, int leftToDistribute) {
        if (current == nrUniquePaths) {
            distribution[current] = leftToDistribute;
            distributionCost.put(distribution, calcDistributionCost(distribution));
            return;
        }
        for (int i = 0; i < leftToDistribute; i++) {
            distribution[current] = i;
            recDistributionCost(current+1, distribution, leftToDistribute-1);
        }
    }

    /**
     * This will create a list of all unique paths without loops from source to destination.
     *
     * @param ncg the adj. matrix of the current latency cost for the network to decide upon
     * @return List of all unique paths.
     */
    public ArrayList<LinkedList<Integer>> getUniquePaths(NetworkCostGraph ncg) {
        int source = 0;
        int dest = ncg.getAdjMatrix().length - 1;
        int numVertices = ncg.getNumVertices();

        //create adjacency list
        LinkedList<Integer>[] adjList = new LinkedList[numVertices];
        for (int i = 0; i < adjList.length; i++) {
            adjList[i] = new LinkedList<Integer>();
            for (int j = 0; j < numVertices; j++) {
                if (ncg.getAdjMatrix()[i][j] != 0) {
                    adjList[i].add(j);
                }
            }
        }

        // use modified DFS to determine unique paths
        visitedDFS = new boolean[numVertices];
        visitedPath = new boolean[numVertices];

        Stack<Integer> stack = new Stack<>();
        LinkedList<Integer> currentPath = new LinkedList<>();
        uniquePaths = new ArrayList<>();
        // list of paths (LinkedList) for each node
        pathsToDest = new ArrayList<>(numVertices);
        for (int i = 0; i < numVertices; i++) {
            pathsToDest.add(new ArrayList<>());
        }

        stack.push(source);

        while(!stack.isEmpty()) {
            int current = stack.peek();

            // found node that was already in path, need to back up and delete corresponding edges
            if (visitedPath[current] == true) {
                // go 1 node back
                current = stack.pop();
                currentPath.removeLast();
                visitedPath[current] = false;
                while(visitedPath[current]) {
                    stack.pop();
                    // not updating visitedDFS because if it's true it has been technically processed by DFS
                    visitedPath[current] = false;
                    currentPath.removeLast();
                    current = stack.peek();
                }
                // have found node that not has been in path, might have been in DFS
                if (visitedDFS[current]) {

                }
            }
            if (visitedDFS[current]) {

            }
            // found complete, new path from source to destination
            if (current == dest) {
                uniquePaths.add(currentPath);
                currentPath.clear();
                // backtrack to last node with unvisited neighbours -> done by stack, still need to unmark nodes up until then in visitedPath
                int u = stack.peek();
                while (visitedDFS[u]) {
                    stack.pop();
                    visitedPath[u] = false;
                }
                // make sure dest can be reached again
                visitedDFS[dest] = false;
                // use u as new current node
                current = u;
            }
            // add node to path if never been seen before in DFS and current path
            if (!visitedDFS[current] && !visitedPath[current]) {
                currentPath.add(current);
                visitedDFS[current] = true;
                visitedPath[current] = true;
                //stack.pop(); //might double pop if current == dest, actually no popping yet
            }

            Iterator it = adjList[current].listIterator();

            while (it.hasNext()) {
                int v = (int) it.next();
                if (!visitedDFS[v]) stack.push(v);
            }
        }

        return null;
    }

    private void prependNodeToPath (int current, int next) {
        Iterator<LinkedList<Integer>> it = pathsToDest.get(next).listIterator();
        LinkedList<Integer> list = new LinkedList<>();
        while (it.hasNext()) {
            list.clear();
            list.add(current);
            list.addAll(it.next()); // now current is prepended to a path from next to dest
            pathsToDest.get(current).add(list);
        }
    }

    /**
     * Calculate cost of a given path.
     *
     * @param path path as a list of nodes
     * @return Cost as a real value.
     */
    public double calcPathCost(LinkedList<Integer> path) {
        double pathCost = 0;
        // empty path or only 1 node
        if (path.isEmpty() || path.size() < 2) {
            return 0.0;
        }

        // path of at least length 1

        Iterator<Integer> it = path.listIterator();

        int current = it.next();
        int next = it.next();
        while (it.hasNext()) {
            pathCost += ec.getEdgeCost(current, next);
            current = next;
            next = it.next();
        }
        pathCost += ec.getEdgeCost(current, next);
        return pathCost;
    }

    /**
     * Calculates the total cost of a given distribution
     *
     * @param distribution distribution of agents over unique paths.
     * @return Cost to route agents according to distribution over paths
     */
    public Double calcDistributionCost(Integer[] distribution) {
        double distributionSum = 0;
        for (int i = 0; i < uniquePaths.size(); i++) {
            distributionSum += distribution[i] * calcPathCost(uniquePaths.get(i));
        }
        return (1.0/totAgents*distributionSum);
    }
}
