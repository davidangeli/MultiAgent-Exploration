package main.java.explore.algorithm;

import lombok.Data;
import main.java.explore.Agent;
import main.java.explore.graph.EdgeState;
import main.java.explore.graph.GraphManager;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This algorithm uses no agent memory, but storage on each node.
 */
public class MultiAgentDFS implements Algorithm {

    @Override
    public void init(Graph graph, ArrayList<Agent> agents, int agentNum) {
        agents.clear();
        //creates storage per Nodes
        graph.getNodeSet().forEach(n -> n.addAttribute(STORAGEID, new MaDfsStorage()));

        //(re)set start nodes and edges style and labels
        int[] startNodeIndexes = {DEFAULT_START_INDEX};
        GraphManager.resetGraph(graph, startNodeIndexes);

        //create agents
        Node startNode = graph.getNode(startNodeIndexes[0]);
        for (int i =0; i < agentNum; i++) {
            Agent agent = new Agent(startNode);
            agents.add(agent);
            evaluateOnArrival(agent, null);
        }
    }

    @Override
    public void evaluateOnArrival(Agent agent, Edge fromEdge) {
        //get storage and memory
        MaDfsStorage store = agent.getCurrentNode().getAttribute(STORAGEID);

        //check if first visit
        boolean firsVisit = store.stream().noneMatch(v -> v.agent == agent);

        //record this visit
        EdgeState prevState = fromEdge == null ? EdgeState.UNVISITED : (EdgeState) fromEdge.getAttribute(EDGESTATEID);
        synchronized (STORELOCK) {
            store.add(new MaDfsVisit(agent, fromEdge, firsVisit, prevState));
        }

        //mark the fromEdge as used
        if (fromEdge != null) EdgeState.VISITED.setEdge(fromEdge);

        //if the agent has been here before (->fromEdge cannot be null)
        if (!firsVisit) EdgeState.FINISHED.setEdge(fromEdge);
    }

    @Override
    public Edge selectNextStep(Agent agent) {
        //get storage
        MaDfsStorage store = agent.getCurrentNode().getAttribute(STORAGEID);

        //previous visit
        Optional<MaDfsVisit> lastVisit = store.stream()
                .filter(v -> v.agent == agent && v.isFinished())
                .reduce((first, second) -> second);

        //current one
        //noinspection OptionalGetWithoutIsPresent there must be a currentVisit at this point
        MaDfsVisit currentVisit = store.stream()
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

        //original entry edges for all agents
        Map<Agent, Edge> originalEdges = store.stream()
                .filter(v -> v.original && v.from != null)
                .collect(Collectors.toMap(MaDfsVisit::getAgent, MaDfsVisit::getFrom));

        //original entry edge for this agent
        Edge originalEdge = originalEdges.get(agent);

        //if there's an edge that is not finished and not original entry to any agents
        //prefer the least used edge (by all agents)
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
    public boolean agentStops(Graph graph, Agent agent) {
        boolean edgesExplored = graph.getEdgeSet()
                .stream()
                .allMatch(e -> e.getAttribute(EDGESTATEID) == EdgeState.FINISHED);
        boolean agentHome = agent.getCurrentNode().getIndex() == ((int[])graph.getAttribute(GraphManager.GRAPH_STARTNODE_INDEX))[0];
        return edgesExplored && agentHome;
    }

    public static class MaDfsMemory {
    }

    public static class MaDfsStorage extends LinkedList<MaDfsVisit> {
    }

    /**
     * Subclass representing a visit on a Node by an Agent in the multiagent DFS algorithm.
     */
    @Data
    public static class MaDfsVisit {
        public final Agent agent;
        public final Edge from;
        public final boolean original;
        private Edge to;
        private final EdgeState fromEdgePrevState;

        public MaDfsVisit(Agent agent, Edge from, boolean original, EdgeState state){
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
            String label = this.agent.getCode() + ":";
            label += from == null ? "null" : from.getId();
            label += "->";
            label += to == null ? "null" : to.getId();
            return label;
        }
    }
}
