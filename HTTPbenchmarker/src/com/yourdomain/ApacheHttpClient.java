package com.yourdomain;

/**
 * @author Mark Cowan
 * https://github.com/mycowan/http_benchmark.git 
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import bb.util.MemoryMeasurer;

public class ApacheHttpClient {

    private static int maxNumClients = 100;
    private static int wait          = 5000;
    private static int loops         = 5;

    protected final String  ACTION   = "publish";
    protected static String protocol = "http";
    protected static String server   = "localhost";
    // protected static String server = "12.34.56.78";
    protected static String port     = "8080";
    protected static String servlet  = "HTTPbenchmarkerServlet";
    protected static String clientId = "JavaSamplePub";

    private static ArrayList<OutMessageRunnable> outMsgTests   = new ArrayList<OutMessageRunnable>();
    private static ArrayList<Thread>             outMsgThreads = new ArrayList<Thread>();

    private static ConcurrentMap<Long, Integer>    timeTrail     = new ConcurrentHashMap<Long, Integer>();
    private static ConcurrentMap<Integer, Integer> timeTrailAvgs = new ConcurrentHashMap<Integer, Integer>();

    private static String topic = "bench/mark/test/";

    private static Integer sum = 0;

    static CloseableHttpClient httpClient = HttpClients.createDefault();

    // CONSTRUCTOR
    private ApacheHttpClient() {
    }

    // Creates a Singleton of ApacheHttpClient
    private static class ApacheHttpClientHelper {
        private static final ApacheHttpClient INSTANCE = new ApacheHttpClient();

    }

    public static ApacheHttpClient getInstance() {
        return ApacheHttpClientHelper.INSTANCE;
    }

    public static void main(String[] args) throws IOException {

        // Parse the arguments -
        for (int i = 0; i < args.length; i++) {
            // Check this is a valid argument
            if (args[i].length() == 2 && args[i].startsWith("-")) {
                char arg = args[i].charAt(1);
                // Handle arguments that take no value
                switch (arg) {
                case 'h':
                case '?':
                    printHelp();
                    return;
                }

                // Now handle arguments that have a value and
                // check each value is valid
                if (i == args.length - 1 || args[i + 1].charAt(0) == '-') {
                    System.out
                            .println("Missing value for argument: " + args[i]);
                    printHelp();
                    return;
                }
                switch (arg) {
                case 't':
                    String tmp = args[++i];
                    if (tmp.lastIndexOf('/') == (tmp.length() - 1)) {
                        setTopic(tmp);
                    } else {

                        System.out.println(
                                "Missing final \"/\" for: " + args[i - 1]);
                        printHelp();
                        return;
                    }

                    break;
                case 'l':
                    setLoops(Integer.parseInt(args[++i]));
                    break;
                case 'm':
                    setMaxNumClients(Integer.parseInt(args[++i]));
                    break;
                case 's':
                    setServer(args[++i]);
                    break;
                case 'p':
                    setPort(args[++i]);
                    break;
                case 'w':
                    setWait(Integer.parseInt(args[++i]));
                    break;
                case 'i':
                    setClientId(args[++i]);
                    break;
                case 'v':
                    setServlet(args[++i]);
                    break;
                default:
                    System.out.println("Unrecognised argument: " + args[i]);
                    printHelp();
                    return;
                }
            } else {
                System.out.println("Unrecognised argument: " + args[i]);
                printHelp();
                return;
            }
        }

        try {
            cleanJvm();
            warmupJvm();
        } catch (Exception e) {

            e.printStackTrace();
        }

        for (int looper = 0; looper < getLoops(); looper++) {

            System.out.println("\nLoop No. " + (looper + 1));

            setOutMsgThreads(new ArrayList<Thread>());

            setTimeTrail(new ConcurrentHashMap<Long, Integer>());

            System.out.print("Setting up messages ...");

            String id = "";

            for (int i = 0; i < getMaxNumClients(); i++) {
                id = getSaltString();
                setupOutMsgClient(id);
                System.out.print(".");

            }

            System.out
                    .println("\nWaiting to send and receive all messages ...");

            Iterator<Thread> outMsgThreadIterator = getOutMsgThreads()
                    .iterator();
            while (outMsgThreadIterator.hasNext()) {
                outMsgThreadIterator.next().start();
            }

            // Need to wait for all the messages to be received
            try {
                Thread.sleep(getWait());
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }

            System.out.println("Average time for loop " + (looper + 1) + " : "
                    + averageTimeTrial(looper) + "ms");
        }

        System.out.println("\n================================");
        System.out.println("Total Average time for all " + loops + " loops : "
                + totalAvgTimeTrials() + "ms");

        try {
            // cleanJvm();
        } catch (Exception e) {

            e.printStackTrace();
        }

    }

    public static Integer averageTimeTrial(int loop) {

        ApacheHttpClient.getInstance();
        Set<?> timeTrailSet = ApacheHttpClient.getTimeTrail().entrySet();

        Iterator<?> timeTrialIterator = timeTrailSet.iterator();

        Map.Entry thisTimeEntry;
        Long thisTimeTrial;
        Integer thisMessageTime;

        while (timeTrialIterator.hasNext()) {

            thisTimeEntry = (Map.Entry) timeTrialIterator.next();
            thisTimeTrial = (Long) thisTimeEntry.getKey();
            thisMessageTime = (Integer) thisTimeEntry.getValue();

            ApacheHttpClient
                    .setSum(ApacheHttpClient.getSum() + thisMessageTime);
        }

        System.out.println(
                "Messages successfully sent from client to server via HTTP "
                        + (((double) timeTrailSet.size()
                                / (double) maxNumClients) * 100)
                        + "%");

        ApacheHttpClient.getTimeTrailAvgs().put(loop,
                (ApacheHttpClient.getSum() / timeTrailSet.size()));

        Integer sum = ApacheHttpClient.getSum();

        ApacheHttpClient.setSum(0);

        return (sum / timeTrailSet.size());

    }

    /**
     * state by aggressively performing object finalization and garbage
     * collection.
     */
    protected static void cleanJvm() {
        MemoryMeasurer.restoreJvm();
    }

    public String getClientId() {
        return ApacheHttpClient.clientId;
    }

    public static void setClientId(String clientId) {
        ApacheHttpClient.clientId = clientId;
    }

    public static int getLoops() {
        return ApacheHttpClient.loops;
    }

    private static void setLoops(int loops) {
        ApacheHttpClient.loops = loops;
    }

    public static int getMaxNumClients() {
        return ApacheHttpClient.maxNumClients;
    }

    public static void setMaxNumClients(int clients) {
        ApacheHttpClient.maxNumClients = clients;
    }

    public static String getPort() {
        return port;
    }

    public static void setPort(String port) {
        ApacheHttpClient.port = port;
    }

    public static String getProtocol() {
        return protocol;
    }

    public static void setProtocol(String protocol) {
        ApacheHttpClient.protocol = protocol;
    }

    protected static String getSaltString() {
        String SALTCHARS = "0123456789";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 9) {
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();

        return saltStr;

    }

    public static String getServlet() {
        return servlet;
    }

    public static void setServlet(String servlet) {
        ApacheHttpClient.servlet = servlet;
    }

    public static ConcurrentMap<Long, Integer> getTimeTrail() {
        return timeTrail;
    }

    public static ConcurrentMap<Integer, Integer> getTimeTrailAvgs() {
        return timeTrailAvgs;
    }

    public static void setTimeTrailAvgs(
            ConcurrentMap<Integer, Integer> timeTrailAvgs) {
        ApacheHttpClient.timeTrailAvgs = timeTrailAvgs;
    }

    public static String getTopic() {
        return topic;
    }

    private static void setTopic(String topic) {
        ApacheHttpClient.topic = topic;
    }

    public static ArrayList<Thread> getOutMsgThreads() {
        return outMsgThreads;
    }

    protected static void setupOutMsgClient(String id) {
        OutMessageRunnable outMsg = new OutMessageRunnable();
        OutMessageRunnable.setProtocol(getProtocol());
        outMsg.setServer(getServer());
        outMsg.setPort(getPort());
        OutMessageRunnable.setServlet(getServlet());
        outMsg.setAction("publish");
        outMsg.setClientId(id);
        outMsg.setPubTopic(getTopic() + id);
        outMsg.setMessage(String.valueOf(System.currentTimeMillis()));
        outMsgTests.add(outMsg);
        outMsgThreads.add(
                new Thread((Runnable) outMsgTests.get(outMsgTests.size() - 1)));
    }

    public static void setOutMsgThreads(ArrayList<Thread> pubThreads) {
        ApacheHttpClient.outMsgThreads = pubThreads;
    }

    public static String getServer() {
        return server;
    }

    public static void setServer(String server) {
        ApacheHttpClient.server = server;
    }

    public static Integer getSum() {
        return sum;
    }

    public static void setSum(Integer sum) {
        ApacheHttpClient.sum = sum;
    }

    public static void setTimeTrail(ConcurrentMap<Long, Integer> timeTrail) {
        ApacheHttpClient.timeTrail = timeTrail;
    }

    public static Integer totalAvgTimeTrials() {

        ApacheHttpClient.getInstance();
        Set<?> timeAvgsSet = ApacheHttpClient.getTimeTrailAvgs().entrySet();

        Iterator<?> timeAvgsIterator = timeAvgsSet.iterator();

        Map.Entry thisAvgEntry;
        int thisTrialAvg;
        Integer thisAvg = 0, sum = 0;

        while (timeAvgsIterator.hasNext()) {

            thisAvgEntry = (Map.Entry) timeAvgsIterator.next();
            thisTrialAvg = (int) thisAvgEntry.getKey();
            thisAvg = (Integer) thisAvgEntry.getValue();

            sum = sum + thisAvg;
        }

        // System.out.println("size of map " + timeAvgsSet.size());

        return (sum / timeAvgsSet.size());

    }

    public static int getWait() {
        return wait;
    }

    public static void setWait(int wait) {
        ApacheHttpClient.wait = wait;
    }

    /**
     * Credit to Brent Boyer In order to give hotspot optimization a chance to
     * complete, the test is executed many times (with no recording of the
     * execution time). This phase lasts for 3 seconds
     * 
     * @throws Exception
     */
    protected static void warmupJvm() throws Exception {
        cleanJvm();

        System.out.print(
                "Performing many executions of task to fully warmup the JVM...");

        Long t = System.currentTimeMillis() + 3000;

        while (System.currentTimeMillis() < t) {

            setOutMsgThreads(new ArrayList<Thread>());

            String id = "";

            for (int i = 0; i < maxNumClients; i++) {
                id = getSaltString();
                setupOutMsgClient(id);
                System.out.print(".");

            }
            // Need to wait for each messages to be ready

            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            System.out.print(".");
        }

        Iterator<Thread> outMsgThreadIterator = getOutMsgThreads().iterator();
        while (outMsgThreadIterator.hasNext()) {
            outMsgThreadIterator.next().start();
        }

        // Need to wait for all the messages to be received
        try {
            Thread.sleep(getWait());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }

        System.out.println("\nWarmup done.");

    }

    /****************************************************************/
    /* End of ApacheHttpClient methods */
    /****************************************************************/

    static void printHelp() {
        System.out.println("Syntax:\n\n"
                + "    Sample [-h] [-q <true|false>] [-t <topic>] [-m <1234>]\n"
                + "           [-l <1234>] -s <hostname|IP address>]\n"
                + "           [-p <port>] [-v <servletname>] [-i <clientId>]\n"
                + "           [-w <1234>] \n\n"
                + "    -h  Print this help text and quit\n"
                + "    -t  Publish/subscribe to <topic> instead of the default\n"
                + "            (topic: \"bench/mark/test/\")\n"
                + "            Note: a final \"/\" is required for these tests\n"
                + "    -m  Use this max no. of clients - default (100)\n"
                + "    -l  Use this max no. of test loops - default (5)\n"
                + "    -s  Use this name/IP address instead of the default (localhost)\n"
                + "    -p  Use this port instead of the default (8080)\n"
                + "    -v  Use this servlet instead of the default (HTTPbenchmarkerServlet)\n"
                + "    -i  Use this client instead of the default (JavaSamplePub)\n"
                + "    -w  Use this wait between test loops period \n instead of the default (5000ms)\n\n"
                + "Delimit strings containing spaces with \"\"\n\n");
    }

}
