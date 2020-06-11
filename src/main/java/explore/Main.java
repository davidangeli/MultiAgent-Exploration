package main.java.explore;

import main.java.explore.algorithm.MultiRobotDFS;

public class Main {

    public static void main(String[] args) {

        Controller controller = new Controller(2,"Tutorial", new MultiRobotDFS());
        Gui frame = new Gui(controller);
        controller.setGui(frame);
        frame.setVisible(true);
    }

}
