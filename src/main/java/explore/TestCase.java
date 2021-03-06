package main.java.explore;

import main.java.explore.algorithm.Algorithm;
import main.java.explore.graph.EdgeState;
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
import java.util.logging.Level;
import java.util.logging.Logger;

public class TestCase implements Callable<int[]> {
    protected static int idc;
    private final int id;
    private final Graph graph;
    private final ArrayList<Agent> agents = new ArrayList<>();
    private Algorithm<?, ?> algorithm;
    private boolean paused = true;
    private Thread thread = new Thread();
    private JLabel stepCountLabel;
    private final int repeats;
    private boolean runsInGui = false;
    private int agentNum, stepCount;

    private static final Logger logger = Logger.getLogger(TestCase.class.getName());

    private final AtomicBoolean stopped = new AtomicBoolean(true);

    public TestCase(Graph graph, Algorithm<?, ?> algorithm, int agentNum, int repeats) {
        logger.setUseParentHandlers(true);
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
     * @param regenerateGraph Boolean setting if the graph should be reset. In case of new graph type, the graph will be reset anyways.
     */
    public synchronized void init(GraphType graphType, Algorithm<?, ?> algorithm, int agentNum, boolean regenerateGraph) {
        assert(runsInGui);
        assert(stopped.get() || paused);
        GraphType oldGrapType = graph.getAttribute(GraphManager.GRAPH_TYPE_LABEL);
        graph.setAttribute(GraphManager.GRAPH_TYPE_LABEL, graphType);
        this.algorithm = algorithm;
        this.agentNum = agentNum;
        reset(regenerateGraph || oldGrapType != graphType);
    }

    /**
     * Resets the test case.
     * @param regenerateGraph Set true if the graph should be renewed as well.
     */
    private synchronized void reset(boolean regenerateGraph) {
        if (regenerateGraph) {
            GraphManager.regenerateGraph(graph);
        }
        paused = runsInGui;
        try {
            algorithm.init(graph, agents, agentNum);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "TestCase" + id + " resetting: algorithm init failed.");
            e.printStackTrace();
        }
        stepCount = 0;

        if (runsInGui) {
            algorithm.createLabels(graph);
            algorithm.updateLabels(graph, agents);
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
        logger.log(Level.INFO, "TestCase" + id + " run started.");
        LinkedList<Integer> results = new LinkedList<>();
        boolean explorationCheck = true;

        for (int i = 0; i < repeats; i++) {
            stopped.set(false);
            if (!runsInGui) {
                reset(true);
            }

            //loop
            while (!stopped.get()) {
                if (Thread.currentThread().isInterrupted()) {
                    i = repeats;
                    stopped.set(true);
                }
                else if (!paused) {
                    tick();
                }
                //InterruptedException is propagated
                //TODO: put into loop
                if (runsInGui) {
                    Thread.sleep(1000);
                }
            }
            explorationCheck = explorationCheck &&
                    graph.getEdgeSet().stream().allMatch(e -> e.getAttribute(Algorithm.EDGESTATEID) != EdgeState.UNVISITED);
            results.add(stepCount);
        }

        if (runsInGui) {
            System.out.println("Testcase run done.");
            showStepCount();
        }

        logger.log(Level.INFO, "TestCase" + id + (Thread.currentThread().isInterrupted() ? " interrupted." : " done."));
        return getStatistics(explorationCheck, results);
    }

    private synchronized void tick () {
        AtomicBoolean allDone = new AtomicBoolean(true);
        stepCount++;

        //get next step or stop
        //this has to be done in a different cycle from the move-evaluation
        HashMap<Agent, Edge> agentNextStep = new HashMap<>();
        agents.stream().filter(Agent::isRunning).forEach(a -> {
            if (algorithm.agentStops(graph, agents, a)) {
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

        //check finished state
        if (allDone.get()) {
            stopped.set(true);
            //System.out.println("Graph explored! " + statistics);
        }

        //update labels if
        if (runsInGui) {
            algorithm.updateLabels(graph, agents);
            showStepCount();
        }
    }

    public Graph getGraph () {
        return graph;
    }

    private int[] getStatistics(boolean explorationCheck, LinkedList<Integer> results) {
        int[] stats = new int[5];

        stats[0] = explorationCheck ? 1 : 0;
        stats[1] = Collections.min(results);
        stats[2] = Collections.max(results);
        stats[3] = (int)(((double)results.stream().reduce(0, Integer::sum)) / ((double) results.size()));
        stats[4] = (int)Math.sqrt(results.stream().map(i -> Math.pow(i-stats[3],2)).reduce(0.0, Double::sum) / ((double) results.size()));
        return stats;
    }

    private void showStepCount() {
        assert (runsInGui);
        stepCountLabel.setText((stopped.get() ? Gui.STEP_TOTAL_LABEL : Gui.STEP_COUNT_LABEL) + stepCount);
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
                graph.getAttribute(GraphManager.GRAPH_SIZE_LABEL) + ";" +
                graph.getNodeCount() + ";" +
                graph.getAttribute(GraphManager.GRAPH_DEGREE_LABEL) + ";" +
                graph.getEdgeCount() + ";" +
                repeats;
    }
}
