package main.java.explore.algorithm;

import lombok.Data;
import main.java.explore.Agent;
import main.java.explore.EdgeState;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This algorith uses no agent memory, but storage on each node.
 */
public class MultiRobotDFS implements Algorithm {

    @Override
    public void initGraph(Graph graph) {
        //creates storage per Nodes
        graph.getNodeSet().forEach(n -> n.addAttribute(STORAGEID, new MrDfsStorage()));
        //set edges to gray
        graph.getEdgeSet().forEach(EdgeState.UNVISITED::setEdge);
    }

    @Override
    public void initAgent(Agent agent) {
    }

    @Override
    public void evaluateOnArrival(Agent agent, Edge fromEdge) {
        //get storage and memory
        MrDfsStorage store = agent.getCurrentNode().getAttribute(STORAGEID);

        //check if first visit
        boolean firsVisit = store.stream().noneMatch(v -> v.agent == agent);

        //record this visit
        EdgeState prevState = fromEdge == null ? EdgeState.UNVISITED : (EdgeState) fromEdge.getAttribute(EDGESTATEID);
        synchronized (STORELOCK) {
            store.add(new MrDfsVisit(agent, fromEdge, firsVisit, prevState));
        }

        //mark the fromEdge as used
        if (fromEdge != null) EdgeState.VISITED.setEdge(fromEdge);

        //if the agent has been here before (->fromEdge cannot be null)
        if (!firsVisit) EdgeState.FINISHED.setEdge(fromEdge);
    }

    @Override
    public Edge selectNextStep(Agent agent) {
        //get storage
        MrDfsStorage store = agent.getCurrentNode().getAttribute(STORAGEID);

        //previous visit
        Optional<MrDfsVisit> lastVisit = store.stream()
                .filter(v -> v.agent == agent && v.isFinished())
                .reduce((first, second) -> second);

        //current one
        //noinspection OptionalGetWithoutIsPresent there must be a currentVisit at this point
        MrDfsVisit currentVisit = store.stream()
                .filter(v -> v.agent == agent)
                .reduce((first, second) -> second)
                .get();

        Edge fromEdge = currentVisit.getFrom();

        //if the agent has been here before
        //but now it came on a different edge then the one it used to left v the last time, it should go back
        if (lastVisit.isPresent() && (lastVisit.get().getTo() != fromEdge)) {
            currentVisit.setTo(fromEdge);
            return fromEdge;
        }

        //original entry edges for all robots
        Map<Agent, Edge> originalEdges = store.stream()
                .filter(v -> v.original && v.from != null)
                .collect(Collectors.toMap(MrDfsVisit::getAgent, MrDfsVisit::getFrom));

        //original entry edge for this robot
        Edge originalEdge = originalEdges.get(agent);

        //if there's an edge that is not finished and not original entry to any robots
        //prefer the least used edge (by all robots)
        Optional<Edge> to = agent.getCurrentNode().getEdgeSet().stream()
                .filter(e -> e.getAttribute(EDGESTATEID) != EdgeState.FINISHED && !originalEdges.containsValue(e))
                .min(Comparator.comparing(e -> store.stream().filter(v -> v.getTo() != null && v.getTo().equals(e)).count()));

        if (to.isPresent()){
            currentVisit.setTo(to.get());
            return to.get();
        }

        //otherwise - no unused edge, go back, using the original entry if exists
        if (originalEdge != null){
            currentVisit.setTo(originalEdge);
            return originalEdge;
        } else {
            //btw this can only happen at the very first step at startnode
            currentVisit.setTo(fromEdge);
            return fromEdge;
        }
    }

    @Override
    public void labelNode(Node node) {
        String label;
        //get storage
        MrDfsStorage store = node.getAttribute(STORAGEID);
        //agent list
        label = store.stream()
                .filter(v -> !v.isFinished())
                .map(v -> v.getAgent().toString())
                .collect(Collectors.joining(","));

        if (!"".equals(label)) {
            label += ": " + store.toString();
        }
        else {
            label = node.getId();
        }

        node.setAttribute(LABELID, label);
    }

    public static class MrDfsMemory {
    }

    public static class MrDfsStorage extends LinkedList<MrDfsVisit> {
    }

    /**
     * Subclass representing a visit on a Node by an Agent in the multirobot DFS algorithm.
     */
    @Data
    public static class MrDfsVisit {
        public final Agent agent;
        public final Edge from;
        public final boolean original;
        private Edge to;
        private final EdgeState fromEdgePrevState;

        public MrDfsVisit(Agent agent, Edge from, boolean original, EdgeState state){
            this.agent = agent;
            this.from = from;
            this.original = original;
            this.fromEdgePrevState = state;
        }

        public boolean isFinished(){
            return to != null;
        }

        @Override
        public String toString () {
            String label = this.agent.toString() + ": ";
            label += from == null ? "null" : from.getId();
            label += "->";
            label += to == null ? "null" : to.getId();
            return label;
        }
    }
}
