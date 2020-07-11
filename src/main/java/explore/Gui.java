package main.java.explore;

import main.java.explore.graph.GraphType;
import org.graphstream.ui.view.Viewer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Gui extends JFrame {
    private final TestCase testCase;
    private final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

    public Gui (TestCase testCase) {
        this.setTitle("Multi Agent Graph Exploration");
        this.testCase = testCase;

        setControlPanel();
        setNewGraphViewPanel();

        //close event
        this.addWindowListener(
                new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        testCase.stopped.set(true);
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
        JComboBox<GraphType> cmbGeneratorType = new JComboBox<>();
        for (GraphType gt : GraphType.values()) {
            cmbGeneratorType.addItem(gt);
        }
        //restart button
        JButton btnRestart = new JButton("Restart");

        //ActionListeners
        btnRestart.addActionListener(e -> {
            testCase.init((GraphType) cmbGeneratorType.getSelectedItem(), (int)txtNumberOfRobots.getSelectedItem(), false);
        });
        cmbGeneratorType.addActionListener(e -> {
            testCase.init((GraphType) cmbGeneratorType.getSelectedItem(), (int)txtNumberOfRobots.getSelectedItem(), true);
            setNewGraphViewPanel();
        });
        //next
        JButton btnNextStep = new JButton("Next step");
        btnNextStep.addActionListener(e -> testCase.tickOne());
        //start-stop
        JButton btnPause = new JButton("Start / stop");
        btnPause.addActionListener(e -> {
        if (!testCase.isRunning()) testCase.start();
        testCase.pause();
        });

        JPanel controlPanel = new JPanel();
        controlPanel.add(lblNumberOfRobots);
        controlPanel.add(txtNumberOfRobots);
        controlPanel.add(btnRestart);
        controlPanel.add(lblGeneratorType);
        controlPanel.add(cmbGeneratorType);
        controlPanel.add(btnNextStep);
        controlPanel.add(btnPause);
        controlPanel.add(testCase.stepCountLabel);

        splitPane.setLeftComponent(controlPanel);
    }
}
