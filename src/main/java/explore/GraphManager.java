package main.java.explore;

import org.graphstream.algorithm.generator.Generator;
import org.graphstream.algorithm.generator.LobsterGenerator;
import org.graphstream.algorithm.generator.RandomGenerator;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

public class GraphManager {

    public static void createGraph (Graph graph, String graphType){
        graph.clear();
        Generator gen;
        int depth;
        switch (graphType)
        {
            case "Tutorial":
                createTutorialGraph(graph);
                return;
            case "Lobster":
                gen = new LobsterGenerator();
                depth = 50;
                break;
            case "Random":
            default:
                gen = new RandomGenerator(4, false, false);
                depth = 3;
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
