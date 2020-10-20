package main.java.explore.algorithm;

import lombok.Data;
import main.java.explore.Agent;
import main.java.explore.graph.EdgeState;
import main.java.explore.graph.GraphManager;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

public class MultiAgentDDFS implements Algorithm {
    @Override
    public void init(Graph graph, ArrayList<Agent> agents, int agentNum) {
        agents.clear();
        Random random = new Random();
        //creates storage per Nodes
        graph.getNodeSet().forEach(n -> n.addAttribute(STORAGEID, new MaDDfsStorage()));
        //agents
        int[] startNodeIndexes = new int[agentNum];
        for (int i =0; i < agentNum; i++) {
            //startnode
            int n = random.nextInt(graph.getNodeCount());
            startNodeIndexes[i] = n;
            Node startNode = graph.getNode(n);
            //agent
            Agent agent = new Agent(startNode);
            agent.setMemory(new MaDDfsMemory());
            agents.add(agent);
            evaluateOnArrival(agent, null);
        }
        //set start node style and edges to gray
        GraphManager.setStartNodeStyle(graph, startNodeIndexes);
        graph.getEdgeSet().forEach(EdgeState.UNVISITED::setEdge);
    }

    @Override
    public void evaluateOnArrival(Agent agent, Edge fromEdge) {
        //get storage and memory
        MaDDfsStorage store = agent.getCurrentNode().getAttribute(STORAGEID);
        MaDDfsMemory memory = (MaDDfsMemory) agent.getMemory();

        //check and set if first visit
        if (store.getExploredBy() == 0) {
            store.setExploredBy(agent.getId());
        }
        int exploredBy = store.getExploredBy();

        //mark the fromEdge as used and which territory it belongs to, and add to path
        if (fromEdge != null) { //it is null at startNode
            //check if the agent goes backwards
            if (!memory.isEmpty() && fromEdge == memory.getLast()) {
                memory.removeLast();
            }
            //normal move, node already explored
            else if (exploredBy != agent.getId()) {
                EdgeState.VISITED.setEdge(fromEdge);
                fromEdge.setAttribute(LABELID, exploredBy);
                memory.add(fromEdge);
            }
            //normal move, unexplored node
            else {
                EdgeState.VISITED.setEdge(fromEdge);
                fromEdge.setAttribute(LABELID, exploredBy);
                memory.add(fromEdge);
            }
        }
    }

    @Override
    public Edge selectNextStep(Agent agent) {
        Edge nextEdge = null;
        MaDDfsStorage store = agent.getCurrentNode().getAttribute(STORAGEID);
        MaDDfsMemory memory = (MaDDfsMemory)agent.getMemory();

        //foreign territory
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

        return nextEdge;
    }

    @Override
    public boolean agentStops(Graph graph, Agent agent) {
        return selectNextStep(agent) == null;
    }


    public static class MaDDfsMemory extends LinkedList<Edge> {
    }

    @Data
    public static class MaDDfsStorage {
        private int exploredBy = 0;

        @Override
        public String toString() {
            return Integer.toString(exploredBy);
        }
    }
}
