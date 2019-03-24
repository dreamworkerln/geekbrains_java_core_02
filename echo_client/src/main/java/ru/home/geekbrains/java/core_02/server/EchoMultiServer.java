package ru.home.geekbrains.java.core_02.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class EchoMultiServer {


    private static class EchoClientHandler extends Thread {

        private int cid; // client id
        private Socket socket;
        private BiConsumer<Integer,String> messageHandler;

        private PrintWriter out;
        private BufferedReader in;


        public EchoClientHandler(int cid, Socket socket, BiConsumer<Integer,String> messageHandler) throws Exception {

            this.cid = cid;
            this.socket = socket;
            this.messageHandler = messageHandler;

            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }


        public void run() {

            try {

                String input;

                while ((input = in.readLine()) != null) {

                    System.out.println("Client " + cid + " in: " + input);

                    if (input.equals(".")) {

                        out.println("bye");
                        break;
                    }
                    messageHandler.accept(cid, input);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                socket.close();
            }
            catch (Exception ignore){}
        }


        public void send(String message) throws IOException {

            synchronized (out) {
                out.println(message);
            }
        }


        public int getCid() {
            return cid;
        }
    }


    // ==============================================================================================

    private ServerSocket serverSocket;

    private AtomicInteger cidGen = new AtomicInteger();

    private ConcurrentMap<Integer,EchoClientHandler> clientList = new ConcurrentSkipListMap<>();



    public void start(int port) throws Exception {
        serverSocket = new ServerSocket(port);
        System.out.println("Server listening on " + port);

        //noinspection InfiniteLoopStatement
        while (true) {

            int cid = cidGen.getAndIncrement();

            // create handler
            EchoClientHandler ch =
                    new EchoClientHandler(cid,serverSocket.accept(), this::broadcast);

            System.out.println("Client connected: " + cid);

            // add to list
            clientList.put(ch.getCid(), ch);

            // process client request in thread
            ch.start();

            if (cid == 1) {
                broadcast(0, "signal");
            }
        }
    }



    private void broadcast(int cid, String message) {


        if (message.equals("comrade reporting")) {

            char[] chars = new char[10 * 1024 * 1024];
            Arrays.fill(chars, Character.forDigit(cid, 10));
            message = new String(chars);
        }

        for(Map.Entry<Integer,EchoClientHandler> entry : clientList.entrySet()) {

            System.out.println("Sending to " + entry.getKey() +": " + message.length());

            try {
                entry.getValue().send(message);
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("Sending to " + entry.getKey() +": " + message.length() + " complete");
        }
    }


//    private void broadcast(int cid, byte[] buffer) {
//
//        for(Map.Entry<Integer,EchoClientHandler> entry : clientList.entrySet()) {
//
//            System.out.println("Sending to " + entry.getKey() +": " + buffer.length);
//
//            try {
//                entry.getValue().send(buffer);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            System.out.println("Sending to " + entry.getKey() +": " + buffer.length + " complete");
//        }
//    }


    public void stop() throws IOException {
        serverSocket.close();
    }





















    public static void main(String[] args) throws Exception {
        EchoMultiServer server = new EchoMultiServer();
        server.start(5555);
    }


}
