package main.java.explore;

import main.java.explore.algorithm.Algorithm;
import main.java.explore.algorithm.MultiRobotDFS;
import main.java.explore.graph.GraphManager;
import main.java.explore.graph.GraphType;
import org.graphstream.ui.view.Viewer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Gui extends JFrame {
    private final TestCase testCase;
    private final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    public final static String STEP_COUNT_LABEL = "Step count: ";
    private final static Algorithm GUI_MRDFS = new MultiRobotDFS();

    public Gui (TestCase testCase) {
        this.setTitle("Multi Agent Graph Exploration");
        this.testCase = testCase;

        setControlPanel();
        setNewGraphViewPanel();

        //close event
        this.addWindowListener(
                new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        testCase.stop();
                    }
        });

        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setSize(800, 800);
        this.setLocationRelativeTo(null);

        this.add(splitPane, BorderLayout.CENTER);
    }
    
    private void setNewGraphViewPanel () {
        Viewer viewer = new Viewer(testCase.getGraph(), Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
        viewer.enableAutoLayout();
        splitPane.setRightComponent(viewer.addDefaultView(false));
    }
    
    private void setControlPanel () {
        //number of robots
        JLabel lblNumberOfRobots = new JLabel("Number of robots");
        JComboBox<Integer> txtNumberOfRobots = new JComboBox<>(new Integer[] {1, 2, 3});
        txtNumberOfRobots.setSelectedIndex(1);
        //generator type
        JLabel lblGeneratorType = new JLabel("Generator type");
        JComboBox<GraphType> cmbGeneratorType = new JComboBox<>(GraphType.values());
        cmbGeneratorType.setSelectedItem(GraphType.TUTORIAL);
        //restart button
        JButton btnRestart = new JButton("Restart");

        //ActionListeners
        btnRestart.addActionListener(e -> {
            testCase.init(testCase.getGraph().getAttribute(GraphManager.GRAPH_TYPE_LABEL), GUI_MRDFS, (int)txtNumberOfRobots.getSelectedItem(), false);
        });
        cmbGeneratorType.addActionListener(e -> {
            testCase.init((GraphType) cmbGeneratorType.getSelectedItem(), GUI_MRDFS, (int)txtNumberOfRobots.getSelectedItem(), true);
            setNewGraphViewPanel();
        });
        //next
        JButton btnNextStep = new JButton("Next step");
        btnNextStep.addActionListener(e -> testCase.tickOne());
        //start-stop
        JButton btnPause = new JButton("Start / stop");
        btnPause.addActionListener(e -> testCase.start());

        JPanel controlPanel = new JPanel();
        controlPanel.add(lblNumberOfRobots);
        controlPanel.add(txtNumberOfRobots);
        controlPanel.add(btnRestart);
        controlPanel.add(lblGeneratorType);
        controlPanel.add(cmbGeneratorType);
        controlPanel.add(btnNextStep);
        controlPanel.add(btnPause);

        JLabel stepCountLabel = new JLabel(STEP_COUNT_LABEL);
        Dimension dimension = new Dimension(120, 80);
        stepCountLabel.setMinimumSize(dimension);
        stepCountLabel.setPreferredSize(dimension);
        stepCountLabel.setMaximumSize(dimension);
        controlPanel.add(stepCountLabel);
        testCase.setStepCountLabel(stepCountLabel);

        splitPane.setLeftComponent(controlPanel);
    }
}
