package main.java.explore.graph;

import org.graphstream.algorithm.generator.Generator;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

public class GraphManager {

    public static Graph getGraph(GraphType graphType, int graphSize) throws IllegalArgumentException {
        Graph graph = new SingleGraph("MultiAgent");
        resetGraph(graph, graphType, graphSize);
        return graph;
    }

    public static void resetGraph (Graph graph, GraphType graphType, int graphSize) {
        graph.clear();
        graph.setStrict(true);
        graph.setAutoCreate(false);
        Generator gen = graphType.generator;
        switch (graphType)
        {
            case TUTORIAL:
                createTutorialGraph(graph);
                return;
            case LOBSTER:
            case RANDOM:
            default:
                break;
        }
        gen.addSink(graph);
        gen.begin();
        for(int i=0; i < graphSize; i++)
            gen.nextEvents();
        gen.end();
    }

    public static void setStartNodeStyle(Node node) {
        node.addAttribute("ui.style", "size: 20;");
    }

    private static void createTutorialGraph(Graph graph) {
        graph.setStrict(false);
        graph.setAutoCreate( true );
        graph.addEdge("AB", "A", "B" ,false);
        graph.addEdge("BC", "B", "C" ,false);
        graph.addEdge("CA", "C", "A" ,false);
        graph.addEdge("CD", "C", "D" ,false);
        graph.addEdge("DE", "D", "E" ,false);
        graph.addEdge("DF", "D", "F" ,false);
    }

    public static GraphType getGraphType (String graphTypeString) throws IllegalArgumentException {
        for (GraphType gt : GraphType.values()) {
            if (gt.code.equals(graphTypeString)) {
                return  gt;
            }
        }

        throw new IllegalArgumentException();
    }
}
