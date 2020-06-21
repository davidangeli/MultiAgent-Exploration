package main.java.explore;

import main.java.explore.algorithm.Algorithm;
import main.java.explore.algorithm.MultiRobotDFS;
import main.java.explore.algorithm.RotorRouter;
import main.java.explore.graph.GraphType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.*;
import java.util.stream.Stream;

public class TestManager {
    private static final String ROTORROUTERCODE = "rr";
    private static final String MULTIROBOTDFSCODE = "mrdfs";
    private static final String GRAPHTUTORIALCODE = "Tutorial";
    private static final String GRAPHRANDOMCODE = "Random";
    private static final String GRAPHLOBSTERCODE = "Lobster";

    private static final Logger logger = Logger.getLogger(TestCase.class.getName());
    private static final ArrayList<TestCase> testCases = new ArrayList<>();

    public TestManager(String fileName) {
        logger.setUseParentHandlers(true);
        readTestCaseFile(fileName);
    }

    private void readTestCaseFile (String fileName) {
        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
            stream.forEach((line) -> {
                Scanner sc = new Scanner (line);
                GraphType graphType = selectGraphType(sc.next());
                Algorithm algorithm = selectAlgorithm(sc.next());
                int agentNum = sc.nextInt();
                testCases.add(new TestCase(graphType, algorithm, agentNum, false));
            });
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Test cases file could not be opened.");
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            logger.log(Level.SEVERE, "Test cases file parameter error.");
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

    private static GraphType selectGraphType (String argument) throws IllegalArgumentException {
        switch (argument) {
            case GRAPHTUTORIALCODE:
                return GraphType.TUTORIAL;
            case GRAPHRANDOMCODE:
                return GraphType.RANDOM;
            case GRAPHLOBSTERCODE:
                return GraphType.LOBSTER;
            default:
                throw new IllegalArgumentException();
        }
    }
}
