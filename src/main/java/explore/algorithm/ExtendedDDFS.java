package main.java.explore.algorithm;

import lombok.Data;
import main.java.explore.Agent;
import main.java.explore.graph.EdgeState;
import main.java.explore.graph.GraphManager;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

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

        //explore mode: mark edge, keep track
        if (!memory.isInSearchMode() && fromEdge != null) {
            //move backwards
            if (!memory.isEmpty() && fromEdge == memory.getLast()) {
                memory.removeLast();
            }
            //normal move
            else {
                if (store.getExploredBy() == 0) {
                    store.setExploredBy(agent.getId());
                }
                EdgeState.VISITED.setEdge(fromEdge);
                fromEdge.setAttribute(LABELID, store.getExploredBy());
                memory.add(fromEdge);
            }
        }

        //check if first visit in search mode: mark node, negate search mode
        if (store.getExploredBy() == 0) {
            store.setExploredBy(agent.getId());
            memory.setInSearchMode(false);
        }
        //acquire agent id if on foreign territory
        else if (store.getExploredBy() != agent.getId()) {
            memory.getKnownAgentIds().add(store.getExploredBy());
        }
    }

    @Override
    public Edge selectNextStep(Agent agent) {
        Node currentNode = agent.getCurrentNode();
        Edge nextEdge = null;
        MaEDDfsStorage store = currentNode.getAttribute(STORAGEID);
        MaEDDfsMemory memory = (MaEDDfsMemory)agent.getMemory();

        //explore mode
        if (!memory.isInSearchMode()) {
            //foreign territory
            //memory should only be empty in foreign if two agent start on same node:
            // but then the second starts in search mode
            if (store.getExploredBy() != agent.getId()) {
                if (!memory.isEmpty()) {
                    nextEdge = memory.getLast();
                }
            }
            //own territory
            else {
                Iterator<Edge> it = currentNode.getEdgeIterator();
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
                    //if reached the start node, set to search mode
                    else {
                        memory.setInSearchMode(true);
                    }
                }
            }
        }

        //search mode
        if (memory.isInSearchMode()) {
            assert (nextEdge == null);
            int degree = currentNode.getDegree();
            int route = (store.routeIndex + 1) % degree;
            nextEdge = currentNode.getEdge(route);
            int count = 0;
            while (nextEdge.getAttribute(EDGESTATEID) != EdgeState.UNVISITED && count < degree) {
                route = (route + 1) % degree;
                count++;
                nextEdge =currentNode.getEdge(route);
            }
            store.setRouteIndex(route);
        }

        return nextEdge;
    }

    @Override
    public boolean agentStops(Graph graph, ArrayList<Agent> agents, Agent agent) {
        MaEDDfsMemory memory = (MaEDDfsMemory) agent.getMemory();
        for (Agent a : agents) {
            if (memory.knownAgentIds.contains(a.getId())) {
                MaEDDfsMemory memory2 = (MaEDDfsMemory) a.getMemory();
                if (!memory2.isInSearchMode()) {
                    return false;
                }
            }
        }
        return memory.isInSearchMode();
    }

    @Data
    public static class MaEDDfsMemory extends LinkedList<Edge> {
        private boolean inSearchMode = true;
        private HashSet<Integer> knownAgentIds =new HashSet<>();
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
