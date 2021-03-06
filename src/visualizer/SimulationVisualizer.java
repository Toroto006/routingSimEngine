package visualizer;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerPipe;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.graphstream.algorithm.Toolkit.randomNode;
import static org.graphstream.ui.graphicGraph.GraphPosLengthUtils.nodePosition;
import static visualizer.VisualizerUtils.createGraphFromJson;
import static visualizer.VisualizerUtils.getSimulationsFromJson;

public class SimulationVisualizer {


    public static void main(String[] args) {
        System.out.println("Starting SimulationVisualizer!");
        ArrayList<JSONObject> sims = getSimulationsFromJson("Simulation_out");
        System.out.println("Read simulations from json successfully!");
        ArrayList<RunAnimations> graphs = new ArrayList<>();
        for (JSONObject sim: sims) {
            //Create animation in viewer
            Graph g = createGraphFromJson(sim);
            JLabel currentAgent = new JLabel("Still starting!");
            JLabel totalCost = new JLabel("The total cost of the graph is: 0");
            List<String> agents = new LinkedList<>(((JSONObject) g.getEdge(0).getAttribute("usage")).keySet());
            graphs.add(new RunAnimations(g, sim.getInt("amountOfAgents"), agents, currentAgent, totalCost, 5000));
            //Make everything around the graph and it's animation
            JPanel graphPanel = new JPanel(new GridLayout()){
                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(640, 480);
                }
            };
            Viewer viewer = new Viewer(g, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
            viewer.enableAutoLayout();
            ViewPanel viewPanel = viewer.addDefaultView(false);
            graphPanel.add(viewPanel);
            //Create legend panel
            JPanel legend = new JPanel(new GridLayout(0, 2));
            currentAgent.setBounds(6, 6, 400, 60);
            legend.add(currentAgent);
            legend.add(new JLabel("Colors: linear interpolation from green, orange to red"));
            legend.add(totalCost);
            legend.add(new JLabel("        for the value (agents on the path)/(total amount of agents)"));
            legend.add(new JLabel(""));
            legend.add(new JLabel("TotalAmount of agents: " + sim.getInt("amountOfAgents")));
            //Combining everything
            JFrame simFrame = new JFrame();
            simFrame.setLayout(new BorderLayout());
            simFrame.add(legend, BorderLayout.NORTH);
            simFrame.add(graphPanel, BorderLayout.CENTER);
            simFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            simFrame.setTitle(sim.getString("networkTitle"));
            simFrame.pack();
            simFrame.setLocationRelativeTo(null);
            simFrame.setVisible(true);
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (Thread g: graphs)
            g.start();
        System.out.println("Started all simulations!");
        for (Thread g: graphs) {
            try {
                g.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
