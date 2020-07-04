package main.java.explore;

import main.java.explore.algorithm.Algorithm;
import main.java.explore.algorithm.MultiRobotDFS;
import main.java.explore.algorithm.RotorRouter;
import main.java.explore.graph.GraphManager;
import main.java.explore.graph.GraphType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;
import java.util.stream.Stream;

public class TestManager {
    private static final String ROTORROUTERCODE = "rr";
    private static final String MULTIROBOTDFSCODE = "mrdfs";
    private static final char COMMENTLINE = '#';

    private static final Logger logger = Logger.getLogger(TestCase.class.getName());
    private static final HashMap<TestCase, Future<Integer>> testCases = new HashMap<>();

    public TestManager(String fileName, int timeout) {
        logger.setUseParentHandlers(true);
        readTestCaseFile(fileName);
        runTests(timeout);
        printResults();
    }

    private void runTests(int timeout) {
        ExecutorService executorService = Executors.newCachedThreadPool();
        testCases.entrySet().forEach(tc -> tc.setValue(executorService.submit(tc.getKey())));
        logger.log(Level.INFO, "Submitted cases to the executor.");

        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(timeout, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
                logger.log(Level.INFO, "Executor shutdownNow() called.");
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
        logger.log(Level.INFO, "Executor done.");
    }

    private void printResults() {
        testCases.forEach((tc, f) -> {
            String result = "exception";
            try {
                result = f.isDone() ? f.get().toString() : "timeout";
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            logger.log(Level.INFO, "{0} results: {1} ", new Object[]{tc, result});
        });
    }

    //TODO: check if the graph is connected
    private void readTestCaseFile (String fileName) {
        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
            stream.forEach((line) -> {
                if (line.length()>1 && line.charAt(0) == COMMENTLINE) {
                    return;
                }
                try {
                    parseInputLine(line);
                    logger.log(Level.INFO, "Test case created: {0}", new Object[]{line});
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

        //graph size: either a number x or a range x-y
        String[] sizeRange = sc.next().split("-");
        int minSize = Integer.parseInt(sizeRange[0]);
        int maxSize = Integer.parseInt(sizeRange[sizeRange.length-1]);

        Algorithm algorithm = selectAlgorithm(sc.next());

        //agent number: either a number x or a range x-y
        String[] agentRange = sc.next().split("-");
        int minAgents = Integer.parseInt(agentRange[0]);
        int maxAgents = Integer.parseInt(agentRange[agentRange.length-1]);

        sc.close();

        for (int graphSize = minSize; graphSize <= maxSize; graphSize++) {
            for (int agentNum = minAgents; agentNum <= maxAgents; agentNum++) {
                testCases.put(new TestCase(graphType, graphSize, algorithm, agentNum, false), null);
            }
        }
    }

    private static Algorithm selectAlgorithm (String argument) throws IllegalArgumentException {
        Algorithm result;
        switch (argument.toLowerCase()) {
            case ROTORROUTERCODE:
                result = new RotorRouter();
                break;
            case MULTIROBOTDFSCODE:
                result = new MultiRobotDFS();
                break;
            default:
                throw new IllegalArgumentException();
        }
        return result;
    }
}
