package main.java.explore;

import main.java.explore.graph.GraphType;
import org.graphstream.ui.view.Viewer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
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
    private JComboBox<Integer> cmbNumberOfAgents;

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
        cmbNumberOfAgents.setSelectedItem(Main.GUI_AGENTNUM);
        cmbAlgorithm.setSelectedItem(Main.GUI_ALGORITHM);
        this.testCase.init((GraphType) cmbGraphType.getSelectedItem(),
                TestManager.selectAlgorithm((String) Objects.requireNonNull(cmbAlgorithm.getSelectedItem())),
                (int)cmbNumberOfAgents.getSelectedItem(), true);

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
        Dimension labelSize = new Dimension(100,20);
        Dimension comboSize = new Dimension(100,20);

        //graph type
        JLabel lblGraphType = new JLabel("New graph:");
        setComponentSize(lblGraphType, labelSize, true);
        cmbGraphType = new JComboBox<>(GraphType.values());
        setComponentSize(cmbGraphType, comboSize, false);
        //algorithm
        JLabel lblAlgorithm = new JLabel("Algorithm:");
        setComponentSize(lblAlgorithm, labelSize, true);
        cmbAlgorithm = new JComboBox<>(new String[] {TestManager.MULTIAGENTDFSCODE, TestManager.ROTORROUTERCODE});
        setComponentSize(cmbAlgorithm, comboSize, false);
        //number of agents
        JLabel lblNumberOfAgents = new JLabel("Agents:");
        setComponentSize(lblNumberOfAgents, labelSize, true);
        cmbNumberOfAgents = new JComboBox<>(new Integer[] {1, 2, 3});
        setComponentSize(cmbNumberOfAgents, comboSize, false);
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
                    (int)cmbNumberOfAgents.getSelectedItem(), true);
            setNewGraphViewPanel();
        });
        btnRestart.addActionListener(e -> testCase.init((GraphType) cmbGraphType.getSelectedItem(),
                    TestManager.selectAlgorithm((String) Objects.requireNonNull(cmbAlgorithm.getSelectedItem())),
                    (int)cmbNumberOfAgents.getSelectedItem(), false));
        btnNextStep.addActionListener(e -> testCase.tickOne());
        btnStartStop.addActionListener(e -> testCase.start());

        JPanel controlPanel = new JPanel();
        JPanel controlPanel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel1.setBorder(new EmptyBorder(10, 0, 0, 0));
        JPanel controlPanel2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel controlPanel3 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JPanel controlPanel4 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        controlPanel1.add(lblGraphType);
        controlPanel1.add(cmbGraphType);
        controlPanel2.add(lblAlgorithm);
        controlPanel2.add(cmbAlgorithm);
        controlPanel2.add(lblNumberOfAgents);
        controlPanel2.add(cmbNumberOfAgents);
        controlPanel2.add(Box.createRigidArea(new Dimension(5, 0)));
        controlPanel2.add(btnRestart);
        controlPanel4.add(btnNextStep);
        controlPanel4.add(Box.createRigidArea(new Dimension(5, 0)));
        controlPanel4.add(btnStartStop);

        JLabel stepCountLabel = new JLabel(STEP_COUNT_LABEL);
        setComponentSize(stepCountLabel, new Dimension(200,20), false);
        controlPanel3.add(stepCountLabel);
        testCase.setStepCountLabel(stepCountLabel);

        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.add(controlPanel1);
        controlPanel.add(controlPanel2);
        controlPanel.add(controlPanel3);
        controlPanel.add(controlPanel4);
        splitPane.setLeftComponent(controlPanel);
    }

    private void setComponentSize(JComponent component, Dimension dimension, boolean bordersToo){
        component.setMinimumSize(dimension);
        component.setPreferredSize(dimension);
        component.setMaximumSize(dimension);
        if (bordersToo) {
            component.setBorder(new EmptyBorder(0, 15, 0, 5));
        }
    }
}
