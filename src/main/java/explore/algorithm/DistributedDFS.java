package main.java.explore.algorithm;

import lombok.Data;
import main.java.explore.Agent;
import main.java.explore.graph.EdgeState;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

public class DistributedDFS implements Algorithm<DistributedDFS.MaDDfsMemory, DistributedDFS.MaDDfsStorage> {

    //for graphical representation, territories are marked with different colors
    //there should not be more than 7 agents ever in gui
    public static final String[] EDGESTYLES = {
            "size: 3px;fill-color: rgb(245, 19, 15);",
            "size: 3px;fill-color: rgb(245, 218, 15);",
            "size: 3px;fill-color: rgb(99, 245, 15);",
            "size: 3px;fill-color: rgb(15, 245, 195);",
            "size: 3px;fill-color: rgb(15, 49, 245);",
            "size: 3px;fill-color: rgb(153, 15, 245);",
            "size: 3px;fill-color: rgb(245, 15, 180);"
    };

    @Override
    public void init(Graph graph, ArrayList<Agent> agents, int agentNum) throws Exception {
        Random random = new Random();
        int[] startNodeIndexes = new int[agentNum];
        for (int i=0; i< startNodeIndexes.length; i++) {
            startNodeIndexes[i] = random.nextInt(graph.getNodeCount());
        }
        Algorithm.super.init(graph, agents, agentNum, MaDDfsMemory.class, MaDDfsStorage.class, startNodeIndexes);
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
            //move backwards
            if (!memory.isEmpty() && fromEdge == memory.getLast()) {
                memory.removeLast();
            }
            //normal move
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
    public boolean agentStops(Graph graph, ArrayList<Agent> agents, Agent agent) {
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
