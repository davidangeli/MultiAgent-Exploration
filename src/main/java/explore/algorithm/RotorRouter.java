package main.java.explore.algorithm;

import lombok.Data;
import main.java.explore.Agent;
import main.java.explore.EdgeState;
import main.java.explore.GraphManager;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import java.util.ArrayList;
import java.util.HashSet;

public class RotorRouter implements Algorithm {
    private final static String AGENTLISTID = "agentlist";

    @Override
    public void init(Graph graph, ArrayList<Agent> agents, int agentNum) {
        //startnode
        Node startNode = graph.getNode(DEFAULT_START_INDEX);
        GraphManager.setStartNodeStyle(startNode);
        //reset labels, add agent set, storage
        graph.getNodeSet().forEach(n -> {
            n.addAttribute(AGENTLISTID, new HashSet<Agent>());
            n.addAttribute(STORAGEID, new RRStorage());
            n.removeAttribute(LABELID);
        });
        //agents
        for (int i =0; i < agentNum; i++) {
            Agent agent = new Agent(startNode, this);
            agents.add(agent);
        }
        //label startnode
        labelNode(null, startNode);
        //set edges to gray
        graph.getEdgeSet().forEach(EdgeState.UNVISITED::setEdge);
    }

    @Override
    public void evaluateOnArrival(Agent agent, Edge fromEdge) {
        Node currentNode = agent.getCurrentNode();
        HashSet<Agent> agentList = currentNode.getAttribute(AGENTLISTID);
        agentList.add(agent);

        if (fromEdge == null) return;
        //mark the fromEdge as used
        EdgeState.VISITED.setEdge(fromEdge);
        //update the previous nodes agentlist
        HashSet<Agent> agentListFrom = fromEdge.getOpposite(currentNode).getAttribute(AGENTLISTID);
        agentListFrom.remove(agent);
    }

    @Override
    public Edge selectNextStep(Agent agent) {
        Edge toGo;
        //get storage
        synchronized (STORELOCK) {
            RRStorage store = agent.getCurrentNode().getAttribute(STORAGEID);
            int route = store.routeIndex;
            toGo = agent.getCurrentNode().getEdge(route);
            route += 1;
            route = route % agent.getCurrentNode().getEdgeSet().size();
            store.setRouteIndex(route);
        }
        return toGo;
    }

    @Override
    public void labelNode(Agent agent, Node node) {
        String label;
        //get storage
        RRStorage store = node.getAttribute(STORAGEID);
        //agent list
        HashSet<Agent> agentList = node.getAttribute(AGENTLISTID);
        label = agentList.toString();

        if (!"".equals(label)) {
            label += ": " + store.toString();
        }
        else {
            label = node.getId() + store.toString();
        }

        node.setAttribute(LABELID, label);
    }

    @Override
    public boolean agentStops(Graph graph, Agent agent) {
        boolean edgesExplored = graph.getEdgeSet()
                .stream()
                .allMatch(e -> e.getAttribute(EDGESTATEID) == EdgeState.VISITED);
        boolean agentHome = agent.getCurrentNode().getIndex() == DEFAULT_START_INDEX;
        return edgesExplored && agentHome;
    }

    public static class RRMemory {
    }

    @Data
    public static class RRStorage {
        public int routeIndex = 0;
        @Override
        public String toString() {
            return Integer.toString(routeIndex);
        }
    }
}
