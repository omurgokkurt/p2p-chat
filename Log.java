package com.company;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Log {

    public static Log instance = new Log();
    public Logger logger;

    public static Log getInstance() {
        return instance;
    }

    public Log() {
        FileHandler fh;
        try {
            logger = Logger.getLogger("MyLog");
            logger.setUseParentHandlers(false);
            logger.setLevel(Level.INFO);
            // This block configures the logger with handler and formatter
            fh = new FileHandler("LogFile%u.txt");
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
