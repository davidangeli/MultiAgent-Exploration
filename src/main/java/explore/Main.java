package main.java.explore;

public class Main {

    public static void main(String[] args) {

        Controller controller = new Controller(2,"Tutorial");
        Gui frame = new Gui(controller);
        controller.setGui(frame);
        frame.setVisible(true);
    }

}
