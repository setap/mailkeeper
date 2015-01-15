package ru.atc.mailkeeper.attachment;

import com.sun.mail.util.BASE64DecoderStream;
import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;
import org.apache.commons.io.IOUtils;
import ru.atc.mailkeeper.config.ConfigProperties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: Stepan
 * Date: 21.09.14
 * Time: 19:23
 * To change this template use File | Settings | File Templates.
 */

public class Attachment {
    URL url;
    String authString;
    Message message = null;
    DataOutputStream out = null;
    DataInputStream in = null;
    HttpURLConnection conn;
    String authEncBytes;

    public Attachment(Message m) throws IOException, Base64DecodingException {
        message = m;

    }

    public void send(String number, String filename, byte[] data) throws IOException {
        ConfigProperties config = new ConfigProperties(System.getenv("ATC_MAIL") + "/config.properties");
        authString = config.get("AUTH") + ":" + config.get("AUTH_PASS");
        url = new URL("http://" + config.get("REST_HOST") + ":" + config.get("REST_PORT") + config.get("REST_PATH")  + config.get("REST_SOURCE") + "/" + number + "/attachments");
        conn = (HttpURLConnection) url.openConnection();

        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestMethod(config.get("REST_METHOD"));
        conn.setRequestProperty("content-type", "application/octet-stream");
        conn.setRequestProperty("Content-Disposition","attachment; filename=" + filename);
        conn.setRequestProperty("Accept-Charset", "UTF-8");
        conn.setRequestProperty("Content-Language", "ru-RU");
        conn.setRequestProperty("Charset", "UTF-8");

        authEncBytes = Base64.encode(authString.getBytes());

        conn.setRequestProperty("Authorization", "Basic " + authEncBytes);

        out = new DataOutputStream(conn.getOutputStream());

        out.write(data);
        out.flush();
        out.close();

        String response = null;

        in = new DataInputStream(conn.getInputStream());

        int ch;
        StringBuilder sb = new StringBuilder();
        while((ch = in.read())!= -1)
            sb.append((char)ch);
//        System.out.println(sb);
    }

    public void addAttachment(Boolean isObjectId, String number) throws IOException, MessagingException, Base64DecodingException {

        if (message.getContent() instanceof MimeMultipart) {
            MimeMultipart mm = (MimeMultipart) message.getContent();
            for (int i = 0; i < mm.getCount(); i++) {
                MimeBodyPart bp = (MimeBodyPart) mm.getBodyPart(i);
                if (bp.getDisposition() != null && bp.getDisposition().equalsIgnoreCase(Part.ATTACHMENT)) {
                    String fName = bp.getFileName();
                    if (fName == null) {
                        fName = "file";
                    }
                    String filename = MimeUtility.decodeText(fName);
                    if (bp.getContent() instanceof BASE64DecoderStream)
                    {
                        BASE64DecoderStream base64DecoderStream = (BASE64DecoderStream) bp.getContent();
                        byte[] byteArray = IOUtils.toByteArray(base64DecoderStream);
                        if (isObjectId) {
//                            System.out.println("ATTACH SEND");
                            send(number,org.apache.commons.codec.binary.Base64.encodeBase64String(filename.getBytes()),byteArray);
                        }
                    }
                }
            }
        }
    }
}
