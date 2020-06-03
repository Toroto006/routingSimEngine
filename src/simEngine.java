import org.json.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;


public class simEngine {
    //Read file content into string with - Files.readAllBytes(Path path)
    //https://howtodoinjava.com/java/io/java-read-file-to-string-examples/
    private static String readAllBytesJava7(String filePath) throws IOException
    {
        String content = "";
        content = new String ( Files.readAllBytes( Paths.get(filePath) ) );
        return content;
    }

    /**
     * import network json, every vertex except dest has to have one outgoing edge
     * @param simulation
     */
    private static SimConfig importSimulationConfiguration(String simulation) throws Exception {
        //json in java https://www.tutorialspoint.com/json/json_java_example.htm
        //TODO implement the import and konversion of the json network and
        //we give the nodes numbers in lexicographic order for internal purpose
        //e.g. a graph with A B C as nodes will internally be a adj. matrix of size 3, where 0 is A
        String filePath = "./networks/"+simulation+".json";
        String jsonConfigString = null;
        try {
            jsonConfigString = readAllBytesJava7(filePath);
        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception("Loading the json file was not successful!");
        }
        JSONObject jsonObject = new JSONObject(jsonConfigString);
        //From now do convertion to config
        String[] nodes = jsonObject.getJSONArray("nodes").toList().toArray(new String[0]);
        int vertices = nodes.length;
        HashMap<String, Integer> nodesMapped = createNodeMappingSI(nodes);
        NetworkGraph networkGraph = new NetworkGraph(vertices);
        //Now read the edges
        Pattern pattern = Pattern.compile("(\\d*\\.?\\d*)t\\+(\\d*\\.?\\d*)", Pattern.MULTILINE);
        for(Object o : jsonObject.getJSONArray("edges")) {
            if (!(o instanceof JSONObject))
                throw new Exception("Decoding the json was not successful, edges are wrong/not given!");
            JSONObject edge = (JSONObject)o;
            int i = 0, j = 0;
            CostFct c = null;
            if (edge.keySet().size() != 2)
                throw new Exception("At least one edge has not the correct amount of keys!");
            for (Object k: edge.keySet() ) {
                if (!(k instanceof String))
                    throw new Exception("Decoding the json was not successful, edges do not have string keys!");
                if (k.equals("cost")){
                    String costFctStr = edge.getString((String)k);
                    Matcher m = pattern.matcher(costFctStr);
                    m.find();
                    try {
                        Float a = Float.valueOf(m.group(1));
                        Float b = Float.valueOf(m.group(2));
                        c = new LinearFct(a, b);
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new Exception("Something went wrong while decoding the linear function!");
                    }
                } else {
                    //We have the name of a vertex
                    i = nodesMapped.get(k);
                    j = nodesMapped.get(edge.getString((String)k));
                }
            }
            //System.out.println("From " + i + " to " + j + " and c: " + c);
            networkGraph.addEdge(i, j, c);
        }
        //Read the rest of the config
        int amountOfAgents = jsonObject.getInt("amountOfAgents");
        String networkTitle = jsonObject.getString("networkTitle");
        return new SimConfig(nodes, networkGraph, amountOfAgents, networkTitle);
    }

    private static HashMap<Integer, String> createNodeMappingIS(String[] nodes) {
        Arrays.sort(nodes);
        HashMap<Integer, String> ret = new HashMap<>();
        int i = 0;
        for (String s: nodes) {
            ret.put(i, s);
            i++;
        }
        return ret;
    }

    private static HashMap<String, Integer> createNodeMappingSI(String[] nodes) {
        Arrays.sort(nodes);
        HashMap<String, Integer> ret = new HashMap<>();
        int i = 0;
        for (String s: nodes) {
            ret.put(s, i);
            i++;
        }
        return ret;
    }

    /**
     * This will export the simulation, s.t. the visualizer can use it
     * @param exportSim
     */
    private static void exportSimulation(String exportSim){
        //json in java https://www.tutorialspoint.com/json/json_java_example.htm
        //TODO implement export Simulation
    }

    /**
     * This runs the whole simulation for one agent
     * @param simConfig the configuration of the simulation
     * @param agent the agent to use for this simulation
     */
    private static void runSimulation(SimConfig simConfig, NetworkAgent agent){
        NetworkCostGraph networkCostGraph = new NetworkCostGraph(simConfig.getNetworkGraph());
        for (int doneAgents = 0; doneAgents < simConfig.getAmountOfAgents(); doneAgents++) {
            //Run one agent
            LinkedList<Integer> agentPath = agent.agentDecide(networkCostGraph, networkCostGraph.getEdgeCosts(), doneAgents);
            for (int i = 0; i < agentPath.size()-1; i++){
                //Add the cost of this agent to the network
                networkCostGraph.addAgent(agentPath.get(i), agentPath.get(i+1));
            }
            //TODO somehow save the progress somewhere to then export simulation
        }
    }

    private static void runSimulationForAgent(NetworkAgent networkAgent, SimConfig simConfig, String simulationName){
        System.out.println("Starting the simulation of " + networkAgent.getClass().getName() + "!");
        runSimulation(simConfig, networkAgent);
        String exportSim = simulationName+"_" + networkAgent.getClass().getName();
        //TODO actually somehow return simulation result to export
        exportSimulation(exportSim);
        System.out.println("Simulation of " + networkAgent.getClass().getName() + " is finished and saved to " + exportSim + ".json");
    }

    public static void main(String[] args) {
        String simulation = "BrassParadox1";
        System.out.println("GameTheory simEngine started!");
        try {
            SimConfig simConfig = importSimulationConfiguration(simulation);
            System.out.println("Loading of " + simulation +" config successful!");
            //TODO set the correct agents here!
            System.out.println(simConfig.getNetworkGraph());
            System.exit(-1);
            //runSimulationForAgent(new Agent1(), simConfig, simulation);
            //runSimulationForAgent(new Agent2(), simConfig, simulation);
            //runSimulationForAgent(new Agent3(), simConfig, simulation);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Loading of " + simulation + " config was not successful!");
        }
        System.out.println("GameTheory simEngine finished, exiting!");
    }
}
