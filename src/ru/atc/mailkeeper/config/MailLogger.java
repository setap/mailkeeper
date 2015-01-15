package ru.atc.mailkeeper.config;

/**
 * Created with IntelliJ IDEA.
 * User: Stepan
 * Date: 25.09.14
 * Time: 17:02
 * To change this template use File | Settings | File Templates.
 */
import java.io.IOException;
import java.util.logging.*;

public class MailLogger {
    static private FileHandler fileTxt;
    static private SimpleFormatter formatterTxt;

    static private FileHandler fileHTML;
    static private Formatter formatterHTML;

    static public void setup() throws IOException {
        Logger logger = Logger.getLogger("");

        try {
            fileTxt=new FileHandler(System.getenv("ATC_MAIL") + "/log.out", true );
        } catch (IOException e) {
            e.printStackTrace();
        }

        logger.setLevel(Level.INFO);
        fileHTML = new FileHandler(System.getenv("ATC_MAIL") + "/log.html", true);

        // create a TXT formatter
        formatterTxt = new SimpleFormatter();
        fileTxt.setFormatter(formatterTxt);
        logger.addHandler(fileTxt);

        // create an HTML formatter
        formatterHTML = new LogFormatter();
        fileHTML.setFormatter(formatterHTML);
        logger.addHandler(fileHTML);
    }
}

