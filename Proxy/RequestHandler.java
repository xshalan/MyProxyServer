package Proxy;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;


// Proxy.RequestHandler is thread that process requests of one client connection
public class RequestHandler extends Thread {


    Socket clientSocket;

    InputStream inFromClient;

    OutputStream outToClient;

    byte[] request = new byte[4096];


    private ProxyServer server;


    public RequestHandler(Socket clientSocket, Proxy.ProxyServer proxyServer) {


        this.clientSocket = clientSocket;


        this.server = proxyServer;

        try {
            clientSocket.setSoTimeout(5000);
            inFromClient = clientSocket.getInputStream();
            outToClient = clientSocket.getOutputStream();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override

    public void run() {

        /**
         * To do
         * Process the requests from a client. In particular,
         * (1) Check the request type, only process GET request and ignore others
         * (2) Write log.
         * (3) If the url of GET request has been cached, respond with cached content
         * (4) Otherwise, call method proxyServertoClient to process the GET request
         *
         */
        // print the request from client to console
        try {
            InputStreamReader in = new InputStreamReader(inFromClient);
            inFromClient.read(request);
            String[] requestLine = new String(request).split(" ");

            String method = requestLine[0];
            String hostUrl = requestLine[1];
            System.out.println("Method: " + method);
            System.out.println("Host URL: " + hostUrl);


            // TODO: (1) Check the request type, only process GET request and ignore others
            if (!method.equals("GET")) {
                return;
            }
            // put request information to request byte

            // TODO: (2) Write log.
            server.writeLog("GET " + hostUrl + " from " + clientSocket.getInetAddress() + " " + clientSocket.getPort() );

            // TODO: (3) If the url of GET request has been cached, respond with cached content
            String cachedFileName = server.getCache(hostUrl);
            if (cachedFileName != null) {
                sendCachedInfoToClient(cachedFileName);
                return;
            }
            // TODO: (4) Otherwise, call method proxyServertoClient to process the GET request
            proxyServertoClient(request);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }


    private void proxyServertoClient(byte[] clientRequest) {

        FileOutputStream fileWriter = null;
        Socket toWebServerSocket = null;
        InputStream inFromServer;
        OutputStream outToServer;

        // Create Buffered output stream to write to cached copy of file
        String fileName = "cached/" + generateRandomFileName() + ".dat";

        // to handle binary content, byte is used
        byte[] serverReply = new byte[4096];


        /*
          To do
          (1) Create a socket to connect to the web server (default port 80)
          (2) Send client's request (clientRequest) to the web server, you may want to use flush() after writing.
          (3) Use a while loop to read all responses from web server and send back to client
          (4) Write the web server's response to a cache file, put the request URL and cache file name to the cache Map
          (5) close file, and sockets.
         */
        try {

            String host = "";
            String line = new String(clientRequest);

            String[] requestLine = line.split(" ");
            host = requestLine[1];
            // remove http://
            host = host.substring(7);
            // remove any / after the host
            host = host.split("/")[0];


            // TODO: (1) Create a socket to connect to the web server (default port 80)
            toWebServerSocket = new Socket(host, 80);
            inFromServer = toWebServerSocket.getInputStream();
            outToServer = toWebServerSocket.getOutputStream();

            // TODO: (2) Send client's request (clientRequest) to the web server, you may want to use flush() after writing.
            outToServer.write(clientRequest);
            outToServer.flush();

            // TODO: (3) Use a while loop to read all responses from web server and send back to client
            int len = inFromServer.read(serverReply);
            while (len != -1) {
                outToClient.write(serverReply, 0, len);
                outToClient.flush();
                fileWriter = new FileOutputStream(fileName, true);
                fileWriter.write(serverReply, 0, len);
                len = inFromServer.read(serverReply);
            }
            // TODO: (4) Write the web server's response to a cache file, put the request URL and cache file name to the cache Map
            server.putCache(host, fileName);
            // TODO: (5) close file, and sockets.
            fileWriter.close();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    // Sends the cached content stored in the cache file to the client
    private void sendCachedInfoToClient(String fileName) {

        try {

            byte[] bytes = Files.readAllBytes(Paths.get(fileName));

            outToClient.write(bytes);
            outToClient.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {

            if (clientSocket != null) {
                clientSocket.close();
            }

        } catch (Exception e) {
            e.printStackTrace();

        }
    }


    // Generates a random file name
    public String generateRandomFileName() {

        String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-_";
        SecureRandom RANDOM = new SecureRandom();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 10; ++i) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }


}

