package main.java.explore;

import org.graphstream.ui.swingViewer.ViewPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Gui extends JFrame {
    private Controller controller;
    private JSplitPane split;
    private ViewPanel graphViewPanel;
    private keyHandler kH;
    private JLabel lblSteps;

    public Gui (Controller controller){
        this.setTitle("Multi Agent Graph Exploration");
        this.controller = controller;
        this.kH = new keyHandler();
        this.addKeyListener(kH);
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
        settingsPanel.addKeyListener(kH);
        //robotok szama
        JLabel lblNumberOfRobots = new JLabel("Number of robots");
        lblNumberOfRobots.addKeyListener(kH);
        String[] robotNumbers = new String[] {"1", "2", "3"};
        JComboBox<String> txtNumberOfRobots = new JComboBox<>(robotNumbers);
        //restart gomb
        JButton btnRestart = new JButton("Restart");
        btnRestart.addKeyListener(kH);
        btnRestart.addActionListener(e -> {
            setSteps(0);
            controller.reset(Integer.parseInt((String)txtNumberOfRobots.getSelectedItem()));
        });
        //generátor típus
        JLabel lblGeneratorType = new JLabel("Generator type");
        String[] genTypes = new String[] {"Tutorial", "Random", "Lobster"};
        JComboBox<String> cmbGeneratorType = new JComboBox<String>(genTypes);
        cmbGeneratorType.addActionListener(e -> {
            setSteps(0);
            controller.init((String)cmbGeneratorType.getSelectedItem(), Integer.parseInt((String)txtNumberOfRobots.getSelectedItem()));
            split.setRightComponent(controller.getViewPanel());
        });
        //next
        JButton btnNextStep = new JButton("Next step");
        btnNextStep.addKeyListener(kH);
        btnNextStep.addActionListener(e -> controller.tickOne());
        //start-stop
        JButton btnPause = new JButton("Start / stop");
        btnPause.addKeyListener(kH);
        btnPause.addActionListener(e -> {
            if (!controller.isRunning()) controller.start();
            controller.pause();
        });

        //lépésszám
        lblSteps = new JLabel("Step count: ");

        settingsPanel.add(lblNumberOfRobots);
        settingsPanel.add(txtNumberOfRobots);
        settingsPanel.add(btnRestart);
        settingsPanel.add(lblGeneratorType);
        settingsPanel.add(cmbGeneratorType);
        settingsPanel.add(btnNextStep);
        settingsPanel.add(btnPause);
        settingsPanel.add(lblSteps);

        this.graphViewPanel =controller.getViewPanel();
        split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,settingsPanel, graphViewPanel);
        split.addKeyListener(kH);

        this.add(split, BorderLayout.CENTER);

        //this.add(controller.getViewPanel(), BorderLayout.CENTER);
    }

    public void setSteps(int s){
        lblSteps.setText("Step count: " + s);
    }

    private class keyHandler implements KeyListener {

        @Override
        public synchronized void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()){
                case KeyEvent.VK_RIGHT:
                    controller.tickOne();
                    break;
                //case KeyEvent.VK_LEFT:
                //    break;
                case KeyEvent.VK_SPACE:
                    controller.pause();
                    break;
            }
        }

        @Override
        public void keyTyped(KeyEvent e) {
            //throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public synchronized void keyReleased(KeyEvent e) {
            //throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
