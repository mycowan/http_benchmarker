package jp.sanix;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class OutMessageRunnable implements Runnable {

    protected String        action   = "publish";
    protected String        message  = null;
    protected static String protocol = "http";
    protected static String server   = "localhost";
    // protected static String server = "52.69.29.92";
    protected static String port     = "8080";
    protected static String servlet  = "HTTPbenchmarkerServlet";
    protected static String clientId = "JavaSamplePub";
    protected static String pubTopic = "bench/mark/test";

    public void run() {

        connect("q=" + this.getAction() + "&clientId=" + this.getClientId()
                + "&message=" + this.getMessage());
    }

    /**
     * @param param
     *            The String message to be sent to the server
     * @return String
     * @Smile.property name="connect"
     */
    protected static String connect(String param) {

        // Create String to return
        String str = "";

        /**
         * Call server with param to get response String from server
         */
        InputStream inputStream = getInputStreamFromUrl(param);

        // Create byte array to receive response inputStreamFromURL
        byte[] responseArray = new byte[1024];

        // needed to if inputStreamFromURL is not empty
        int inputStreamSize;
        try {
            inputStreamSize = inputStream.read(responseArray);
            inputStream.close();

            if (inputStreamSize > 0) {
                str = new String(responseArray, 0, inputStreamSize);

            }

        } catch (IOException e) {

            e.printStackTrace();
        }

        ApacheHttpClient.getTimeTrail()
                .put(Long.parseLong(
                        str.substring(0, str.indexOf(":"))),
                Integer.parseInt(
                        str.substring(str.indexOf(":") + 1, str.length())));

        return str;
    }

    /**
     * Uploader of the message to server
     * 
     * @param serverCall
     *            The message to upload.
     * @return InputStream
     */
    public static InputStream getInputStreamFromUrl(String serverCall) {

        InputStream content = null;

        try {

            String userAgent = System.getProperty("http.agent");

            HttpGet httpGet = new HttpGet(protocol + "://" + server + ":" + port
                    + "/" + servlet + "?" + serverCall);
            httpGet.setHeader("User-Agent", userAgent);

            // Execute HTTP Get Request
            HttpResponse response = getHttpclient().execute(httpGet);

            content = response.getEntity().getContent();

        } catch (Exception e) {
            e.printStackTrace();

        }
        return content;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static String getProtocol() {
        return protocol;
    }

    public static void setProtocol(String protocol) {
        OutMessageRunnable.protocol = protocol;
    }

    public static String getServlet() {
        return OutMessageRunnable.servlet;
    }

    public static void setServlet(String servlet) {
        OutMessageRunnable.servlet = servlet;
    }

    public String getServer() {
        return OutMessageRunnable.server;
    }

    public void setServer(String broker) {
        OutMessageRunnable.server = broker;
    }

    public String getPort() {
        return OutMessageRunnable.port;
    }

    public void setPort(String port) {
        OutMessageRunnable.port = port;
    }

    public static CloseableHttpClient getHttpclient() {
        return HttpClients.createDefault();
    }

    public String getClientId() {
        return OutMessageRunnable.clientId;
    }

    public void setClientId(String clientId) {
        OutMessageRunnable.clientId = clientId;
    }

    public String getPubTopic() {
        return OutMessageRunnable.pubTopic;
    }

    public void setPubTopic(String pubTopic) {
        OutMessageRunnable.pubTopic = pubTopic;
    }

}
