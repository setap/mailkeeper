package ru.atc.mailkeeper.mail;

import org.json.simple.JSONObject;
import ru.atc.mailkeeper.config.MailLogger;

import javax.mail.*;
import javax.mail.internet.MimeUtility;
import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: Stepan
 * Date: 24.09.14
 * Time: 21:04
 * To change this template use File | Settings | File Templates.
 */
public class MailBody {
    private final static Logger LOGGER = Logger.getLogger(MailLogger.class
            .getName());

    Message message = null;
    public boolean textIsHtml = false;
    String messageBody;
    String messageBodyInBase64;
    public String messageSubjectInBase64;
    SimpleDateFormat dateFormat;
    String fromAddresesString = "";
    String toAddresesString = "";
    String objectId = "";
    String mailIdInBase64;
    String direction;
    private String subject = "";

    public MailBody(Message m, String flag) throws IOException, MessagingException {
        message = m;
        direction = flag;
        Part p = (Part) m;

        String body = "";

        body = getText(p);
        subject = m.getSubject();

        if (body == null) {
            body = "Текст пистьма пустой";
//            System.out.println(body);
        }

        if (subject == null) {
            subject="";
        }
        System.out.println(subject);

        mailIdInBase64 = org.apache.commons.codec.binary.Base64.encodeBase64String(message.getHeader("Message-ID")[0].getBytes());
        messageBody = com.sun.org.apache.xml.internal.security.utils.Base64.encode(body.getBytes());
        messageBodyInBase64 = org.apache.commons.codec.binary.Base64.encodeBase64String(body.getBytes());
        messageSubjectInBase64 = org.apache.commons.codec.binary.Base64.encodeBase64String(subject.getBytes());
        dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

        Address[] a;

        // FROM
        if ((a = m.getFrom()) != null) {
            for (int j = 0; j < a.length; j++) {
                fromAddresesString += MimeUtility.decodeText(a[j].toString());
            }
        }

        Address[] to;

        // FROM
        if ((to = m.getAllRecipients()) != null) {
            for (int j = 0; j < to.length; j++) {
                toAddresesString += MimeUtility.decodeText(to[j].toString());
            }
        }

        Pattern pattern = Pattern.compile("((IM|SD)\\d+)\\s*");
        Matcher match = pattern.matcher(MimeUtility.decodeText(subject));

        if (match.find()) {
//            System.out.println(match.group());
            objectId = match.group();
        }


    }

    public String getText(Part p) throws
            MessagingException, IOException {

        if (p.isMimeType("text/*")) {
            String s = (String) p.getContent();
            textIsHtml = p.isMimeType("text/html");
            return s;
        }

        if (p.isMimeType("multipart/alternative")) {
            // prefer html text over plain text
            Multipart mp = (Multipart) p.getContent();


            String text = null;
            for (int i = 0; i < mp.getCount(); i++) {
                Part bp = mp.getBodyPart(i);


                if (bp.isMimeType("text/plain")) {
                    if (text == null)
                        text = getText(bp);
                    continue;
                } else if (bp.isMimeType("text/html")) {
                    String s = getText(bp);
                    if (s != null)
                        return s;
                } else {
                    return getText(bp);
                }
            }
            return text;
        } else if (p.isMimeType("multipart/*")) {

            Multipart mp = (Multipart) p.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                String s = getText(mp.getBodyPart(i));
                if (s != null)
                    return s;
            }
        }

        return null;
    }

    public String getJSONString() throws MessagingException, IOException {
        JSONObject obj=new JSONObject();
        LinkedHashMap mail = new LinkedHashMap();

        mail.put("ATCid", org.apache.commons.codec.binary.Base64.encodeBase64String(message.getHeader("Message-ID")[0].getBytes()));
        mail.put("Date", dateFormat.format(message.getSentDate()).toString());
        mail.put("From", org.apache.commons.codec.binary.Base64.encodeBase64String(fromAddresesString.getBytes()));
        mail.put("To", org.apache.commons.codec.binary.Base64.encodeBase64String(toAddresesString.getBytes()));
        mail.put("Subject", messageSubjectInBase64);
        mail.put("ObjectId", objectId);
        mail.put("Text", messageBodyInBase64);
        mail.put("Flag",direction);
        obj.put("atcmail",mail);

        StringWriter o = new StringWriter();
        obj.writeJSONString(o);

        LOGGER.info("GET MAIL Object:" + (String) mail.get("ObjectId") + " Sent date: " + (String) mail.get("Date") + " Subject:  " + subject);

        return o.toString();
    }

    public Boolean isObjectId() {
        if (!objectId.isEmpty() && objectId != null) {
            return true;
        }
        return false;
    }

    public String getMessageSubjectInBase64() {
        return messageSubjectInBase64;
    }

    public String getObjectId() {
        return objectId;
    }

    public  String getMailIdInBase64() {
        return mailIdInBase64;
    }
}
