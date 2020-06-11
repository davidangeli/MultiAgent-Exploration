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
        JComboBox<Integer> txtNumberOfRobots = new JComboBox<>(new Integer[] {1, 2, 3});
        //restart button
        JButton btnRestart = new JButton("Restart");
        btnRestart.addActionListener(e -> controller.reset((int)txtNumberOfRobots.getSelectedItem()));
        //generator type
        JLabel lblGeneratorType = new JLabel("Generator type");
        JComboBox<String> cmbGeneratorType = new JComboBox<>(new String[] {"Tutorial", "Random", "Lobster"});
        cmbGeneratorType.addActionListener(e -> {
            controller.init((String)cmbGeneratorType.getSelectedItem(), (int)txtNumberOfRobots.getSelectedItem());
            splitPane.setRightComponent(controller.getNewViewPanel());
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

        settingsPanel.add(lblNumberOfRobots);
        settingsPanel.add(txtNumberOfRobots);
        settingsPanel.add(btnRestart);
        settingsPanel.add(lblGeneratorType);
        settingsPanel.add(cmbGeneratorType);
        settingsPanel.add(btnNextStep);
        settingsPanel.add(btnPause);
        settingsPanel.add(controller.stepCount);

        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, settingsPanel, controller.getNewViewPanel());

        this.add(splitPane, BorderLayout.CENTER);
        //this.add(controller.getViewPanel(), BorderLayout.CENTER);
    }
}
