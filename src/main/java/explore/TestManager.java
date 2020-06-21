package main.java.explore;

import main.java.explore.algorithm.Algorithm;
import main.java.explore.algorithm.MultiRobotDFS;
import main.java.explore.algorithm.RotorRouter;
import main.java.explore.graph.GraphManager;
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
                GraphType graphType = GraphManager.getGraphType(sc.next());
                int graphSize = sc.nextInt();
                Algorithm algorithm = selectAlgorithm(sc.next());
                int agentNum = sc.nextInt();
                testCases.add(new TestCase(graphType, graphSize, algorithm, agentNum, false));
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
}
