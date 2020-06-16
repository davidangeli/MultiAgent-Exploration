package main.java.explore;

import main.java.explore.algorithm.Algorithm;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;
import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class TestController<M, S> implements Runnable {
    private final Graph graph = new SingleGraph("MultiAgent");
    private final ArrayList<Agent> agents = new ArrayList<>();
    private final Algorithm<M, S> algorithm;
    private boolean paused = true;
    private Viewer viewer;
    private Thread thread = new Thread(this);
    public final JLabel stepCount = new JLabel();

    public AtomicBoolean stopped = new AtomicBoolean(true);

    public TestController(int agentNum, String graphType, Algorithm<M, S> algorithm) {
        this.algorithm = algorithm;
        init(graphType, agentNum);
    }

    public synchronized void reset(int agentNum) {
        paused = true;
        algorithm.init(graph, agents, agentNum);
        algorithm.createLabels(graph);
        algorithm.updateLabels(agents);
        stepCount.setText("Step count: 0");
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
        while (!stopped.get()){

            if (!paused) {
                tick();
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                //TODO log
            }
        }

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
        agents.stream().filter(Agent::isRunning).forEach(a -> a.move(agentNextStep.get(a)));

        //update labels if
        algorithm.updateLabels(agents);
        showStepCount();

        //check finished state
        if (allDone.get()) {
            stopped.set(true);
            System.out.println("Graph explored! " + stepCount.getText());
        }
    }

    public ViewPanel getNewViewPanel() {
        if (viewer != null) viewer.close();
        viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
        viewer.enableAutoLayout();
        return viewer.addDefaultView(false);
    }

    //TODO: think this over again
    private void showStepCount() {
        String t = stepCount.getText().split(" ")[2];
        int i = Integer.parseInt(t) + 1;
        stepCount.setText(stepCount.getText().replace(t, Integer.toString(i)));
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
