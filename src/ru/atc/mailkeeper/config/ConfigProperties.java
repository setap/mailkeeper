package ru.atc.mailkeeper.config;

import java.io.*;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: Stepan
 * Date: 24.09.14
 * Time: 16:22
 * To change this template use File | Settings | File Templates.
 */

public class ConfigProperties {
    Properties prop;
    OutputStream out;
    String filename;

    public  ConfigProperties(String filename) throws IOException {
        prop = new Properties();
        this.filename = filename;
    }

    public void set(String name, String value) throws IOException {
        OutputStream output = null;

        try {
            output = new FileOutputStream(filename);
            prop.setProperty(name,value);
            prop.store(output, null);

        } catch (IOException io) {
            io.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
   }

    public String get(String name) throws IOException {
        InputStream stream = null;
        InputStreamReader reader = null;

        try {
            stream = new FileInputStream(new File(filename));
            reader = new InputStreamReader(stream,"UTF-8");
            prop = new Properties();
            prop.load(reader);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            stream.close();
            if (reader != null) {
                reader.close();
            }
        }

        return prop.getProperty(name);
    }
}
