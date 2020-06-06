package main.java.explore;

import org.graphstream.graph.Edge;

public enum EdgeState {
    UNVISITED ("size: 1px;fill-color: rgb(47,79,79);"),
    VISITED ("size: 3px;fill-color: rgb(47,79,79);"),
    FINISHED ("size: 3px;fill-color: rgb(205,92,92);");

    public final String style;

    EdgeState (String style){
        this.style = style;
    }

    public void setEdge(Edge edge){
        edge.addAttribute("state", EdgeState.this);
        edge.addAttribute("ui.style", this.style);
    }

}

