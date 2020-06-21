package main.java.explore;

import main.java.explore.algorithm.Algorithm;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

public class TestCase implements Callable<Integer> {
    private final Graph graph = new SingleGraph("MultiAgent");
    private final ArrayList<Agent> agents = new ArrayList<>();
    private final Algorithm algorithm;
    private boolean paused = true;
    private Thread thread = new Thread();
    public final JLabel stepCount = new JLabel("Step count: 0");
    private int statistics;

    private final boolean runsInGui;

    public AtomicBoolean stopped = new AtomicBoolean(true);

    public TestCase(String graphType, Algorithm algorithm, int agentNum, boolean runsInGui) {
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
        thread = new Thread(()->{
            try {
                this.call();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        stopped.set(false);
        thread.start();
    }

    @Override
    public Integer call() throws Exception {

        //loop
        while (!stopped.get()) {
            if (!paused) {
                tick();
            }
            //InterruptedException is propagated
            Thread.sleep(1000);
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
