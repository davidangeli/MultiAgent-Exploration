package main.java.explore;

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
    private JComboBox<GraphType> cmbGraphType;
    private JComboBox<String> cmbAlgorithm;
    private JComboBox<Integer> txtNumberOfAgents;

    public Gui (TestCase testCase) {
        this.setTitle("Multi Agent Graph Exploration");
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setSize(800, 800);
        this.setLocationRelativeTo(null);
        this.testCase = testCase;

        setControlPanel();
        setNewGraphViewPanel();
        this.add(splitPane, BorderLayout.CENTER);

        cmbGraphType.setSelectedItem(Main.GUI_GRAPHTYPE);
        txtNumberOfAgents.setSelectedItem(Main.GUI_AGENTNUM);
        cmbAlgorithm.setSelectedItem(Main.GUI_ALGORITHM);
        this.testCase.init((GraphType) cmbGraphType.getSelectedItem(),
                TestManager.selectAlgorithm((String) Objects.requireNonNull(cmbAlgorithm.getSelectedItem())),
                (int)txtNumberOfAgents.getSelectedItem(), true);

        //setting focus to start/stop button
        this.addWindowFocusListener(new WindowAdapter() {
            public void windowGainedFocus(WindowEvent e) {
                btnStartStop.requestFocusInWindow();
            }
        });
        //close event
        this.addWindowListener(
                new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        testCase.stop();
                    }
                });
    }
    
    private void setNewGraphViewPanel () {
        Viewer viewer = new Viewer(testCase.getGraph(), Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
        viewer.enableAutoLayout();
        splitPane.setRightComponent(viewer.addDefaultView(false));
    }
    
    private void setControlPanel () {
        //graph type
        JLabel lblGraphType = new JLabel("New graph:");
        cmbGraphType = new JComboBox<>(GraphType.values());
        //algorithm
        JLabel lblAlgorithm = new JLabel("Algorithm:");
        cmbAlgorithm = new JComboBox<>(new String[] {TestManager.MULTIROBOTDFSCODE, TestManager.ROTORROUTERCODE});
        //number of robots
        JLabel lblNumberOfAgents = new JLabel("Number of robots:");
        txtNumberOfAgents = new JComboBox<>(new Integer[] {1, 2, 3});
        //restart button
        JButton btnRestart = new JButton("Restart");
        //next
        JButton btnNextStep = new JButton("Next step");
        //start-stop
        btnStartStop = new JButton("Start / stop");

        //ActionListeners
        cmbGraphType.addActionListener(e -> {
            testCase.init((GraphType) cmbGraphType.getSelectedItem(),
                    TestManager.selectAlgorithm((String) Objects.requireNonNull(cmbAlgorithm.getSelectedItem())),
                    (int)txtNumberOfAgents.getSelectedItem(), true);
            setNewGraphViewPanel();
        });
        btnRestart.addActionListener(e -> testCase.init((GraphType) cmbGraphType.getSelectedItem(),
                TestManager.selectAlgorithm((String) Objects.requireNonNull(cmbAlgorithm.getSelectedItem())),
                (int)txtNumberOfAgents.getSelectedItem(), false));
        btnNextStep.addActionListener(e -> testCase.tickOne());
        btnStartStop.addActionListener(e -> testCase.start());

        JPanel controlPanel = new JPanel();
        JPanel controlPanel1 = new JPanel();
        JPanel controlPanel2 = new JPanel();
        JPanel controlPanel3 = new JPanel();
        controlPanel1.add(lblGraphType);
        controlPanel1.add(cmbGraphType);
        controlPanel2.add(lblAlgorithm);
        controlPanel2.add(cmbAlgorithm);
        controlPanel2.add(lblNumberOfAgents);
        controlPanel2.add(txtNumberOfAgents);
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
