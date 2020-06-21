package main.java.explore;

import org.graphstream.ui.view.Viewer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Gui extends JFrame {
    private final TestCase controller;
    private final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

    public Gui (TestCase controller) {
        this.setTitle("Multi Agent Graph Exploration");
        this.controller = controller;

        setControlPanel();
        setNewGraphViewPanel();

        //close event
        this.addWindowListener(
                new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        controller.stopped.set(true);
                    }
        });

        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setSize(800, 800);
        this.setLocationRelativeTo(null);

        this.add(splitPane, BorderLayout.CENTER);
    }
    
    private void setNewGraphViewPanel () {
        Viewer viewer = new Viewer(controller.getGraph(), Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
        viewer.enableAutoLayout();
        splitPane.setRightComponent(viewer.addDefaultView(false));
    }
    
    private void setControlPanel () {
        //number of robots
        JLabel lblNumberOfRobots = new JLabel("Number of robots");
        JComboBox<Integer> txtNumberOfRobots = new JComboBox<>(new Integer[] {1, 2, 3});
        //restart button
        JButton btnRestart = new JButton("Restart");
        btnRestart.addActionListener(e -> controller.reset((int)txtNumberOfRobots.getSelectedItem()));
        //generator type
        JLabel lblGeneratorType = new JLabel("Generator type");
        JComboBox<String> cmbGeneratorType = new JComboBox<>(new String[] {"Tutorial", "Random", "Lobster"});
        cmbGeneratorType.addActionListener(e -> {
            controller.init((String)cmbGeneratorType.getSelectedItem(), (int)txtNumberOfRobots.getSelectedItem());
            setNewGraphViewPanel();
        });
        //next
        JButton btnNextStep = new JButton("Next step");
        btnNextStep.addActionListener(e -> controller.tickOne());
        //start-stop
        JButton btnPause = new JButton("Start / stop");
        btnPause.addActionListener(e -> {
        if (!controller.isRunning()) controller.start();
        controller.pause();
        });

        JPanel controlPanel = new JPanel();
        controlPanel.add(lblNumberOfRobots);
        controlPanel.add(txtNumberOfRobots);
        controlPanel.add(btnRestart);
        controlPanel.add(lblGeneratorType);
        controlPanel.add(cmbGeneratorType);
        controlPanel.add(btnNextStep);
        controlPanel.add(btnPause);
        controlPanel.add(controller.stepCount);

        splitPane.setLeftComponent(controlPanel);
    }
}
