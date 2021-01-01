package main.java.explore.algorithm;

import lombok.Data;
import main.java.explore.Agent;
import main.java.explore.graph.EdgeState;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;

import java.util.ArrayList;

public class RotorRouter implements Algorithm<RotorRouter.RRMemory, RotorRouter.RRStorage> {

    @Override
    public void init(Graph graph, ArrayList<Agent> agents, int agentNum) throws Exception {
        int[] startNodeIndexes = {DEFAULT_START_INDEX};
        Algorithm.super.init(graph, agents, agentNum, RRMemory.class, RRStorage.class, startNodeIndexes);
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
    public boolean agentStops(Graph graph, ArrayList<Agent> agents, Agent agent) {
        return graph.getEdgeSet()
                .stream()
                .allMatch(e -> e.getAttribute(EDGESTATEID) == EdgeState.VISITED);
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
