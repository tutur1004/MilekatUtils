package fr.milekat.utils.logger;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.slf4j.LoggerFactory;

@SuppressWarnings("unused")
public class LoggerAdapter extends Logger {
    private final org.slf4j.Logger slf4jLogger;

    protected LoggerAdapter(String name) {
        super(name, null);
        this.slf4jLogger = LoggerFactory.getLogger(name);
    }

    @Override
    public void info(String msg) {
        slf4jLogger.info(msg);
    }

    @Override
    public void warning(String msg) {
        slf4jLogger.warn(msg);
    }

    @Override
    public void severe(String msg) {
        slf4jLogger.error(msg);
    }

    @Override
    public void log(Level level, String msg) {
        if (level == Level.SEVERE) {
            slf4jLogger.error(msg);
        } else if (level == Level.WARNING) {
            slf4jLogger.warn(msg);
        } else if (level == Level.INFO) {
            slf4jLogger.info(msg);
        } else {
            slf4jLogger.debug(msg);
        }
    }
}
