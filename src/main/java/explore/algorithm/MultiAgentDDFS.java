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
        for (int i =0; i < agentNum; i++) {
            //startnode
            int n = random.nextInt(graph.getNodeCount());
            Node startNode = graph.getNode(n);
            GraphManager.setStartNodeStyle(graph, n);
            //agent
            Agent agent = new Agent(startNode);
            agent.setMemory(new MultiAgentDDFS());
            agents.add(agent);
            evaluateOnArrival(agent, null);
        }
        //set edges to gray
        graph.getEdgeSet().forEach(EdgeState.UNVISITED::setEdge);
    }

    @Override
    public void evaluateOnArrival(Agent agent, Edge fromEdge) {
        //get storage
        MaDDfsStorage store = agent.getCurrentNode().getAttribute(STORAGEID);

        //check if first visit
        if (store.getExploredBy() == 0) {
            store.setExploredBy(agent.getId());
        }
        int exploredBy = store.getExploredBy();

        //mark the fromEdge as used and which territory it belongs to, and add to path
        if (fromEdge != null) { //it is null at startNode
            EdgeState.VISITED.setEdge(fromEdge);
            fromEdge.setAttribute(LABELID, exploredBy);
            ((MaDDfsMemory)agent.getMemory()).add(fromEdge);
        }
    }

    @Override
    public Edge selectNextStep(Agent agent) {
        Edge nextEdge = null;
        Iterator<Edge> it = agent.getCurrentNode().getEdgeIterator();
        while (it.hasNext() && nextEdge == null) {
            Edge edge = it.next();
            if (edge.getAttribute(EDGESTATEID) == EdgeState.UNVISITED) {
                nextEdge = edge;
            }
        }

        if (nextEdge == null) {
            MaDDfsMemory memory = (MaDDfsMemory)agent.getMemory();
            if (!memory.isEmpty()) {
                nextEdge = memory.removeLast();
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
        private int exploredBy;
    }
}
