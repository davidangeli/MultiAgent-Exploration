package main.java.explore;

import main.java.explore.graph.GraphManager;
import main.java.explore.graph.GraphType;
import org.graphstream.ui.view.Viewer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Objects;

public class Gui extends JFrame {
    private final TestCase testCase;
    private final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    public final static String STEP_COUNT_LABEL = "Step count: ";
    private JButton btnStartStop;

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

        //setting focus to start/stop button
        this.addWindowFocusListener(new WindowAdapter() {
            public void windowGainedFocus(WindowEvent e) {
                btnStartStop.requestFocusInWindow();
            }
        });
    }
    
    private void setNewGraphViewPanel () {
        Viewer viewer = new Viewer(testCase.getGraph(), Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
        viewer.enableAutoLayout();
        splitPane.setRightComponent(viewer.addDefaultView(false));
    }
    
    private void setControlPanel () {
        //generator type
        JLabel lblGeneratorType = new JLabel("New graph:");
        JComboBox<GraphType> cmbGeneratorType = new JComboBox<>(GraphType.values());
        cmbGeneratorType.setSelectedItem(GraphType.TUTORIAL);
        //algorithm
        JLabel lblAlgorithm = new JLabel("Algorithm:");
        JComboBox<String> cmbAlgorithm = new JComboBox<>(new String[] {"mrdfs", "rr"});
        cmbAlgorithm.setSelectedIndex(0);
        //number of robots
        JLabel lblNumberOfRobots = new JLabel("Number of robots:");
        JComboBox<Integer> txtNumberOfRobots = new JComboBox<>(new Integer[] {1, 2, 3});
        txtNumberOfRobots.setSelectedIndex(1);
        //restart button
        JButton btnRestart = new JButton("Restart");
        //next
        JButton btnNextStep = new JButton("Next step");
        //start-stop
        btnStartStop = new JButton("Start / stop");

        //ActionListeners
        cmbGeneratorType.addActionListener(e -> {
            testCase.init((GraphType) cmbGeneratorType.getSelectedItem(),
                    TestManager.selectAlgorithm((String) Objects.requireNonNull(cmbAlgorithm.getSelectedItem())),
                    (int)txtNumberOfRobots.getSelectedItem(), true);
            setNewGraphViewPanel();
        });
        btnRestart.addActionListener(e -> {
            testCase.init(testCase.getGraph().getAttribute(GraphManager.GRAPH_TYPE_LABEL),
                    TestManager.selectAlgorithm((String) Objects.requireNonNull(cmbAlgorithm.getSelectedItem())),
                    (int)txtNumberOfRobots.getSelectedItem(), false);
            setNewGraphViewPanel();
        });
        btnNextStep.addActionListener(e -> testCase.tickOne());
        btnStartStop.addActionListener(e -> testCase.start());

        JPanel controlPanel = new JPanel();
        JPanel controlPanel1 = new JPanel();
        JPanel controlPanel2 = new JPanel();
        JPanel controlPanel3 = new JPanel();
        controlPanel1.add(lblGeneratorType);
        controlPanel1.add(cmbGeneratorType);
        controlPanel2.add(lblAlgorithm);
        controlPanel2.add(cmbAlgorithm);
        controlPanel2.add(lblNumberOfRobots);
        controlPanel2.add(txtNumberOfRobots);
        controlPanel2.add(btnRestart);
        controlPanel3.add(btnNextStep);
        controlPanel3.add(btnStartStop);

        JLabel stepCountLabel = new JLabel(STEP_COUNT_LABEL);
        Dimension dimension = new Dimension(120, 80);
        stepCountLabel.setMinimumSize(dimension);
        stepCountLabel.setPreferredSize(dimension);
        stepCountLabel.setMaximumSize(dimension);
        controlPanel3.add(stepCountLabel);
        testCase.setStepCountLabel(stepCountLabel);

        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.add(controlPanel1);
        controlPanel.add(controlPanel2);
        controlPanel.add(controlPanel3);
        splitPane.setLeftComponent(controlPanel);
    }
}
