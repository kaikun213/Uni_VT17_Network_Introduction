package mj223gn_jh223gj_assign2;

import mj223gn_jh223gj_assign2.exceptions.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.time.Duration;
import java.time.Instant;

public class TCPServer {

    // Root path of the server to search requested files (URL) - relative to working directory
    public static final String BASEPATH = "resources";
    public static final int MYPORT = 4950;
    public static final double HTTPVERSION = 2.0;

    public static void main(String[] args) throws IOException {
        TCPServer server = new TCPServer();
        server.run(args);
    }

    public void run(String[] args) {
        try {
            /* Create Socket */
            ServerSocket socket = new ServerSocket(MYPORT);
			
			/* Endless loop waiting for client connections */
            while (true) {
				/* Open new thread for each new client connection */
                new Thread(new ConnectionHandler(socket.accept())).start();
				
				/* Print out to investigate open threads */
                printThreadsInfo();
            }
        } catch (IOException e) {
        }
    }

    /* Prints the status of the currently active threads in the console */
    private void printThreadsInfo() {
        System.out.println("******************************************");
        System.out.println("Active Threads:" + Thread.activeCount());
        Thread[] threads = new Thread[Thread.activeCount()];
        Thread.enumerate(threads);
        for (Thread t : threads)
            System.out.println(t.toString());
        System.out.println("******************************************");
    }

    /* Handles client connection */
    class ConnectionHandler implements Runnable {
        private Socket connection;

        public ConnectionHandler(Socket connection) {
            this.connection = connection;
        }

        @Override
        public void run() {

            HTTPResponseFactory factory = new HTTPResponseFactory();

            try {
                HTTPReader parser = new HTTPReader(connection.getInputStream());
                try {
					/* Parse HTTP Request from input stream */
                    HTTPRequest request = parser.read();
                    HTTPResponse response = factory.getHTTPResponse(request);

                    // Sends response as constructed stream
                    Instant before = Instant.now();
                    response.getHeaderAsByteArrayOutputStream().writeTo(connection.getOutputStream());
                    if (response.hasResponseBody())
                        response.getBodyAsByteArrayOutputStream().writeTo(connection.getOutputStream());
                    System.out.println("PERFORMANCE CHECK: Time for STREAM: " + Duration.between(before, Instant.now()).toNanos());


					/* Print status of request */

                    System.out.printf("TCP echo request from %s", connection.getInetAddress().getHostAddress());
                    System.out.printf(" using port %d", connection.getPort());
                    System.out.printf(" handled from thread %d; Method-Type: <%s>,", Thread.currentThread().getId(), request.getType());
                    System.out.printf(" URL: %s\n", request.getUrl());
                    if (request.getRequestBody() != null)
                        System.out.println(" Content: " + new String(request.getRequestBody()));
				    
				    /* Print status of response */

                    System.out.printf("TCP echo response from %s", connection.getLocalAddress());
                    System.out.printf(" using port %d", connection.getLocalPort());
                    System.out.printf(" Headers: %s \n", response.toString());

                } catch (UnsupportedMediaTypeException e) {
                    HTTPResponse response = factory.getErrorResponse(HTTPResponse.HTTPStatus.UnsupportedMediaType);
                    System.out.println("---- Unsupported MediaType Exception ----\n" + e.getMessage());
                    connection.getOutputStream().write(response.toBytes());
                } catch (HTTPVersionIsNotSupportedException e) {
                    HTTPResponse response = factory.getErrorResponse(HTTPResponse.HTTPStatus.HTTPVersionNotSupported);
                    System.out.println("---- HTTP Version Error ----\n" + e.getMessage());
                    connection.getOutputStream().write(response.toBytes());
                } catch (InvalidRequestFormatException e) {
                    HTTPResponse response = factory.getErrorResponse(HTTPResponse.HTTPStatus.BadRequest);
                    System.out.println("---- Bad Request Exception ----\n" + e.getMessage());
                    connection.getOutputStream().write(response.toBytes());
                } catch (ResourceNotFoundException e) {
                    HTTPResponse response = factory.getErrorResponse(HTTPResponse.HTTPStatus.NotFound);
                    System.out.println("---- Not Found Exception ----\n" + e.getMessage());
                    connection.getOutputStream().write(response.toBytes());
                } catch (AccessRestrictedException e) {
                    HTTPResponse response = factory.getErrorResponse(HTTPResponse.HTTPStatus.Forbidden);
                    System.out.println("---- Forbidden Exception ----\n" + e.getMessage());
                    connection.getOutputStream().write(response.toBytes());
                } catch (HTTPMethodNotImplementedException e) {
                    HTTPResponse response = factory.getErrorResponse(HTTPResponse.HTTPStatus.NotImplemented);
                    System.out.println("---- Method not implemented ----\n" + e.getMessage());
                    connection.getOutputStream().write(response.toBytes());
                } catch (ContentLengthRequiredException e) {
                    HTTPResponse response = factory.getErrorResponse(HTTPResponse.HTTPStatus.LengthRequired);
                    System.out.println("---- Content length required ----\n" + e.getMessage());
                    connection.getOutputStream().write(response.toBytes());
                } catch (RequestURIToLongException e) {
                    HTTPResponse response = factory.getErrorResponse(HTTPResponse.HTTPStatus.URIToLong);
                    System.out.println("---- Request URI to long ----\n" + e.getMessage());
                    connection.getOutputStream().write(response.toBytes());
                } catch (Exception e) {
                    e.printStackTrace();
                    HTTPResponse response = factory.getErrorResponse(HTTPResponse.HTTPStatus.InternalServerError);
                    System.out.println("---- Internal server error 500 ----\n" + e.getMessage());
                    e.printStackTrace();
                    connection.getOutputStream().write(response.toBytes());
                }

                System.out.printf("TCP Connection from %s using port %d handled by thread %d is closed.\n", connection.getInetAddress().getHostAddress(), connection.getPort(), Thread.currentThread().getId());
                connection.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


}
