package main.java.explore.algorithm;

import main.java.explore.Agent;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import java.util.ArrayList;

/**
 * Inteface declaring expected methods for any exploration algorithms.
 */
public interface Algorithm  {

    Object STORELOCK = new Object();
    String LABELID = "ui.label";
    String STORAGEID = "storage";
    String EDGESTATEID = "edgestate";
    int DEFAULT_START_INDEX = 0;

    void init(Graph graph, ArrayList<Agent> agents, int agentNum);
    void evaluateOnArrival(Agent agent, Edge fromEdge);
    Edge selectNextStep(Agent agent);
    void labelNode (Agent agent, Node node);
    boolean agentStops(Graph graph, Agent agent);
}
