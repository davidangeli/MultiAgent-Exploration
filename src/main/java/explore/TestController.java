package main.java.explore;

import main.java.explore.algorithm.Algorithm;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;
import javax.swing.JLabel;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class TestController implements Runnable {
    private final static String STEPCOUNTLABEL = "Step count: ";
    private final Graph graph = new SingleGraph("MultiAgent");
    private final ArrayList<Agent> agents = new ArrayList<>();
    private final Algorithm algorithm;
    private boolean paused = true;
    private Viewer viewer;
    private Thread thread = new Thread(this);
    public final JLabel stepCount = new JLabel(STEPCOUNTLABEL);

    public AtomicBoolean stopped = new AtomicBoolean(true);

    public TestController(int agentNum, String graphType, Algorithm algorithm) {
        this.algorithm = algorithm;
        init(graphType, agentNum);
    }

    public synchronized void reset(int agentNum) {
        //if (!isFinished() || stopped.get()) return;
        paused = true;
        agents.clear();
        algorithm.init(graph, agents, agentNum);
        stepCount.setText(STEPCOUNTLABEL);
    }

    public synchronized void init(String graphType, int r) {
        //if (!isFinished() || stopped.get()) return;
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
        int ticks = 0;

        //loop
        while (!stopped.get()){

            if (!paused) {
                tick();
                stepCount.setText(STEPCOUNTLABEL + (++ticks));
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                //TODO log
            }
        }
        stopped.set(true);
        System.out.println("Graph explored! " + stepCount.getText());
    }

    private synchronized void tick () {
        AtomicBoolean allDone = new AtomicBoolean(true);

        //move agents
        agents.forEach(a -> {
            if (!algorithm.agentStops(graph, a)) {
                allDone.set(false);
                a.move();
            }
        });
        stopped.set(allDone.get());
    }

    public ViewPanel getNewViewPanel() {
        if (viewer != null) viewer.close();
        viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
        viewer.enableAutoLayout();
        return viewer.addDefaultView(false);
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
