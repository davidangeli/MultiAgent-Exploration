package main.java.explore.graph;

import org.graphstream.algorithm.generator.Generator;
import org.graphstream.algorithm.measure.ConnectivityMeasure;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

import static main.java.explore.algorithm.Algorithm.LABELID;

public class GraphManager {

    public static final String GRAPH_TYPE_LABEL = "typeLabel";
    public static final String GRAPH_SIZE_LABEL = "sizeLabel";
    public static final String GRAPH_DEGREE_LABEL = "degreeLabel";
    public static final String GRAPH_STARTNODE_INDEX = "startNodeIndex";

    public static Graph getGraph(GraphType graphType, int graphSize, int avgDegree) throws IllegalArgumentException {
        Graph graph = new SingleGraph("MultiAgent");
        setGraphAttributes(graph, graphType, graphSize, avgDegree);
        return graph;
    }

    private static void setGraphAttributes(Graph graph, GraphType graphType, int graphSize, int avgDegree) {
        graph.addAttribute(GRAPH_TYPE_LABEL, graphType);
        graph.addAttribute(GRAPH_SIZE_LABEL, graphSize);
        graph.addAttribute(GRAPH_DEGREE_LABEL, avgDegree);
    }

    private static boolean isConnected(Graph graph) {
        return ConnectivityMeasure.isKEdgeConnected(graph,1);
    }

    public static void regenerateGraph (Graph graph) {
        GraphType graphType = graph.getAttribute(GRAPH_TYPE_LABEL);
        int graphSize = graph.getAttribute(GRAPH_SIZE_LABEL);
        int avgDegree = graph.getAttribute(GRAPH_DEGREE_LABEL);

        graph.setStrict(true);
        graph.setAutoCreate(false);
        Generator gen = graphType.getGenerator(avgDegree);
        switch (graphType)
        {
            case TUTORIAL:
                graph.clear();
                createTutorialGraph(graph);
                setGraphAttributes(graph, graphType, graphSize, avgDegree);
                return;
            case LOBSTER:
            case RANDOM:
            default:
                break;
        }

        gen.addSink(graph);

        boolean connected = false;
        while (!connected) {
            graph.clear();
            gen.begin();
            int i = 0;
            while ((i < graphSize - 1) && gen.nextEvents()) {
                i++;
            }
            gen.end();
            connected = isConnected(graph);
        }

        setGraphAttributes(graph, graphType, graphSize, avgDegree);
    }

    public static void resetGraph (Graph graph, int[] startNodeIndexes) {
        //set edges to unvisited and remove labels
        graph.getEdgeSet().forEach(e -> {
            EdgeState.UNVISITED.setEdge(e);
            e.removeAttribute(LABELID);
        });
        setStartNodeStyle(graph, startNodeIndexes);
    }

    private static void setStartNodeStyle(Graph graph, int[] nodeIndexes) {
        //clear previous start node data and reset nodes
        if (graph.hasAttribute(GRAPH_STARTNODE_INDEX)) {
            int[] startNodeIndexes = (int[]) graph.getAttribute(GRAPH_STARTNODE_INDEX);
            for (int startNodeIndex: startNodeIndexes) {
                setStartNodeStyle(graph.getNode(startNodeIndex), false);
            }
        }
        //set new ones
        graph.addAttribute(GRAPH_STARTNODE_INDEX, (Object) nodeIndexes);
        for (int startNodeIndex: nodeIndexes) {
            setStartNodeStyle(graph.getNode(startNodeIndex), true);
        }
    }

    private static void setStartNodeStyle(Node node, boolean on) {
        if (on) {
            node.setAttribute("ui.style", "size: 20;");
        }
        else {
            node.setAttribute("ui.style", "size: 10;");
        }
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
