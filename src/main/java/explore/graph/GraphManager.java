package main.java.explore.graph;

import org.graphstream.algorithm.generator.Generator;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

public class GraphManager {

    public static Graph getGraph(String graphTypeString, int size) throws IllegalArgumentException {
        Graph graph = new SingleGraph("MultiAgent");

        for (GraphType graphType : GraphType.values()) {
            if (graphTypeString.equals(graphType.code)) {
                createGraph(graph, graphType, size);
                return graph;
            }
        }
        throw new IllegalArgumentException();
    }

    public static void createGraph (Graph graph, GraphType graphType) {
        createGraph(graph, graphType, 10);
    }

    public static void createGraph (Graph graph, GraphType graphType, int size) {
        graph.clear();
        Generator gen = graphType.generator;
        int depth;
        switch (graphType)
        {
            case TUTORIAL:
                createTutorialGraph(graph);
                return;
            case LOBSTER:
                depth = 10;
                break;
            case RANDOM:
            default:
                depth = 10;
                break;
        }
        gen.addSink(graph);
        gen.begin();
        for(int i=0; i<depth; i++)
            gen.nextEvents();
        gen.end();
    }

    public static void setStartNodeStyle(Node node) {
        node.addAttribute("ui.style", "size: 20;");
    }

    private static void createTutorialGraph(Graph graph) {
        graph.addNode("A" );
        graph.addNode("B" );
        graph.addNode("C" );
        graph.addNode("D" );
        graph.addNode("E" );
        graph.addNode("F" );
        graph.addEdge("AB", "A", "B" ,false);
        graph.addEdge("BC", "B", "C" ,false);
        graph.addEdge("CA", "C", "A" ,false);
        graph.addEdge("CD", "C", "D" ,false);
        graph.addEdge("DE", "D", "E" ,false);
        graph.addEdge("DF", "D", "F" ,false);
    }
}
