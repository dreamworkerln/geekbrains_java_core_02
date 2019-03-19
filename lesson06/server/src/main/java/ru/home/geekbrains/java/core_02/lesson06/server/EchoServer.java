package ru.home.geekbrains.java.core_02.lesson06.server;

import org.apache.log4j.Logger;
import ru.home.geekbrains.java.core_02.lesson06.server.jobpool.AsyncJobPool;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntUnaryOperator;

public class EchoServer  {

    private static final Logger log = Logger.getLogger(MethodHandles.lookup().lookupClass());


    private static final int MAX_POOL_SIZE = 100;

    private AsyncJobPool<Void> requestPool = new AsyncJobPool<>(null);
    private AsyncJobPool<Void> pushPool = new AsyncJobPool<>(null);

    // Non-negative AtomicInteger incrementator
    private static IntUnaryOperator AtomicNonNegativeIntIncrementator = (i) -> i == Integer.MAX_VALUE ? 0 : i + 1;
    // client id generator
    private static final AtomicInteger clientIdGen =  new AtomicInteger();

    // connection list
    private Map<Integer,ClientWrapper> clientList = new ConcurrentSkipListMap<>();

    private ServerSocketChannel serverChannel;




    public EchoServer(int port) {
        try {
            serverChannel = ServerSocketChannel.open();
            serverChannel.socket().bind(new InetSocketAddress(port));

            log.info("TCP server listening on " +
                     serverChannel.socket().getInetAddress().getHostAddress() + ":" +
                     serverChannel.socket().getLocalPort());

        } catch (Exception e) {
            log.error(e);
        }
    }





    public void start() {

        try {
            //noinspection InfiniteLoopStatement
            while (true) {

                // wait on new connection on serverChannel.accept()
                SocketChannel channel = serverChannel.accept();

                // Generate unique cid (client id) for client
                int cid = clientIdGen.getAndUpdate(AtomicNonNegativeIntIncrementator);

                ClientWrapper client = new ClientWrapper(cid, channel);
                client.onMessage = this::onClientMessage;
                client.onDisconnect = this::onClientDisconnect;




                // Do not accept client due to MAX_POOL_SIZE
                if (requestPool.size() > EchoServer.MAX_POOL_SIZE) {

                    client.send("GTFO");
                    client.close();

                    continue;
                }

                clientList.put(client.getCid(), client);

                // Proceed client request in requestPool thread
                requestPool.add(client::start);

                broadcast("connected: " + client.getCid());
            }
        }
        // on client disconnected
        catch(ClosedByInterruptException ignored) {}
        catch (Exception e) {
            log.error("Can't accept client connection ", e);
        }
        finally {
            close();
        }
    }




    private void onClientMessage(ClientWrapper client, String message) {

        // Broadcast in different ThreadPool due to slowloris clients
        pushPool.add(() -> broadcast(message));
    }



    private void onClientDisconnect(ClientWrapper client) {

        clientList.remove(client.getCid());
        broadcast("disconnected: " + client.getCid());
    }




    // Broadcasting
    private Void broadcast(String message) {

        for (Map.Entry<Integer,ClientWrapper> entry : clientList.entrySet()) {

            entry.getValue().send(message);
        }
        return null;
    }



    private void close() {
        try {
            serverChannel.close();
        }
        catch (IOException ignored) {}
    }

}