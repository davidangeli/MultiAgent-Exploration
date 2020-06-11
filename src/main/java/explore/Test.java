package main.java.explore;

import main.java.explore.algorithm.Algorithm;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.ui.view.Viewer;

import java.util.ArrayList;

public class Test {
    private final Graph graph;
    private final ArrayList<Agent> agents;
    private Node startNode;
    private boolean paused = true;
    private int ticks=0;
    private Viewer viewer;
    private Thread t;
    private Gui gui;
    private final static int STARTNODEINDEX = 0;

    public Test(Algorithm algorithm, Graph graph, ArrayList<Agent> agents) {
        this.graph = graph;
        this.agents = agents;
    }

}
