package main.java.explore.graph;

import org.graphstream.algorithm.generator.Generator;
import org.graphstream.algorithm.generator.LobsterGenerator;
import org.graphstream.algorithm.generator.PetersenGraphGenerator;
import org.graphstream.algorithm.generator.RandomGenerator;

public enum GraphType {
    LOBSTER ("lobster"),
    PETERSEN ("petersen"),
    RANDOM ("random"),
    TUTORIAL ("tutorial");

    public final String code;

    GraphType(String code) {
        this.code = code;
    }

    Generator getGenerator(int avgDegree) {
        Generator generator;
        switch (this) {
            case LOBSTER:
                generator = new LobsterGenerator();
                break;
            case PETERSEN:
                generator = new PetersenGraphGenerator();
                break;
            case RANDOM:
                generator = new RandomGenerator(avgDegree, false, false);
                break;
            default:
                generator = null;
        }
        return generator;
    }

    @Override
    public String toString() {
        return code;
    }
}
