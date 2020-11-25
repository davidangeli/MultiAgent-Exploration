package main.java.explore.graph;

import org.graphstream.algorithm.generator.*;

public enum GraphType {
    LOBSTER ("lobster"),
    PETERSEN ("petersen"),
    RANDOM ("random"),
    COMPLETE ("complete"),
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
            case COMPLETE:
                generator = new FullGenerator();
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
