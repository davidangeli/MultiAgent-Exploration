package main.java.explore;

import main.java.explore.algorithm.MultiRobotDFS;
import main.java.explore.algorithm.RotorRouter;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.logging.*;

public class Main {
    private static final String CONFIGFILE = "config.properties";
    private static final Properties properties = new Properties();
    public static final Logger logger = Logger.getLogger("");

    public static void main(String[] args) {

        //get Properties
        try {
            properties.load(new FileInputStream(CONFIGFILE));
        } catch (IOException ex) {
            logger.log(Level.WARNING, "Error while reading properties file. Application quits.");
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

        TestController controller;
        //controller = new TestController(2,"Tutorial", new RotorRouter(), true);
        controller = new TestController(2,"Tutorial", new MultiRobotDFS(), true);

        Gui frame = new Gui(controller);
        frame.setVisible(true);
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
                sb.append(record.getMessage());
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
