package Proxy;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;



public class ProxyServer {

    //cache is a Map: the key is the URL and the value is the file name of the file that stores the cached content
    Map<String, String> cache;

    ServerSocket proxySocket;

    String logFileName = "log.txt";

    public static void main(String[] args) {

        new ProxyServer().startServer(Integer.parseInt(args[0]));
    }

    void startServer(int proxyPort) {

        cache = new ConcurrentHashMap<>();

        // create the directory to store cached files.
        File cacheDir = new File("cached");
        if (!cacheDir.exists() || (cacheDir.exists() && !cacheDir.isDirectory())) {
            cacheDir.mkdirs();
        }

        /**
         * To do:
         * create a serverSocket to listen on the port (proxyPort)
         * Create a thread (Proxy.RequestHandler) for each new client connection
         * remember to catch Exceptions!
         *
         */
        // create a serverSocket to listen on the port (proxyPort)
        try {
            proxySocket = new ServerSocket(proxyPort);
            System.out.println("ProxyServer is listening on port " + proxyPort);
            while (true) {
                new RequestHandler(proxySocket.accept(), this).start();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }



    public String getCache(String hashcode) {
        return cache.get(hashcode);
    }

    public void putCache(String hashcode, String fileName) {
        cache.put(hashcode, fileName);
    }

    public synchronized void writeLog(String info) {

        /**
         * To do
         * write string (info) to the log file, and add the current time stamp
         * e.g. String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
         *
         */

        // create log.txt if not exists
        File logFile = new File(logFileName);
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        // TODO: write string (info) to the log file, and add the current time stamp
        String timestamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
        try {
            Files.write(Paths.get(logFileName), (timestamp + " " + info + "\n").getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}

