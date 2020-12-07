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
    public final static String STEP_TOTAL_LABEL = "Steps total: ";
    private JButton btnStartStop;
    private JComboBox<GraphType> cmbGraphType;
    private JComboBox<String> cmbAlgorithm;
    private JComboBox<Integer> cmbNumberOfAgents;

    public Gui (TestCase testCase) {
        this.setTitle("Multi Agent Graph Exploration");
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setSize(900, 800);
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
        Dimension labelSize = new Dimension(150,22);
        Dimension comboSize = new Dimension(100,22);

        //graph type
        JLabel lblGraphType = new JLabel("New graph:");
        setComponentSize(lblGraphType, labelSize, true);
        cmbGraphType = new JComboBox<>(GraphType.values());
        setComponentSize(cmbGraphType, comboSize, false);
        //algorithm
        JLabel lblAlgorithm = new JLabel("Algorithm:");
        setComponentSize(lblAlgorithm, labelSize, true);
        cmbAlgorithm = new JComboBox<>(new String[] {
                TestManager.MULTIAGENTDFSCODE,
                TestManager.MULTIAGENTDDFSCODE,
                TestManager.MULTIAGENTEDDFSCODE,
                TestManager.ROTORROUTERCODE
        });
        setComponentSize(cmbAlgorithm, comboSize, false);
        //number of agents
        JLabel lblNumberOfAgents = new JLabel("Agents:");
        setComponentSize(lblNumberOfAgents, labelSize, true);
        cmbNumberOfAgents = new JComboBox<>(new Integer[] {1, 2, 3});
        setComponentSize(cmbNumberOfAgents, comboSize, false);
        //restart button
        JButton btnRestart = new JButton("Reset");
        setComponentSize(btnRestart, comboSize, false);
        //next
        JButton btnNextStep = new JButton("Next step");
        setComponentSize(btnNextStep, comboSize, false);
        //start-stop
        btnStartStop = new JButton("Start / stop");
        setComponentSize(btnStartStop, comboSize, false);

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

        JLabel stepCountLabel = new JLabel(STEP_COUNT_LABEL);
        setComponentSize(stepCountLabel, comboSize, false);
        testCase.setStepCountLabel(stepCountLabel);

        //controlPanel2.add(Box.createRigidArea(new Dimension(5, 0)));
        // controlPanel1.setBorder(new EmptyBorder(10, 0, 0, 0));
        JPanel controlPanelA = new JPanel();
        controlPanelA.setLayout(new BoxLayout(controlPanelA, BoxLayout.Y_AXIS));
        JPanel controlPanelA1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel controlPanelA2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel controlPanelA3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel controlPanelA4 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanelA4.setBorder(new EmptyBorder(0, 15, 0, 5));
        controlPanelA1.add(lblGraphType);
        controlPanelA1.add(cmbGraphType);
        controlPanelA2.add(lblAlgorithm);
        controlPanelA2.add(cmbAlgorithm);
        controlPanelA3.add(lblNumberOfAgents);
        controlPanelA3.add(cmbNumberOfAgents);
        controlPanelA4.add(btnRestart);
        controlPanelA.add(controlPanelA1);
        controlPanelA.add(controlPanelA2);
        controlPanelA.add(controlPanelA3);
        controlPanelA.add(controlPanelA4);

        JPanel controlPanelB = new JPanel();
        controlPanelB.setLayout(new BoxLayout(controlPanelB, BoxLayout.Y_AXIS));
        JPanel controlPanelB1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel controlPanelB2 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JPanel controlPanelB3 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        controlPanelB1.add(stepCountLabel);
        controlPanelB2.add(btnNextStep);
        controlPanelB3.add(btnStartStop);
        controlPanelB.add(controlPanelB1);
        controlPanelB.add(controlPanelB2);
        controlPanelB.add(controlPanelB3);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.X_AXIS));
        controlPanel.add(controlPanelA);
        controlPanel.add(controlPanelB);
        splitPane.setLeftComponent(controlPanel);
    }

    private void setComponentSize(JComponent component, Dimension dimension, boolean bordersToo){
        component.setMinimumSize(dimension);
        component.setPreferredSize(dimension);
        component.setMaximumSize(dimension);
        if (bordersToo) {
            component.setBorder(new EmptyBorder(0, 15, 0, 5));
        }
        if (component.getClass() == JButton.class) {
            component.setFont(new Font("Serif", Font.PLAIN, 14));
        }
        else {
            component.setFont(new Font("Serif", Font.PLAIN, 16));
        }
    }
}
