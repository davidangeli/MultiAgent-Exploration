package main.java.explore;

import lombok.Data;
import org.graphstream.graph.Node;

@Data
public class Agent implements Runnable {
    private Object Memory;
    private Node currentNode;
    protected static int idc;
    private final int id;

    public Agent (Node node) {
        this.id = ++idc;
        this.currentNode = node;
    }

    @Override
    public void run() {

    }
}
