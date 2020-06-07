package main.java.explore.algorithm;

import lombok.Data;
import main.java.explore.Agent;
import main.java.explore.EdgeState;
import main.java.explore.Visit;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import static java.util.stream.Collectors.toSet;

public class MultiRobotDFS implements Algorithm<MultiRobotDFS.MrDfsStorage, MultiRobotDFS.MrDfsMemory> {

    @Override
    public void initGraph(Graph graph) {
        //creates storage per Nodes
        graph.getNodeSet().stream().forEach(n -> n.addAttribute(STORAGELABEL, new MrDfsStorage()));
        //set edges to gray
        graph.getEdgeSet().forEach(e -> EdgeState.UNVISITED.setEdge(e) );
    }

    @Override
    public void initAgent(Agent agent) {
        agent.setMemory(new MrDfsMemory());
    }

    @Override
    public void evaluatePosition(Agent agent) throws TimeoutException {
        //get storage and memory
        MrDfsStorage store = agent.getCurrentNode().getAttribute(STORAGELABEL);
        MrDfsMemory mem = (MrDfsMemory) agent.getMemory();

        //check if first visit
        boolean firsVisit = store.stream()
                .filter(v -> v.agent == agent)
                .count() == 0;

        //record this visit
        Edge fromEdge = mem.getFromEdge();
        EdgeState prevState = fromEdge == null ? EdgeState.UNVISITED : (EdgeState) fromEdge.getAttribute(EDGESTATELABEL);
        store.add(new MrDfsVisit (agent, fromEdge, firsVisit, prevState));

        //mark the fromEdge as used
        if (fromEdge != null) EdgeState.VISITED.setEdge(fromEdge);

        //if the agent has been here before
        if (!firsVisit) {
            EdgeState.FINISHED.setEdge(fromEdge);
        }
    }

    @Override
    public Edge selectNextStep(Agent agent) {
        //get storage and memory
        MrDfsStorage store = agent.getCurrentNode().getAttribute(STORAGELABEL);
        MrDfsMemory mem = (MrDfsMemory) agent.getMemory();

        Optional<MrDfsVisit> lastVisit = store.stream()
                .filter(v -> v.agent == agent)
                .sorted(Comparator.comparing(MrDfsVisit::getWhen).reversed())
                .findFirst();

        MrDfsVisit currentVisit = store.get(store.size()-1);
        Edge fromEdge = mem.getFromEdge();
        //if the agent has been here before
        //but now it came on a different edge then the one it used to left v the last time, it should go back
        if (lastVisit.isPresent() && (lastVisit.get().getTo() != fromEdge)) {
            currentVisit.setTo(fromEdge);
            return fromEdge;
        }

        //original entry edges for all robots
        Set<Edge> originalEdges = store.stream()
                .filter(v -> v.isOriginal() && v.from != null)
                .map(v -> v.from).collect(toSet());

        //original entry edge for this robot
        Optional<Edge> originalEdge = store.stream()
                .filter(v -> v.agent == agent && v.isOriginal() && v.from != null)
                .map(v -> v.from)
                .findFirst();

        //if there's an edge that is not finished and not original entry to any robots
        //prefer the least used edge (by all robots)
        Optional<Edge> to = agent.getCurrentNode().getEdgeSet().stream()
                .filter(e -> e.getAttribute(EDGESTATELABEL) != EdgeState.FINISHED && !originalEdges.contains(e))
                .sorted(Comparator.comparing(e -> store.stream().filter(v -> v.getTo() != null && v.getTo().equals(e)).count()))
                .findFirst();

        if (to.isPresent()){
            currentVisit.setTo(to.get());
            return to.get();
        }

        //otherwise - no unused edge, go back, using the original entry if exists
        if (originalEdge.isPresent()){
            currentVisit.setTo(originalEdge.get());
            return originalEdge.get();
        } else {
            //btw this can only happen at the very first step at startnode
            currentVisit.setTo(fromEdge);
            return fromEdge;
        }
    }

    @Data
    public class MrDfsMemory {
        private Edge fromEdge;
    }

    public class MrDfsStorage extends ArrayList<MrDfsVisit> {
    }

    @Data
    public class MrDfsVisit {
        public final Agent agent;
        public final Edge from;
        public final boolean original;
        private Edge to;
        private Instant when;
        private final EdgeState fromEdgePrevState;

        public MrDfsVisit(Agent agent, Edge from, boolean original, EdgeState state){
            this.agent = agent;
            this.from = from;
            this.original = original;
            this.fromEdgePrevState = state;
        }

        public boolean isNotFinished(){
            return when == null;
        }

        public void finish() {
            this.when = Instant.now();
        }

        @Override
        public String toString(){
            String label = this.agent.toString() + ": ";
            label += from == null ? "null" : from.getId();
            label += "->";
            label += to == null ? "null" : to.getId();
            return label;
        }
    }
}
