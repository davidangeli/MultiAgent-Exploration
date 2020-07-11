package main.java.explore;

import main.java.explore.algorithm.Algorithm;
import main.java.explore.graph.GraphManager;
import main.java.explore.graph.GraphType;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

public class TestCase implements Callable<int[]> {
    protected static int idc;
    private final int id;
    private final Graph graph;
    private final ArrayList<Agent> agents = new ArrayList<>();
    private final Algorithm algorithm;
    private boolean paused = true;
    private Thread thread = new Thread();
    public final JLabel stepCountLabel = new JLabel();
    private final int repeats;
    private final boolean runsInGui;
    private int agentNum, stepCount;
    private final static String STEP_COUNT_LABEL = "Step count: ";

    public AtomicBoolean stopped = new AtomicBoolean(true);

    public TestCase(Graph graph, Algorithm algorithm, int agentNum, boolean runsInGui, int repeats) {
        this.id = ++idc;
        this.graph = graph;
        this.algorithm = algorithm;
        this.runsInGui = runsInGui;
        this.repeats = repeats;
        this.agentNum = agentNum;
    }

    //should only be called from gui
    public synchronized void init(GraphType graphType, int agentNum, boolean resetGraph) {
        assert(runsInGui);
        graph.setAttribute(GraphManager.GRAPH_TYPE_LABEL, graphType);
        this.agentNum = agentNum;
        init(resetGraph);
    }

    public synchronized void init(boolean resetGraph) {
        if (resetGraph) {
            GraphManager.resetGraph(graph);
        }
        paused = runsInGui;
        algorithm.init(graph, agents, agentNum);
        stepCount = 0;
        stopped.set(false);

        if (runsInGui) {
            algorithm.createLabels(graph);
            algorithm.updateLabels(agents);
            showStepCount();
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
    public int[] call() throws Exception {
        LinkedList<Integer> results = new LinkedList<>();

        for (int i = 0; i < repeats; i++) {
            init(true);

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
            results.add(stepCount);
        }

        if (runsInGui) {
           stepCountLabel.setText("Done in " + stepCount + " steps.");
        }
        return getStatistics(results);
    }

    private synchronized void tick () {
        AtomicBoolean allDone = new AtomicBoolean(true);
        stepCount++;

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
            showStepCount();
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

    private int[] getStatistics(LinkedList<Integer> results) {
        int[] stats = new int[3];

        stats[0] = Collections.min(results);
        stats[1] = Collections.max(results);
        stats[2] = (int)(((double)results.stream().reduce(0, Integer::sum)) / ((double) results.size()));
        return stats;
    }

    private void showStepCount() {
        assert (runsInGui);
        stepCountLabel.setText(STEP_COUNT_LABEL + stepCount);
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
        return "TestCase" + id + ";" +
                algorithm.getName() + ";" +
                agents.size() + ";" +
                graph.getAttribute(GraphManager.GRAPH_TYPE_LABEL) + ";" +
                graph.getNodeCount() + ";" +
                graph.getEdgeCount() + ";" +
                repeats;
    }
}
