package main.java.explore;

import lombok.Data;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;

/**
 * Represents an agent that is exploring a graph following an algorithm.
 */

@Data
public class Agent implements Runnable {
    protected static int idc;
    private final int id;
    private Object memory;
    private Node currentNode;
    private volatile boolean running = true, paused = false;
    private int moves;

    public Agent (Node node) {
        this.id = ++idc;
        this.currentNode = node;
    }

    @Override
    public void run() {
        //TODO: implement parallel run
        while (running) {
        }
    }

    public void move(Edge moveOn) {
        moves++;
        currentNode = moveOn.getOpposite(currentNode);
    }

    public void stop() {
        running = false;
    }

    public void pause(boolean p) {
        paused = p;
    }

    @Override
    public String toString() {
        return "Agent" + id;
    }

    public String getCode () { return "A" + id; }

    //TODO: better equals and hash, or with lombok
    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Agent))
            return false;

        return ((Agent) o).id == this.id;
    }

    @Override
    public final int hashCode() {
        return id;
    }
}
