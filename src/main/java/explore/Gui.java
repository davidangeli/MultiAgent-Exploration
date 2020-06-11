package main.java.explore;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Gui extends JFrame {
    private JSplitPane splitPane;
    private final JLabel lblSteps;

    public Gui (Controller controller){
        this.setTitle("Multi Agent Graph Exploration");

        this.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e) {
                controller.stopped.set(true);
            }
        });

        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setSize(800, 800);
        this.setLocationRelativeTo(null);

        //settings panel
        JPanel settingsPanel = new JPanel();
        //number of robots
        JLabel lblNumberOfRobots = new JLabel("Number of robots");
        JComboBox<String> txtNumberOfRobots = new JComboBox<>(new String[] {"1", "2", "3"});
        //restart button
        JButton btnRestart = new JButton("Restart");
        btnRestart.addActionListener(e -> {
            setSteps(0);
            controller.reset(Integer.parseInt((String)txtNumberOfRobots.getSelectedItem()));
        });
        //generator type
        JLabel lblGeneratorType = new JLabel("Generator type");
        JComboBox<String> cmbGeneratorType = new JComboBox<>(new String[] {"Tutorial", "Random", "Lobster"});
        cmbGeneratorType.addActionListener(e -> {
            setSteps(0);
            controller.init((String)cmbGeneratorType.getSelectedItem(), Integer.parseInt((String)txtNumberOfRobots.getSelectedItem()));
            splitPane.setRightComponent(controller.getViewPanel());
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

        //Step count
        lblSteps = new JLabel("Step count: ");

        settingsPanel.add(lblNumberOfRobots);
        settingsPanel.add(txtNumberOfRobots);
        settingsPanel.add(btnRestart);
        settingsPanel.add(lblGeneratorType);
        settingsPanel.add(cmbGeneratorType);
        settingsPanel.add(btnNextStep);
        settingsPanel.add(btnPause);
        settingsPanel.add(lblSteps);

        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, settingsPanel, controller.getViewPanel());

        this.add(splitPane, BorderLayout.CENTER);
        //this.add(controller.getViewPanel(), BorderLayout.CENTER);
    }

    public void setSteps(int s){
        lblSteps.setText("Step count: " + s);
    }
}
