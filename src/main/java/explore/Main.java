package main.java.explore;

import main.java.explore.algorithm.MultiRobotDFS;
import main.java.explore.graph.GraphManager;
import main.java.explore.graph.GraphType;
import org.graphstream.graph.Graph;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.logging.*;

public class Main {
    private static final String CONFIGFILE = "/config.properties";
    private static final String DEFAULT_INPUT_FILE = "input.txt";
    private static final String DEFAULT_OUTPUT_FILE = "output.txt";
    public final static int GUI_GRAPHSIZE = 8, GUI_GRAPH_DEGREE = 4;
    private static final Properties properties = new Properties();
    public static final Logger logger = Logger.getLogger("");

    public static void main(String[] args) {

        //get Properties
        try (InputStream inputStream = Main.class.getResourceAsStream(CONFIGFILE)) {
            properties.load(inputStream);
        } catch (IOException ex) {
            logger.log(Level.INFO, "Error while reading properties file. Application quits.");
            return;
        }

        //set Logging
        try {
            setLogging();
        } catch (IOException | SecurityException ex) {
            logger.log(Level.WARNING, "Log file cannot be opened. Application quits.");
            endLogging();
            return;
        }

        logger.log(Level.INFO, "Setup finished.");

        if (args.length == 0) {
            TestCase testCase;
            Graph graph = GraphManager.getGraph(GraphType.TUTORIAL, GUI_GRAPHSIZE, GUI_GRAPH_DEGREE);
            testCase = new TestCase(graph, new MultiRobotDFS(),2,true, 1);
            Gui frame = new Gui(testCase);
            frame.setVisible(true);
            logger.log(Level.INFO, "Graphical interface started.");
        }
        else {
            int timeout = Integer.parseInt(properties.getProperty("testcase.timeout"));
            TestManager testManager = new TestManager(DEFAULT_INPUT_FILE, DEFAULT_OUTPUT_FILE, timeout);
            logger.log(Level.INFO, "TestManager created.");
        }
    }

    private static void setLogging() throws SecurityException, IOException {

        String path = properties.getProperty("app.logpath");
        String str = path.endsWith("/") ? "" : "/";

        //filehandler
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        FileHandler fh = new FileHandler(path + str + sdf.format(date) + ".log");

        //formatter
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy:MM:dd  HH:mm:ss");
        SimpleFormatter sf = new SimpleFormatter() {
            @Override
            public String format(LogRecord record) {
                String LINE_SEPARATOR = System.getProperty("line.separator");
                String FQER = "main.java.explore.";

                StringBuilder sb = new StringBuilder();
                sb.append(sdf2.format(new Date(record.getMillis())));
                sb.append("\t");
                sb.append(record.getLevel());
                sb.append("\t");
                sb.append(record.getSourceClassName().replace(FQER,""));
                sb.append(":\t");
                String message = record.getMessage();
                Object[] parameters= record.getParameters();
                if (parameters != null) {
                    for (int i = 0; i < parameters.length; i++) {
                        message = message.replace("{"+i+"}", parameters[i].toString());
                    }
                }
                sb.append(message);
                sb.append(LINE_SEPARATOR);
                return sb.toString();
            }
        };

        logger.addHandler(fh);

        for (Handler handler : logger.getHandlers()) {
            handler.setFormatter(sf);
        }

        logger.setLevel(Level.parse(properties.getProperty("app.loglevel")));
    }

    private static void endLogging() {
        for (Handler handler : logger.getHandlers()) {
            handler.close();
        }
    }

}
