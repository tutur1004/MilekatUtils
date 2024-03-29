package fr.milekat.utils;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public class MileLogger {
    private final Logger logger;
    private boolean DEBUG;

    public MileLogger() {
        this.logger = Logger.getLogger("MileLogger");
    }

    public MileLogger(Boolean debug) {
        this.logger = Logger.getLogger("MileLogger");
        this.DEBUG = debug;
    }

    public MileLogger(String name) {
        this.logger = Logger.getLogger(name);
    }

    public MileLogger(String name, Boolean debug) {
        this.logger = Logger.getLogger(name);
        this.DEBUG = debug;
    }

    public MileLogger(Logger logger) {
        this.logger = logger;
    }

    public MileLogger(Logger logger, Boolean debug) {
        this.logger = logger;
        this.DEBUG = debug;
    }

    public void log(Level level, String message) {
        logger.log(level, message);
    }

    public void debug(String message) {
        if (DEBUG) info("[DEBUG] " + message);
    }

    public void info(String message) {
        logger.info(message);
    }

    public void warning(String message) {
        logger.warning(message);
    }

    public void stack(StackTraceElement[] stacks) {
        if (DEBUG) Arrays.stream(stacks).distinct().forEach(stackTraceElement -> warning(stackTraceElement.toString()));
    }

    public boolean isDebug() {
        return DEBUG;
    }

    public void setDebug(Boolean debug) {
        this.DEBUG = debug;
    }
}
