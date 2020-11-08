package main.java.explore;

import main.java.explore.algorithm.*;
import main.java.explore.graph.GraphManager;
import main.java.explore.graph.GraphType;
import org.graphstream.graph.Graph;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;
import java.util.stream.Stream;

public class TestManager {
    public static final String ROTORROUTERCODE = "rr";
    public static final String MULTIAGENTDFSCODE = "madfs";
    public static final String MULTIAGENTDDFSCODE = "maddfs";
    public static final String MULTIAGENTEDDFSCODE = "maeddfs";
    private static final char COMMENTLINE = '#';

    private static final Logger logger = Logger.getLogger(TestCase.class.getName());
    private static final HashMap<TestCase, Future<int[]>> testCases = new HashMap<>();

    public TestManager(String inputFile, String outputFile, int timeout) {
        logger.setUseParentHandlers(true);
        readTestCaseFile(inputFile);
        runTests(timeout);
        printResults(outputFile);
    }

    public TestManager(String inputFile, int timeout) {
        logger.setUseParentHandlers(true);
        readTestCaseFile(inputFile);
        runTests(timeout);
        printResults();
    }

    private void runTests(int timeout) {
        ExecutorService executorService = Executors.newCachedThreadPool();
        testCases.entrySet().forEach(tc -> tc.setValue(executorService.submit(tc.getKey())));
        logger.log(Level.INFO, "Submitted cases to the executor.");

        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(timeout, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
                logger.log(Level.INFO, "Executor shutdownNow() called.");
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
        logger.log(Level.INFO, "Executor done.");
    }

    private void printResults(String outputFile) {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            writer.write("Testcase;Algorithm;Agents;GraphType;GraphSize;Nodes;AvgDegree;Edges;Repeats;allEdgeVisited;minSteps;maxSteps;avgSteps;deviation");
            writer.newLine();
            testCases.forEach((tc, f) -> {

                try {
                    if (f.isDone()) {
                        int[] result = f.get();
                        writer.write(tc + ";" + result[0] + ";" + result[1] + ";" + result[2] + ";" + result[3] + ";" + result[4]);
                    }
                    else {
                        writer.write(tc + ";timeout");
                    }
                    writer.newLine();

                } catch (InterruptedException | ExecutionException | IOException e) {
                    logger.log(Level.SEVERE, "Error reading Future or writing Output.");
                    e.printStackTrace();
                }
            });
            writer.flush();
            logger.log(Level.INFO, "Results written into the output file.");

        } catch (IOException e) {
            logger.log(Level.SEVERE, "FileWriter error on opening output file.");
        }
    }

    private void printResults() {
        testCases.forEach((tc, f) -> {
            String result = "exception";
            try {
                result = f.isDone() ? Arrays.toString(f.get()) : "timeout";
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

            logger.log(Level.INFO, "{0} results: {1} ", new Object[]{tc, result});
        });
    }

    private void readTestCaseFile (String fileName) {
        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
            stream.forEach((line) -> {
                if (line.length()>1 && line.charAt(0) == COMMENTLINE) {
                    return;
                }
                try {
                    parseInputLine(line);
                    logger.log(Level.INFO, "Test cases added: {0}", new Object[]{line});
                }
                catch (Exception ex) {
                    logger.log(Level.WARNING, "Test cases parse error: {0}: {1}", new Object[]{line, ex.getMessage()});
                }
            });
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Test cases file could not be opened.");
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            logger.log(Level.SEVERE, "Test cases file parameter error.");
        }
    }

    private void parseInputLine (String line) throws IllegalArgumentException, InputMismatchException, NullPointerException {
        Scanner sc = new Scanner (line);

        GraphType graphType = GraphManager.getGraphType(sc.next());
        //range: either a number x,x,1 or a range x,y,s
        int[] sizeRange = parseRange(sc.next());
        int[] degreeRange = parseRange(sc.next());
        Algorithm algorithm = selectAlgorithm(sc.next());
        int[] agentRange = parseRange(sc.next());
        int repeats = sc.nextInt();

        sc.close();

        for (int graphSize = sizeRange[0]; graphSize <= sizeRange[1]; graphSize += sizeRange[2]) {
            for (int agentNum = agentRange[0]; agentNum <= agentRange[1]; agentNum += agentRange[2]) {
                for (int avgDegree = degreeRange[0]; avgDegree <= degreeRange[1]; avgDegree += degreeRange[2]) {
                    Graph graph = GraphManager.getGraph(graphType, graphSize, avgDegree);
                    testCases.put(new TestCase(graph, algorithm, agentNum, repeats), null);
                }
            }
        }
    }

    /**
     * Reads integer range information (min, max, step) from a String token.
     * @param token String input with integer parameters
     * @return A 3 long integer array.
     * @throws NumberFormatException If the token does not have the expected integer representations.
     */
    private int[] parseRange (String token) throws NumberFormatException {

        int[] result = new int[3];
        String RANGESEPARATOR = "-";
        String STEPSEPARATOR = ":";

        String[] parts = token.split(STEPSEPARATOR);
        String[] range = parts[0].split(RANGESEPARATOR);
        result[0] = Integer.parseInt(range[0]);
        result[1] = Integer.parseInt(range[range.length-1]);
        result[2] = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;

        return result;
    }

    public static Algorithm selectAlgorithm (String argument) throws IllegalArgumentException {
        Algorithm result;
        switch (argument.toLowerCase()) {
            case ROTORROUTERCODE:
                result = new RotorRouter();
                break;
            case MULTIAGENTDFSCODE:
                result = new DFS();
                break;
            case MULTIAGENTDDFSCODE:
                result = new DistributedDFS();
                break;
            case MULTIAGENTEDDFSCODE:
                result = new ExtendedDDFS();
                break;
            default:
                throw new IllegalArgumentException();
        }
        return result;
    }
}
