package fr.milekat.utils;

import java.util.Arrays;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

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
        configureLogger();
    }

    public MileLogger(String name) {
        this.logger = Logger.getLogger(name);
        configureLogger();
    }

    public MileLogger(String name, Boolean debug) {
        this.logger = Logger.getLogger(name);
        this.DEBUG = debug;
        configureLogger();
    }

    public MileLogger(Logger logger) {
        this.logger = logger;
    }

    public MileLogger(Logger logger, Boolean debug) {
        this.logger = logger;
        this.DEBUG = debug;
    }

    private void configureLogger() {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter() {
            private static final String format = "[%1$tF %1$tT] [%2$s] %3$s %n";

            @Override
            public synchronized String format(java.util.logging.LogRecord lr) {
                return String.format(format,
                        new java.util.Date(lr.getMillis()),
                        lr.getLevel().getLocalizedName(),
                        lr.getMessage()
                );
            }
        });
        logger.setUseParentHandlers(false);
        logger.addHandler(handler);
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

    public void warn(String message) {
        logger.warning(message);
    }

    public void warning(String message) {
        logger.warning(message);
    }

    public void error(String message) {
        logger.severe(message);
    }

    public void severe(String message) {
        logger.severe(message);
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
