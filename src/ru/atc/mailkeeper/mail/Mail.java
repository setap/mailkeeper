package ru.atc.mailkeeper.mail;

import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;
import ru.atc.mailkeeper.attachment.Attachment;
import ru.atc.mailkeeper.config.ConfigProperties;
import ru.atc.mailkeeper.config.MailLogger;

import javax.mail.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: Stepan
 * Date: 21.09.14
 * Time: 19:23
 * To change this template use File | Settings | File Templates.
 */

public class Mail {
    private final static Logger LOGGER = Logger.getLogger(MailLogger.class
            .getName());
    URL url;
    String authString;
    DataOutputStream out = null;
    DataInputStream in = null;
    HttpURLConnection conn;
    Message message;
    String authEncBytes;
    SimpleDateFormat dateFormat;
    ConfigProperties config;
    String direction;

    public Mail(Message m, String direction) throws Base64DecodingException, MessagingException, IOException {
        message = m;
        this.direction = direction;
        config = new ConfigProperties(System.getenv("ATC_MAIL") + "/config.properties");
        authString = config.get("AUTH") + ":" + config.get("AUTH_PASS");
        url = new URL("http://" + config.get("REST_HOST") + ":" + config.get("REST_PORT") + config.get("REST_PATH") + config.get("REST_SOURCE"));
        conn = (HttpURLConnection) url.openConnection();

        dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestMethod(config.get("REST_METHOD"));
        conn.setRequestProperty("content-type", "application/json");

        authEncBytes = Base64.encode(authString.getBytes());

    }
    public Boolean send() throws MessagingException, Base64DecodingException, IOException {
        conn.setRequestProperty("Authorization", "Basic " + authEncBytes);

        MailBody body = new MailBody(message, direction);

        out = new DataOutputStream(conn.getOutputStream());



        if (body.isObjectId()) {
            out.writeBytes(body.getJSONString());
            out.flush();
            out.close();

            String response = null;

            try {
                in = new DataInputStream(conn.getInputStream());
                int ch;
                StringBuilder sb = new StringBuilder();
                while((ch = in.read())!= -1) {
                    sb.append((char) ch);
                }
                Pattern pat = Pattern.compile("added");
                Matcher mat = pat.matcher(sb);
                if (mat.find()) {
                    Attachment attach = new Attachment(message);
                    attach.addAttachment(body.isObjectId(),body.getMailIdInBase64());
                    return true;
                }
            } catch (IOException e) {
                LOGGER.severe(e.getMessage());
                return false;
            }
        }
        return false;
    }
}
