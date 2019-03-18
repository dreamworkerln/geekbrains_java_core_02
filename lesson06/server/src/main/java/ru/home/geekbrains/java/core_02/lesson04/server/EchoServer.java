package ru.home.geekbrains.java.core_02.lesson04.server;

import org.apache.log4j.Logger;
import ru.home.geekbrains.java.core_02.lesson04.server.jobpool.AsyncJobPool;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.security.InvalidParameterException;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntUnaryOperator;

public class EchoServer  {

    private static final Logger log = Logger.getLogger(MethodHandles.lookup().lookupClass());


    // =====================================================================================


    private class EchoClientHandler {

        private int cid;
        private SocketChannel channel; // client channel


        public EchoClientHandler(int cid, SocketChannel channel) {

            this.cid = cid;
            this.channel = channel;

        }





        public Void start() {

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            try {

                if (requestPool.size() > MAX_POOL_SIZE) {

                    channel.write(ByteBuffer.wrap("GTFO".getBytes()));
                    channel.close();
                }

                int contentLength = -1;
                int bodyReadied = -1;
                int count;

                WritableByteChannel outChannel = Channels.newChannel(outputStream);
                //ByteBuffer buffer = ByteBuffer.allocate(64*1024);
                ByteBuffer buffer = ByteBuffer.allocate(4);

                // reading from buffer till all data readied (may need several cycles)
                // ClosedChannelException || count == -1 mean client disconnected
                while ((count = channel.read(buffer)) > 0) {


                    buffer.flip(); // switch to read mode

                    // Initial Read first 4 bytes - get body length
                    if (contentLength == -1) {

                        if (buffer.limit() < 4)
                            throw new InvalidParameterException("Reading contentLength bytes != 4");

                        // read contentLength
                        contentLength = buffer.getInt();

                        // 2. Write remaining data to outChannel
                        outChannel.write(buffer);
                        bodyReadied = buffer.limit() - Integer.BYTES;
                    }
                    // Next readings
                    else {

                        // write next body chunk to outChannel
                        outChannel.write(buffer);
                        bodyReadied += buffer.limit();
                    }

                    buffer.clear(); // clear buffer

                    // client finished transmission
                    if (contentLength == bodyReadied) {

                        String message = outputStream.toString();

                        log.info("IN: " + message);

                        // reset buffer and outputStream
                        contentLength = -1;
                        outputStream.reset();

                        // broadcasting
                        pushPool.add(() -> broadcast(message));

                    }
                }

            }
            catch (Exception e) {
                log.error("IOException: ",e);
            }


            // disconnect client
            try {
                clientList.remove(cid);
                channel.close();
            } catch (Exception ignored) {}


            return null;
        }
    }






    // ============================================================================================



    // ============================================================================================





    private static final int MAX_POOL_SIZE = 100;

    private AsyncJobPool<Void> requestPool = new AsyncJobPool<>(null);
    private AsyncJobPool<Void> pushPool = new AsyncJobPool<>(null);

    // Non-negative AtomicInteger incrementator
    private static IntUnaryOperator AtomicNonNegativeIntIncrementator = (i) -> i == Integer.MAX_VALUE ? 0 : i + 1;
    // client id generator
    private static final AtomicInteger clientIdGen =  new AtomicInteger();

    // connection list
    private Map<Integer,SocketChannel> clientList = new ConcurrentSkipListMap<>();

    //private ServerSocket serverSocket;
    private ServerSocketChannel serverChannel;




    public EchoServer(int port) {
        try {
            serverChannel = ServerSocketChannel.open();
            serverChannel.socket().bind(new InetSocketAddress(port));
            
            log.info("TCP server listening on " +
                               serverChannel.socket().getInetAddress().getHostAddress() + ":" +
                               serverChannel.socket().getLocalPort());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }





    public void start() {

        try {
            //noinspection InfiniteLoopStatement
            while (true) {

                // wait on new connection on serverChannel.accept()
                SocketChannel client = serverChannel.accept();

                // safe while use in multithreading
                int cid = clientIdGen.getAndUpdate(AtomicNonNegativeIntIncrementator);

                clientList.put(cid, client);

                // then proceed client in requestPool thread
                // in EchoClientHandler.start
                requestPool.add(new EchoClientHandler(cid, client)::start);

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



    private void close() {
        try {
            serverChannel.close();
        }
        catch (IOException ignored) {}
    }






//    private void onMessage(String message) {
//
//        //System.out.println(message);
//
//        // broadcast in different pool
//
//
//    }


    // Broadcast
    private Void broadcast(String message) {

        for (Map.Entry<Integer,SocketChannel> entry : clientList.entrySet()) {

            try {

                SocketChannel channel = entry.getValue();

                byte[] messageBytes = message.getBytes();

                // buffer
                ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + messageBytes.length);

                // write contentLength
                buffer.putInt(messageBytes.length);

                // write message bytes
                buffer.put(messageBytes);

                buffer.flip();

                // send to endpoint
                channel.write(buffer);

            }
            catch (IOException ignored) {}
        }
        return null;
    }


}