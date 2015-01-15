package ru.atc.mailkeeper.config;

/**
 * Created with IntelliJ IDEA.
 * User: Stepan
 * Date: 26.09.14
 * Time: 10:32
 * To change this template use File | Settings | File Templates.
 */

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.*;


public class LoggerTest {
    private final static Logger logger = Logger.getLogger(LoggerTest.class.getName());
    private static FileHandler fh = null;

    public static void init(){
        try {
            fh=new FileHandler("loggerExample.log", true );
        } catch (IOException e) {
            e.printStackTrace();
        }
        Logger l = Logger.getLogger("");
        fh.setFormatter(new SimpleFormatter());
        l.addHandler(fh);
        l.setLevel(Level.CONFIG);
    }

    public static void main(String[] args) {
        LoggerTest.init();

        logger.log(Level.INFO, "message 1");
        logger.log(Level.SEVERE, "message 2");
        logger.log(Level.FINE, "message 3");
    }
}
