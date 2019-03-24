package ru.home.geekbrains.java.core_02.client;

import java.io.*;
import java.net.Socket;

public class EchoClient {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;


    public static void main(String[] args) {

        EchoClient client1 = new EchoClient();
        client1.startConnection("localhost", 5555);

        EchoClient client2 = new EchoClient();
        client2.startConnection("localhost", 5555);
    }

    public void startConnection(String ip, int port) {
        try {
            socket = new Socket(ip, port);

            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String input;

            while ((input = in.readLine()) != null) {

                onMessage(input);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

//    public void sendMessage(String msg) {
//        try {
//
//            // send to server
//            out.println(msg);
//
//            // handle message from server
//            onMessage(msg);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    private void onMessage(String message) throws IOException {

        System.out.println("Incoming: " + message);

        // echo to server on special message
        if (message.equals("signal")) {

            String outMessage = "comrade reporting";
            System.out.println(outMessage);
            // send to server
            out.println(outMessage);
        }
    }

    public void stopConnection() {
        try {
            in.close();
            out.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
