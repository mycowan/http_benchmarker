package jp.sanix;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.logging.LoggingPermission;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class HTTPbenchmarkerServlet
 */
@WebServlet(description = "A servlet for benchmarking http connections",
        urlPatterns = { "/HTTPbenchmarkerServlet" })
public class HTTPbenchmarkerServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static enum SERVLET_QUERIES {
        publish, subscribe, message, qos, broker, // not needed
        port, // not needed
        clientId, subTopic, cleanSession, // not needed
        ssl, // not needed
        password, // not needed
        userName // not needed

    }

    /**
     * Default constructor.
     */
    public HTTPbenchmarkerServlet() {
        // TODO Auto-generated constructor stub
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {

        // Find out what is being requested by checking <code>q</code> parameter
        try {

            switch (SERVLET_QUERIES.valueOf(request.getParameter("q"))) {

            case publish:

                if (request.getParameter("clientId") != null) {

                    this.printWriter(request.getParameter(
                                            "clientId")
                            + ":"
                            + (System.currentTimeMillis() - Long.parseLong(request.getParameter("message"))),
                            response);

                } else {
                    this.printWriter("Failure! clientId - "
                            + request.getParameter("clientId")
                            + " - CANNOT be found", response);
                }

                break;

            case subscribe: // get a profile of a user

                if (request.getParameter("clientId") != null) {

                    this.printWriter(
                            "Subscriber is " + request.getParameter("clientId"),
                            response);

                } else {
                    this.printWriter("Failure! clientId - "
                            + request.getParameter("clientId")
                            + " - CANNOT be found", response);
                }

                break;

            default:
                System.err.println("Server Failure! query is "
                        + SERVLET_QUERIES.valueOf(request.getParameter("q")));
                this.printWriter("Server Failure! query is "
                        + SERVLET_QUERIES.valueOf(request.getParameter("q")),
                        response);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Server Connection Failure!" + e);
            this.printWriter(
                    "Server Failure! query was: " + request.getParameter("q"),
                    response);
        }
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        // TODO Auto-generated method stub
        doGet(request, response);
    }

    /**
     * Send <code>result</code> back to the client
     * 
     * @param String
     *            _result the message to be sent client
     * @param HttpServletResponse
     *            _response the HttpServletResponse sent by the client
     */
    private void printWriter(String result, HttpServletResponse response) {
        
        PrintWriter pw;
        try {
            pw = new PrintWriter(
                    new OutputStreamWriter(response.getOutputStream()));
            pw.print(result);
            pw.close();
        } catch (IOException e) {

            System.err.println("Server Failure! : getting PrintWriter" + e);
            e.printStackTrace();
        }
    }

}
