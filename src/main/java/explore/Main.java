package main.java.explore;

import main.java.explore.algorithm.MultiRobotDFS;
import main.java.explore.algorithm.RotorRouter;

public class Main {

    public static void main(String[] args) {

        TestController controller = new TestController(2,"Tutorial", new RotorRouter());
        Gui frame = new Gui(controller);
        frame.setVisible(true);
    }

}
