package ru.atc.mailkeeper;

import com.sun.mail.imap.IMAPFolder;
import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.*;

import ru.atc.mailkeeper.config.ConfigProperties;
import ru.atc.mailkeeper.config.MailLogger;
import ru.atc.mailkeeper.mail.Mail;

public class CheckMails {

    private final static Logger LOGGER = Logger.getLogger(MailLogger.class
            .getName());

//http://www.oracle.com/technetwork/java/javamail/faq/index.html#mainbody
//http://www.oracle.com/technetwork/java/javamail/faq/index.html#examples

// Get format text from mail body
    public static void check(String host, String port, String storeType, String user,
                             String password, String mailbox) throws IOException, MessagingException, Base64DecodingException, ParseException {

        String prefix = "IN_DATE";

        if (mailbox.equals("Отправленные")) {
            prefix = "OUT_DATE";
        } else if (mailbox.equals("INBOX")) {
            prefix = "IN_DATE";
        }

        ConfigProperties config = new ConfigProperties(System.getenv("ATC_MAIL") + "/config.properties");
        ConfigProperties configSync = new ConfigProperties(System.getenv("ATC_MAIL") + "/sync.out");

        IMAPFolder emailFolder;
        Store store;

        //create properties field
        Properties props = new Properties();
        props.setProperty("mail.imaps.host", host);
        props.setProperty("mail.imaps.port", port);

        Session emailSession = Session.getDefaultInstance(props);
        store = emailSession.getStore("imap");
        store.connect(host, user, password);

        //create the folder object and open it
        emailFolder = (IMAPFolder) store.getFolder(mailbox);
        emailFolder.open(Folder.READ_ONLY);

        Message[] messages = emailFolder.getMessages();
        List<Message> newMessages = new ArrayList<Message>();

        LOGGER.info("Number messages in mailbox " + prefix + " " + messages.length);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date lastDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(configSync.get(prefix));

        for (int i = messages.length - 1; i >= 0; i-- ) {
            Date sentDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                    .parse(dateFormat.format(messages[i].getSentDate()).toString());
            if (sentDate.after(lastDate))  {
//                System.out.println("sentdate" + sentDate);
//                System.out.println("lasdate" + lastDate);
                newMessages.add(messages[i]);
            }
        }

        LOGGER.info("Count new messages " + prefix + " " + newMessages.size());

        for (int i = newMessages.size()-1; i >= 0; i--) {
            if (newMessages.get(i).getHeader("Message-ID") != null) {
                Mail mail = new Mail(newMessages.get(i), prefix);
                if (mail.send()) {
                    configSync.set(prefix, dateFormat.format(newMessages.get(i).getSentDate()).toString());
                }
            }
        }

        emailFolder.close(false);
        store.close();
    }

}