package main.java.explore;

import main.java.explore.algorithm.Algorithm;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TestController implements Runnable {
    private final Graph graph = new SingleGraph("MultiAgent");
    private final ArrayList<Agent> agents = new ArrayList<>();
    private final Algorithm algorithm;
    private boolean paused = true;
    private Thread thread = new Thread(this);
    public final JLabel stepCount = new JLabel("Step count: 0");
    private static final Logger logger = Logger.getLogger(TestController.class.getName());
    private final boolean runsInGui;

    public AtomicBoolean stopped = new AtomicBoolean(true);

    public TestController(int agentNum, String graphType, Algorithm algorithm, boolean runsInGui) {
        logger.setUseParentHandlers(true);
        this.algorithm = algorithm;
        this.runsInGui = runsInGui;
        init(graphType, agentNum);
    }

    public synchronized void reset(int agentNum) {
        paused = true;
        algorithm.init(graph, agents, agentNum);

        if (runsInGui) {
            algorithm.createLabels(graph);
            algorithm.updateLabels(agents);
            showStepCount(true);
        }
    }

    public synchronized void init(String graphType, int r) {
        GraphManager.createGraph(graph, graphType);
        reset(r);
    }

    public boolean isRunning() {
        return thread.isAlive();
    }

    public synchronized void start() {
        if (!stopped.get()) return;
        thread = new Thread(this);
        stopped.set(false);
        thread.start();
    }

    @Override
    public void run() {

        //loop
        while (!stopped.get()) {
            if (!paused) {
                tick();
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                //TODO log
            }
        }
        logger.log(Level.INFO, "Run finished.");
    }

    private synchronized void tick () {
        AtomicBoolean allDone = new AtomicBoolean(true);

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
            System.out.println("Graph explored! " + stepCount.getText());
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
}
