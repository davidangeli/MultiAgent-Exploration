package main.java.explore;

import main.java.explore.algorithm.Algorithm;
import org.graphstream.algorithm.generator.Generator;
import org.graphstream.algorithm.generator.LobsterGenerator;
import org.graphstream.algorithm.generator.RandomGenerator;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import static main.java.explore.algorithm.Algorithm.EDGESTATEID;
import static main.java.explore.algorithm.Algorithm.LABELID;

public class Controller implements Runnable {
    private Graph graph = new SingleGraph("MultiAgent");
    private final Algorithm algorithm;
    private ArrayList<Agent> agents = new ArrayList<>();
    private Node startNode;
    private boolean paused = true;
    private int ticks=0;
    private Viewer viewer;
    private Thread thread = new Thread(this);
    private Gui gui;

    public AtomicBoolean stopped = new AtomicBoolean(true);
    private final static int STARTNODEINDEX = 0;

    public Controller(int r, String graphType, Algorithm algorithm) {
        this.algorithm = algorithm;
        //graph-robot setup. should contain at least one node
        init(graphType, r);
    }

    public synchronized void reset(int r) {
        //if (!isFinished() || stopped.get()) return;
        paused = true;
        setGraph();
        agents.clear();
        for (int i =0; i < r; i++) {
            agents.add(new Agent(startNode, algorithm));
        }
        graph.getNodeSet().forEach(n -> n.removeAttribute(LABELID));
        algorithm.labelNode(startNode);
        ticks=0;
    }

    public synchronized void init(String graphType, int r) {
        paused = true;
        //if (!isFinished() || stopped.get()) return;
        createGraph2(graphType);
        reset(r);
    }
    public boolean isRunning() {
        return thread.isAlive();
    }
    public void start() {
        if (!stopped.get()) return;
        thread = new Thread(this);
        stopped.set(false);
        thread.start();
    }

    public void setGui(Gui g) {
        this.gui=g;
    }

    @Override
    public void run() {

        //startnode label & display graph
        algorithm.labelNode(startNode);

        //loop
        while (!isFinished() && !stopped.get()){

            if (!paused) tick();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                //TODO log
            }
        }
        stopped.set(true);
        System.out.println("Graph explored in " + ticks + " steps");
    }

    private synchronized void tick () {
        ticks++;

        //remove labels, move agents, set labels
        boolean graphExplored = edgesFinished();
        agents.forEach(a -> {
            if (!graphExplored || (a.getCurrentNode() != startNode)) {
                Node prevNode = a.getCurrentNode();
                a.move();
                algorithm.labelNode(prevNode);
                algorithm.labelNode(a.getCurrentNode());
            }
        });

        gui.setSteps(ticks);
    }

    private boolean isFinished() {
        boolean agentsFinished = agents.stream().allMatch(r -> r.getCurrentNode() == startNode);
        return agentsFinished && edgesFinished();
    }

    private boolean edgesFinished() {
        return graph.getEdgeSet().stream().allMatch(e -> e.getAttribute(EDGESTATEID) == EdgeState.FINISHED);
    }

    public ViewPanel getViewPanel() {
        if (viewer != null) viewer.close();
        viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
        viewer.enableAutoLayout();
        return viewer.addDefaultView(false);
    }

    public synchronized void pause() {
        paused = !paused;
    }

    public synchronized void tickOne() {
        if (paused) tick();
    }

    private void setGraph() {

        startNode = graph.getNode(STARTNODEINDEX);
        startNode.addAttribute("ui.style", "size: 20;");
        algorithm.initGraph(graph);
    }

    private void createGraph1(){

        graph.addNode("A" );
        graph.addNode("B" );
        graph.addNode("C" );
        graph.addNode("D" );
        graph.addNode("E" );
        graph.addNode("F" );
        graph.addEdge("AB", "A", "B" ,false);
        graph.addEdge("BC", "B", "C" ,false);
        graph.addEdge("CA", "C", "A" ,false);
        graph.addEdge("CD", "C", "D" ,false);
        graph.addEdge("DE", "D", "E" ,false);
        graph.addEdge("DF", "D", "F" ,false);
    }

    private void createGraph2 (String graphType){
        graph.clear();
        Generator gen;
        int depth;
        switch (graphType)
        {
            case "Tutorial":
                createGraph1();
                return;
            case "Lobster":
                gen = new LobsterGenerator();
                depth = 50;
                break;
            case "Random":
                default:
                gen = new RandomGenerator(4, false, false);
                depth = 3;
                break;

        }
        gen.addSink(graph);
        gen.begin();
        for(int i=0; i<depth; i++)
            gen.nextEvents();
        gen.end();
    }
}
