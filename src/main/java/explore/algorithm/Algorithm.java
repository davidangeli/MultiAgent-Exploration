package main.java.explore.algorithm;

import main.java.explore.Agent;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;

/**
 * Inteface declaring expected methods for any exploration algorithms.
 */
public interface Algorithm<M, S>  {

    Object STORELOCK = new Object();
    String LABELID = "ui.label";
    String STORAGEID = "storage";
    String EDGESTATEID = "edgestate";
    int DEFAULT_START_INDEX = 0;

    /**
     * Initializes the graph and the agents.
     * @param graph The graph.
     * @param agentNum The number of agents to have.
     */
    void init(Graph graph, ArrayList<Agent> agents, int agentNum);

    /**
     * Evaluates situation for an agent on a new node.
     * This method is intended to be called right after an agent moves.
     * @param agent The agent.
     * @param fromEdge The edge the agent is coming from.
     */
    void evaluateOnArrival(Agent agent, Edge fromEdge);

    /**
     * Selects an edge for an agent to move on.
     * This method should be called after every agent made their move and evaluation.
     * @param agent The agent.
     * @return The edge the agent should move on.
     */
    Edge selectNextStep(Agent agent);

    /**
     * This method sets initial labels on the graph for graphical runs.
     * @param graph The graph.
     */
    default void createLabels(Graph graph) {
        for (Node node : graph.getNodeSet()) {
            createLabel(node);
        }
    }

    default void createLabel (Node node) {
        LinkedHashSet<String> labels = new LinkedHashSet<>();
        labels.add(node.getId());
        node.setAttribute(LABELID, labels);
    }

    /**
     * This method updates labels on the graph for graphical runs.
     * @param agents The agent list.
     */
    default void updateLabels(ArrayList<Agent> agents) {
        //get nearby nodes
        LinkedHashSet<Node> affectedNodes = new LinkedHashSet<>();
        for (Agent agent: agents) {
            Iterator<Node> it = agent.getCurrentNode().getNeighborNodeIterator();
            while (it.hasNext()) {
                affectedNodes.add(it.next());
            }
        }

        //clear labels
        Iterator<Node> it = affectedNodes.iterator();
        while (it.hasNext()) {
            createLabel(it.next());
        }

        //add agents
        for (Agent agent: agents) {
            addLabel(agent.getCurrentNode(), agent.toString());
        }

        //add storage strings
        it = affectedNodes.iterator();
        while (it.hasNext()) {
            Node node = it.next();
            S nodeStore = node.getAttribute(STORAGEID);
            String nodeStoreSting = nodeStore.toString();
            addLabel(node, nodeStoreSting);
        }
    }

    default void addLabel(Node node, String label) {
        LinkedHashSet<String> labels = node.getAttribute(LABELID);
        labels.add(label);
        node.setAttribute(LABELID, labels);
    }

    default void removeLabel(Node node, String label) {
        LinkedHashSet<String> labels = node.getAttribute(LABELID);
        labels.remove(label);
        node.setAttribute(LABELID, labels);
    }

    /**
     * This method tells if an agent is finished in the current algorithm implementation.
     * @param graph The graph.
     * @param agent The agent.
     * @return True or false.
     */
    boolean agentStops(Graph graph, Agent agent);
}
