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
    private Algorithm algorithm;
    private boolean paused = true;
    private Thread thread = new Thread();
    private JLabel stepCountLabel;
    private final int repeats;
    private boolean runsInGui = false;
    private int agentNum, stepCount;

    private AtomicBoolean stopped = new AtomicBoolean(true);

    public TestCase(Graph graph, Algorithm algorithm, int agentNum, int repeats) {
        this.id = ++idc;
        this.graph = graph;
        this.algorithm = algorithm;
        this.repeats = repeats;
        this.agentNum = agentNum;
    }

    public TestCase(Graph graph) {
        this.id = ++idc;
        this.graph = graph;
        this.repeats = 1;
    }

    /**
     * This special initialization should only be called from the Gui. It enables changing agent numbers
     * and the type of the graph.
     * @param graphType Type of the graph. If changes, the graph will be reset.
     * @param algorithm Algorithm.
     * @param agentNum Number of agents.
     * @param resetGraph Boolean setting if the graph should be reset. In case of new graph type, the graph will be reset anyways.
     */
    public synchronized void init(GraphType graphType, Algorithm algorithm, int agentNum, boolean resetGraph) {
        assert runsInGui : "This method should onyl be called from Gui.";
        assert stopped.get() : "Test case seems to be running still.";
        GraphType oldGrapType = graph.getAttribute(GraphManager.GRAPH_TYPE_LABEL);
        graph.setAttribute(GraphManager.GRAPH_TYPE_LABEL, graphType);
        this.algorithm = algorithm;
        this.agentNum = agentNum;
        reset(resetGraph || oldGrapType != graphType);
    }

    /**
     * Resets the test case.
     * @param resetGraph Set true if the graph should be renewed as well.
     */
    private synchronized void reset(boolean resetGraph) {
        if (resetGraph) {
            GraphManager.resetGraph(graph);
        }
        paused = runsInGui;
        algorithm.init(graph, agents, agentNum);
        stepCount = 0;

        if (runsInGui) {
            algorithm.createLabels(graph);
            algorithm.updateLabels(agents);
            showStepCount();
        }
    }

    /**
     * This method starts or pauses/unpauses automatic execution in a Gui.
     */
    public synchronized void start() {
        assert(runsInGui);
        if (!stopped.get()) {
            pause();
        }
        else if (!thread.isAlive()) {
            paused = false;
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
        stopped.set(false);

        for (int i = 0; i < repeats; i++) {
            if (!runsInGui) {
                reset(true);
            }

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
            showStepCount();
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
        stepCountLabel.setText(Gui.STEP_COUNT_LABEL + stepCount);
    }

    public void setStepCountLabel(JLabel stepCountLabel) {
        this.stepCountLabel = stepCountLabel;
        this.runsInGui = true;
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
