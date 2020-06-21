package main.java.explore.graph;

import org.graphstream.algorithm.generator.Generator;
import org.graphstream.algorithm.generator.LobsterGenerator;
import org.graphstream.algorithm.generator.RandomGenerator;

public enum GraphType {
    TUTORIAL ("tutorial", null),
    RANDOM ("random", new RandomGenerator(4, false, false)),
    LOBSTER ("lobster", new LobsterGenerator());

    public final String code;
    public final Generator generator;

    GraphType(String code, Generator generator) {
        this.code = code;
        this.generator = generator;
    }
}
