package visualizer;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.json.JSONObject;
import simEngine.CostFct;

import javax.swing.*;
import java.util.List;

public class RunAnimations extends Thread {
    private final int amountOfAgents;
    private final Graph g;
    private final int usageAmount;
    private final List<String> agents;
    private final JLabel currentAgent;
    private final JLabel totalCost;
    private int totalAnimationTime;

    public RunAnimations(Graph g, int amountOfAgents, List<String> agents, JLabel currentAgent, JLabel totalCost) {
        this.amountOfAgents = amountOfAgents;
        this.usageAmount = g.getAttribute("usageAmount");
        this.g = g;
        this.agents = agents;
        totalAnimationTime = 3000;
        this.currentAgent = currentAgent;
        this.totalCost = totalCost;
    }

    public RunAnimations(Graph g, int amountOfAgents, List<String> agents, JLabel currentAgent, JLabel totalCost, int totalAnimationTime) {
        this(g, amountOfAgents, agents, currentAgent, totalCost);
        this.totalAnimationTime = totalAnimationTime;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(500);
            double totalCostD = 0;
            while (true) {
                for (String agent: agents) {
                    currentAgent.setText("Current agent running: " + agent);
                    for (int i = 0; i < usageAmount; i++) {
                        totalCostD = updateAnimationAgent(g, agent, i);
                        totalCost.setText("The total cost of the graph is: " + totalCostD);
                        Thread.sleep(totalAnimationTime/usageAmount);
                    }
                    //Let the final setup up for a bit
                    Thread.sleep(1000);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * Update the edges with colors corresponding to entities passes.
     */
    public double updateAnimationAgent(Graph graph, String agentName, int iteration) {
        double totalCost  = 0.0;
        for(Edge edge:graph.getEachEdge()) {
            List<Object> usageList = ((JSONObject) edge.getAttribute("usage")).getJSONArray(agentName).toList();
            int usage = ((Integer)usageList.get(iteration));
            CostFct c = edge.getAttribute("costFct");
            double thisCost = usage != 0 ? c.getCost(usage) : 0;
            totalCost += thisCost;
            edge.setAttribute("ui.label", c.toString(usage));
            double color = usage*1.0/amountOfAgents;
            edge.setAttribute("ui.color", color);
        }
        return totalCost;
    }
}