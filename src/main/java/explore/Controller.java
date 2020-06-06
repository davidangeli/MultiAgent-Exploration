package main.java.explore;

import org.graphstream.algorithm.generator.Generator;
import org.graphstream.algorithm.generator.LobsterGenerator;
import org.graphstream.algorithm.generator.RandomGenerator;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class Controller implements Runnable {
    private Graph graph;
    private ArrayList<Robot> robots;
    private Node startNode;
    private boolean paused = true;
    private int ticks=0;
    private Viewer viewer;
    private Thread t;
    private Gui gui;

    public AtomicBoolean stopped = new AtomicBoolean(false);

    public Controller(int r, String graphType){
        graph = new SingleGraph("MultiRobot");
        robots = new ArrayList<>();

        //graph-robot setup. should contain at least one node
        init(graphType, r);
    }

    public synchronized void reset(int r) {
        //if (!isFinished() || stopped.get()) return;
        paused = true;
        setGraph(0);
        robots.clear();
        for (int i =0; i < r; i++) {
            robots.add(new Robot(startNode));
        }
        graph.getNodeSet().forEach(n -> n.removeAttribute("ui.label"));
        setLabel(startNode);
        ticks=0;
    }

    public synchronized void init(String graphType, int r){
        paused = true;
        //if (!isFinished() || stopped.get()) return;
        createGraph2(graphType);
        reset(r);
    }
    public boolean isRunning(){
        if (t == null) return false;
        return t.isAlive();
    }
    public void start(){
        this.stopped.set(false);
        t = new Thread(this);
        t.start();
    }

    public void setGui(Gui g){
        this.gui=g;
    }

    @Override
    public void run(){

        //startnode label & display graph
        setLabel(startNode);

        //loop
        while (!isFinished() && !stopped.get()){

            if (!paused) tick();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
            }
        }
        stopped.set(true);
        System.out.println("Graph explored in " + ticks + " steps");
    }

    private synchronized void tick () {
        ticks++;
        //remove labels
        graph.getNodeSet().forEach(n -> n.removeAttribute("ui.label"));

        //nodes with robots
        Set<Node> currentNodes = robots.stream()
                .map(Robot::getCurrentNode)
                .collect(Collectors.toSet());

        //robots choose new edge to move on
        boolean graphdone=edgesFinished();
        robots.stream()
                .filter(r -> !graphdone || (r.getCurrentNode() != startNode))
                .forEach(Robot::chooseDirection);

        //moves - based on unfinished visits with valid to edge
        currentNodes.forEach(n -> {
            ArrayList<Visit> chalks = (ArrayList<Visit>) n.getAttribute("chalks");
            chalks.stream()
                    .filter(v -> v.isNotFinished() && v.getTo() != null)
                    .forEach(v -> {
                        v.robot.moveForward();
                        v.finish();
                    });
        });

        //labels
        robots.stream()
                .map(Robot::getCurrentNode)
                .distinct()
                .forEach(n -> setLabel(n));

        gui.setSteps(ticks);
    };

    private boolean isFinished(){
        boolean robotsFinished = robots.stream().allMatch(r -> r.getCurrentNode() == startNode);
        return  robotsFinished && edgesFinished();
    }

    private boolean edgesFinished () {

        boolean b =graph.getEdgeSet().stream().allMatch(e -> e.hasAttribute("state") && e.getAttribute("state") == EdgeState.FINISHED);

        return b;
    }

    private void setLabel(Node node){
        String label = robots.stream()
                .filter(r -> r.getCurrentNode().equals(node))
                .map(Robot::toString)
                .collect(Collectors.joining(","));
        label = node.getId() + ": " + label;
        node.setAttribute("ui.style", "text-size: 12px;");

        if (! paused){
            node.setAttribute("ui.label", label);
            return;
        }

        //extended labeling
        ArrayList<Visit> chalks = node.getAttribute("chalks");
        String chalklabel = chalks.stream()
                .map(Visit::toString)
                .collect(Collectors.joining(System.getProperty("line.separator")));

        node.setAttribute("ui.label", label + " " + System.lineSeparator() + chalks);

        node.getEdgeSet().forEach(e -> {
            Node n = e.getOpposite(node);
            if (!n.hasAttribute("ui.label")) n.setAttribute("ui.label", n.getId());
        });
    }

    public ViewPanel getViewPanel(){
        if (viewer != null) viewer.close();
        viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
        viewer.enableAutoLayout();
        ViewPanel view = viewer.addDefaultView(false);
        return view;
    }

    public synchronized void pause (){
        paused = !paused;
        if (paused) {
            //labels
            robots.stream()
                    .map(Robot::getCurrentNode)
                    .distinct()
                    .forEach(n -> setLabel(n));
        }
    }

    public synchronized void tickOne (){
        if (paused) tick();
    }

    private void setGraph (int startNodeIndex){

        startNode = graph.getNode(startNodeIndex);
        startNode.addAttribute("ui.style", "size: 20;");

        //add chalks
        //graph.getNodeSet().stream().forEach(n -> n.removeAttribute("chalks"));
        graph.getNodeSet().stream().forEach(n -> n.addAttribute("chalks", new ArrayList<Visit>()));
        //set edges to gray
        graph.getEdgeSet().forEach(e -> EdgeState.UNVISITED.setEdge(e) );
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
        int depth =0;
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
