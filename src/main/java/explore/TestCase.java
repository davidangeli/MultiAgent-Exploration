package main.java.explore;

import main.java.explore.algorithm.Algorithm;
import main.java.explore.graph.GraphManager;
import main.java.explore.graph.GraphType;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

public class TestCase implements Callable<Integer> {
    protected static int idc;
    private final int id;
    private final Graph graph;
    private final ArrayList<Agent> agents = new ArrayList<>();
    private final Algorithm algorithm;
    private boolean paused = true;
    private Thread thread = new Thread();
    public final JLabel stepCount = new JLabel("Step count: 0");
    private int statistics;

    private final boolean runsInGui;

    public AtomicBoolean stopped = new AtomicBoolean(true);

    public TestCase(GraphType graphType, int graphSize, int graphDegree, Algorithm algorithm, int agentNum, boolean runsInGui) {
        this(GraphManager.getGraph(graphType, graphSize, graphDegree), algorithm, agentNum, runsInGui);
    }

    public TestCase(Graph graph, Algorithm algorithm, int agentNum, boolean runsInGui) {
        this.id = ++idc;
        this.graph = graph;
        this.algorithm = algorithm;
        this.runsInGui = runsInGui;
        init(agentNum, true);
    }

    //should only be called from gui
    public synchronized void init(GraphType graphType, int agentNum) {
        graph.setAttribute(GraphManager.GRAPH_TYPE_LABEL, graphType);
        init(agentNum, true);
    }

    public synchronized void init(int agentNum, boolean resetGraph) {
        if (resetGraph) {
            GraphManager.resetGraph(graph);
        }
        paused = runsInGui;
        algorithm.init(graph, agents, agentNum);
        statistics = 0;

        if (runsInGui) {
            algorithm.createLabels(graph);
            algorithm.updateLabels(agents);
            showStepCount(true);
        }
    }

    public boolean isRunning() {
        return thread.isAlive();
    }

    public synchronized void start() {
        if (!stopped.get()) return;

        if (runsInGui) {
            thread = new Thread(() -> {
                try {
                    this.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            thread.start();
        }
    }

    @Override
    public Integer call() throws Exception {
        stopped.set(false);

        //loop
        while (!stopped.get()) {
            if (!paused) {
                tick();
            }
            //InterruptedException is propagated
            //TODO: put into loop
            if (runsInGui) {
                Thread.sleep(1000);
            }
        }
        if (runsInGui) {
            System.out.println("Algorithm finished exploration in " + statistics + " steps");
        }
        return statistics;
    }

    private synchronized void tick () {
        AtomicBoolean allDone = new AtomicBoolean(true);
        statistics++;

        //get next step or stop
        //this has to be done in a different cycle from the move-evaluation
        HashMap<Agent, Edge> agentNextStep = new HashMap<>();
        agents.stream().filter(Agent::isRunning).forEach(a -> {
            if (algorithm.agentStops(graph, a)) {
                a.stop();
            } else {
                agentNextStep.put(a, algorithm.selectNextStep(a));
                allDone.set(false);
            }
        });

        //move agents
        agents.stream().filter(Agent::isRunning).forEach(a -> {
            Edge moveOn = agentNextStep.get(a);
            a.move(moveOn);
            algorithm.evaluateOnArrival(a, moveOn);
        });

        //update labels if
        if (runsInGui) {
            algorithm.updateLabels(agents);
            showStepCount(false);
        }

        //check finished state
        if (allDone.get()) {
            stopped.set(true);
            //System.out.println("Graph explored! " + statistics);
        }
    }

    public Graph getGraph () {
        return graph;
    }

    //TODO: think this over again, esp if we'll have a counter
    private void showStepCount(boolean reset) {
        if (!runsInGui) { return; }

        String oldI = stepCount.getText().split(" ")[2];
        //reset
        int newI = 0;
        //update
        if (!reset) {
            newI = Integer.parseInt(oldI) + 1;
        }
        stepCount.setText(stepCount.getText().replace(oldI, Integer.toString(newI)));
    }

    public synchronized void pause() {
        paused = !paused;
    }

    public synchronized void stop() {
        stopped.set(true);
    }

    public synchronized void tickOne() {
        if (paused) tick();
    }

    @Override
    public String toString() {
        String result = "TestCase" + id + ": ";
        result += algorithm.getName() + "(" + agents.size() + ")";
        result += graph.getAttribute(GraphManager.GRAPH_TYPE_LABEL);
        result += "(v:" +  graph.getNodeCount() + ",e:" + graph.getEdgeCount() + ")";
        return result;
    }
}
