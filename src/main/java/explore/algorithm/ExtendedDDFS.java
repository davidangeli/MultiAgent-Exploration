package main.java.explore.algorithm;

import lombok.Data;
import main.java.explore.Agent;
import main.java.explore.graph.EdgeState;
import main.java.explore.graph.GraphManager;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;

import java.util.*;

public class ExtendedDDFS implements Algorithm {

    @Override
    public void init(Graph graph, ArrayList<Agent> agents, int agentNum) {
        agents.clear();
        //creates storage per Nodes
        graph.getNodeSet().forEach(n -> n.setAttribute(STORAGEID, new MaEDDfsStorage()));

        //(re)set start nodes and edges style and labels
        Random random = new Random();
        int[] startNodeIndexes = new int[agentNum];
        for (int i=0; i< startNodeIndexes.length; i++) {
            startNodeIndexes[i] = random.nextInt(graph.getNodeCount());
        }
        GraphManager.resetGraph(graph, startNodeIndexes);

        //create agents
        for (int i =0; i < agentNum; i++) {
            Agent agent = new Agent(graph.getNode(startNodeIndexes[i]));
            agent.setMemory(new MaEDDfsMemory());
            agents.add(agent);
            evaluateOnArrival(agent, null);
        }
    }

    @Override
    public void evaluateOnArrival(Agent agent, Edge fromEdge) {
        //get storage and memory
        MaEDDfsStorage store = agent.getCurrentNode().getAttribute(STORAGEID);
        MaEDDfsMemory memory = (MaEDDfsMemory) agent.getMemory();

        //check and set if first visit, negate search mode
        if (store.getExploredBy() == 0) {
            store.setExploredBy(agent.getId());
            memory.isInSearchMode = false;
        }

        //search mode
        if (memory.isInSearchMode) {
            //nothing to do here
        }
        //explore mode
        else {
            int exploredBy = store.getExploredBy();
            //mark the fromEdge as used and which territory it belongs to, and add to path
            if (fromEdge != null) { //it is null at startNode
                //move backwards
                if (!memory.isEmpty() && fromEdge == memory.getLast()) {
                    memory.removeLast();
                    //if reached the start node, set to search mode
                    if (memory.isEmpty() && selectNextStep(agent) == null) {
                        memory.setFinishedOnce(true);
                        memory.isInSearchMode = true;
                    }
                }
                //normal move
                else {
                    EdgeState.VISITED.setEdge(fromEdge);
                    fromEdge.setAttribute(LABELID, exploredBy);
                    memory.add(fromEdge);
                }
            }
        }
    }

    @Override
    public Edge selectNextStep(Agent agent) {
        Edge nextEdge = null;
        MaEDDfsStorage store = agent.getCurrentNode().getAttribute(STORAGEID);
        MaEDDfsMemory memory = (MaEDDfsMemory)agent.getMemory();

        //search mode
        if (memory.isInSearchMode) {
            int route = store.routeIndex;
            nextEdge = agent.getCurrentNode().getEdge(route);
            route += 1;
            route = route % agent.getCurrentNode().getEdgeSet().size();
            store.setRouteIndex(route);
        }
        else {
            //foreign territory
            //memory should only be in foreign if two agent start on same node:
            // but then the second starts in search mode
            if (store.getExploredBy() != agent.getId()) {
                if (!memory.isEmpty()) {
                    nextEdge = memory.getLast();
                }
            }
            //own territory
            else {
                Iterator<Edge> it = agent.getCurrentNode().getEdgeIterator();
                while (it.hasNext() && nextEdge == null) {
                    Edge edge = it.next();
                    if (edge.getAttribute(EDGESTATEID) == EdgeState.UNVISITED) {
                        nextEdge = edge;
                    }
                }

                if (nextEdge == null) {
                    if (!memory.isEmpty()) {
                        nextEdge = memory.getLast();
                    }
                }
            }
        }

        return nextEdge;
    }

    @Override
    public boolean agentStops(Graph graph, ArrayList<Agent> agents, Agent agent) {
        for (Agent a: agents) {
            MaEDDfsMemory memory = (MaEDDfsMemory)a.getMemory();
            if (!memory.isInSearchMode() && !memory.isFinishedOnce()) {
                return false;
            }
        }
        return true;
    }

    @Data
    public static class MaEDDfsMemory extends LinkedList<Edge> {
        private boolean finishedOnce = false;
        public boolean isInSearchMode = true;
    }

    @Data
    public static class MaEDDfsStorage {
        private int exploredBy = 0;
        public int routeIndex = 0;

        @Override
        public String toString() {
            return exploredBy + ", " + routeIndex;
        }
    }
}
