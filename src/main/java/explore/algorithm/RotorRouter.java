package main.java.explore.algorithm;

import lombok.Data;
import main.java.explore.Agent;
import main.java.explore.graph.EdgeState;
import main.java.explore.graph.GraphManager;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import java.util.ArrayList;

public class RotorRouter implements Algorithm {

    @Override
    public void init(Graph graph, ArrayList<Agent> agents, int agentNum) {
        agents.clear();
        //startnode
        //int startNodeIndex = graph.getNodeCount() - 1;
        int startNodeIndex = DEFAULT_START_INDEX;
        Node startNode = graph.getNode(startNodeIndex);
        GraphManager.setStartNodeStyle(graph, startNodeIndex);
        //creates storage per Nodes
        graph.getNodeSet().forEach(n -> n.addAttribute(STORAGEID, new RRStorage()));
        //agents
        for (int i =0; i < agentNum; i++) {
            Agent agent = new Agent(startNode);
            agents.add(agent);
            evaluateOnArrival(agent, null);
        }
        //set edges to gray
        graph.getEdgeSet().forEach(EdgeState.UNVISITED::setEdge);
    }

    @Override
    public void evaluateOnArrival(Agent agent, Edge fromEdge) {
        //mark the fromEdge as used
        if (fromEdge == null) return;
        EdgeState.VISITED.setEdge(fromEdge);
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
    public boolean agentStops(Graph graph, Agent agent) {
        boolean edgesExplored = graph.getEdgeSet()
                .stream()
                .allMatch(e -> e.getAttribute(EDGESTATEID) == EdgeState.VISITED);
        boolean agentHome = agent.getCurrentNode().getIndex() == (int)graph.getAttribute(GraphManager.GRAPH_STARTNODE_INDEX);
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
