package ru.atc.mailkeeper;

import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import ru.atc.mailkeeper.config.ConfigProperties;
import ru.atc.mailkeeper.config.MailLogger;
import javax.mail.MessagingException;
import java.io.*;
import java.text.ParseException;
import java.util.Properties;
import java.util.logging.Logger;


public class Run {
    private final static Logger log = Logger.getLogger(MailLogger.class.getName());
    public static void main(String[] args) throws IOException, Base64DecodingException, MessagingException, ParseException {

        MailLogger.setup();
        log.info("Absolute path is " + new File("").getAbsolutePath());

        ConfigProperties p = new ConfigProperties(System.getenv("ATC_MAIL") + "/config.properties");
        final String value = System.getenv("ATC_MAIL");

        log.info("Enviroment variable $ATC_MAIL: " + value);

            CheckMails.check(p.get("HOST"),p.get("PORT"), "pop3", p.get("LOGIN"), p.get("PASSWORD"),"INBOX");
            CheckMails.check(p.get("HOST"),p.get("PORT"), "pop3", p.get("LOGIN"), p.get("PASSWORD"),"Отправленные");
    }
}
