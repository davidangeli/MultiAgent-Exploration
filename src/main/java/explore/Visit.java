package main.java.explore;

import lombok.Getter;
import lombok.Setter;
import org.graphstream.graph.Edge;
import java.time.Instant;

@Getter
@Setter
public class Visit {
    public final Robot robot;
    public final Edge from;
    public final boolean original;
    private Edge to;
    private Instant when;
    private final EdgeState fromEdgePrevState;

    public Visit(Robot robot, Edge from, boolean original, EdgeState state){
        this.robot = robot;
        this.from = from;
        this.original = original;
        this.fromEdgePrevState = state;
    }

    public boolean isNotFinished(){
        return when == null;
    }

    public void finish() {
        this.when = Instant.now();
    }

    @Override
    public String toString(){
        String label = this.robot.toString() + ": ";
        label += from == null ? "null" : from.getId();
        label += "->";
        label += to == null ? "null" : to.getId();
        return label;
    }

}
