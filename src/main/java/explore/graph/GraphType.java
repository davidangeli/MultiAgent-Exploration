package main.java.explore.graph;

import org.graphstream.algorithm.generator.Generator;
import org.graphstream.algorithm.generator.LobsterGenerator;
import org.graphstream.algorithm.generator.RandomGenerator;

public enum GraphType {
    TUTORIAL ("tutorial"),
    RANDOM ("random"),
    LOBSTER ("lobster");

    public final String code;

    GraphType(String code) {
        this.code = code;
    }

    Generator getGenerator() {
        Generator generator;
        switch (code) {
            case "random":
                generator = new RandomGenerator(4, false, false);
                break;
            case "lobster":
                generator = new LobsterGenerator();
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
