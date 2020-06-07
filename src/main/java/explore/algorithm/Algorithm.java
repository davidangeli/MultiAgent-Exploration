package main.java.explore.algorithm;

import main.java.explore.Agent;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import java.util.concurrent.TimeoutException;

/**
 * Inteface declaring expected methods for any exploration algorithms.
 * @param <S> Type parameter for storage on graph nodes.
 * @param <M> Type parameter for memory of the agents.
 */
public interface Algorithm<S, M>  {
    public static final String STORAGELABEL = "storage";
    public static final String MEMORYLABEL = "memory";
    public final static String EDGESTATELABEL = "ss";

    public void initGraph (Graph graph);
    public void initAgent (Agent agent);
    public void evaluatePosition (Agent agent) throws TimeoutException;
    public Edge selectNextStep (Agent agent);

}
