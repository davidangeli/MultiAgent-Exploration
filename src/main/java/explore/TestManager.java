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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class TestManager {
    public static final String ROTORROUTERCODE = "rr";
    public static final String MULTIAGENTDFSCODE = "madfs";
    public static final String MULTIAGENTDDFSCODE = "maddfs";
    public static final String MULTIAGENTEDDFSCODE = "maeddfs";
    private static final char COMMENTLINE = '#';

    private final Properties properties;
    private final HashMap<TestCase, Future<int[]>> testCases = new HashMap<>();
    private static final Logger logger = Logger.getLogger(TestCase.class.getName());

    public TestManager(String inputFile, String outputFile, Properties properties) {
        this.properties = properties;
        logger.setUseParentHandlers(true);

        //read input file
        try {
            readTestCaseFile(inputFile);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Test cases input file could not be opened.");
            return;
        }

        //check output file, write headers
        try {
            printResultsHeaders(outputFile);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Test cases output file could not be opened.");
            return;
        }

        logger.log(Level.INFO, "TestManager created.");

        //do the things
        runTests();
        printResults(outputFile);
    }

    private void runTests() {
        int timeout = Main.getIntProperty(properties, "testcase.timeout", Main.TESTCASE_TIMEOUT);
        int cores = Runtime.getRuntime().availableProcessors();

        ExecutorService executorService = Executors.newFixedThreadPool(cores);
        logger.log(Level.INFO, "Executor created with " + cores + " threads.");

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

    private void printResultsHeaders(String outputFile) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
        writer.write("Testcase;Algorithm;Agents;GraphType;GraphSize;Nodes;AvgDegree;Edges;Repeats;allEdgeVisited;minSteps;maxSteps;avgSteps;deviation");
        writer.newLine();
        writer.flush();
        writer.close();
        logger.log(Level.INFO, "Results headers written into the output file.");
    }

    private void printResults(String outputFile) {
        int[] dummyResult = new int[5];

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, true))) {
            testCases.forEach((tc, f) -> {

                try {
                    int[] result;
                    if (f.isDone()) {
                        result = f.get();
                    }
                    else {
                        result = dummyResult;
                    }
                    writer.write(tc + ";" + result[0] + ";" + result[1] + ";" + result[2] + ";" + result[3] + ";" + result[4]);
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

    private void readTestCaseFile (String fileName) throws IOException {
        int minDegree = Main.getIntProperty(properties, "testcase.min_degree", Main.TESTCASE_MINDEGREE);
        int maxDegree = Main.getIntProperty(properties, "testcase.max_degree", Main.TESTCASE_MAXDEGREE);
        Stream<String> stream = Files.lines(Paths.get(fileName));
        stream.forEach((line) -> {
            if (line.isBlank() || line.charAt(0) == COMMENTLINE) {
                return;
            }
            try {
                parseInputLine(line, minDegree, maxDegree);
                logger.log(Level.INFO, "Test cases added: {0}", new Object[]{line});
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, "Test cases parse error: {0}: {1}", new Object[]{line, ex.getMessage()});
            }
        });
        stream.close();
    }

    private void parseInputLine (String line, int minDegree, int maxDegree) throws IllegalArgumentException, InputMismatchException, NullPointerException {
        Scanner sc = new Scanner (line);

        GraphType graphType = GraphManager.getGraphType(sc.next());
        //range: either a number x,x,1 or a range x,y,s
        int[] sizeRange = parseRange(sc.next());
        int[] degreeRange = parseRange(sc.next(), minDegree, Integer.min(maxDegree, sizeRange[1]-1));
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
    private int[] parseRange(String token) throws NumberFormatException {
        return parseRange(token, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    /**
     * Reads integer range information (min, max, step) from a String token.
     * @param token String input with integer parameters
     * @param minBound Specifies minimum acceptable value for this range.
     * @param maxBound Specifies minimum acceptable value for this range.
     * @return A 3 long integer array.
     * @throws NumberFormatException If the token does not have the expected integer representations.
     * @throws IllegalArgumentException If the value read does not fit between minBound and maxBound.
     */
    private int[] parseRange (String token, int minBound, int maxBound) throws IllegalArgumentException {

        int[] result = new int[3];
        String RANGESEPARATOR = "-";
        String STEPSEPARATOR = ":";

        String[] parts = token.split(STEPSEPARATOR);
        String[] range = parts[0].split(RANGESEPARATOR);
        result[0] = Integer.parseInt(range[0]);
        result[1] = Integer.parseInt(range[range.length-1]);
        if (result[0] < minBound || result[1] > maxBound) {
            throw new IllegalArgumentException("Parameter value do not fit into min and max bounds.");
        }
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
                throw new IllegalArgumentException("No match for this algorithm code:" + argument);
        }
        return result;
    }
}
