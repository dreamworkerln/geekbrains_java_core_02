package ru.home.geekbrains.java.core_02.lesson06.server.entities;

import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;
import java.time.Instant;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Connection {

    private static final Logger log = Logger.getLogger(MethodHandles.lookup().lookupClass());

    private int cid; // connection id
    private SocketChannel channel; // user channel

    private User user; // if have one - than successfully authenticated
    private Instant connectedTime;

    protected BiConsumer<Connection,String> onMessage;
    protected Consumer<Connection> onDisconnect;


    public Connection(int cid, SocketChannel channel) {
        this.cid = cid;
        this.channel = channel;
        this.connectedTime = Instant.now();
    }


    /**
     * Handle user request
     */
    public Void start() {


        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

             //WritableByteChannel outChannel = Channels.newChannel(outputStream)) {

            int bodyLength = -1;  // length of single message(body)
            int bodyReadied = -1; // currently readied body size
            int dataSize;         // total amount of bytes readied from server to buffer

            ByteBuffer buffer = ByteBuffer.allocate(64 * 1024);
            //ByteBuffer buffer = ByteBuffer.allocate(4);

            // Здесь в цикле будут вычитываться данные из пакетов в буффер.
            // по мере их поступления через сетевую подсистему

            // reading from buffer till all data readied (may need several cycles)
            // ClosedChannelException || count == -1 mean user disconnected
            while ((dataSize = channel.read(buffer)) > 0) {


                buffer.flip(); // switch to read mode

                // Here we have readied several messages to buffer
                // or readied only part of one message
                while (buffer.remaining() > 0) {


                    // Initial Read first 4 bytes - get body length
                    if (bodyReadied == -1) {

                        // Не хватает оставшихся байтов в буффере, чтобы прочесть из него int
                        if (buffer.remaining() < Integer.BYTES) {
                            break;
                        }

                        // header contains body length int(4 bytes)
                        // get message body length
                        bodyLength = buffer.getInt();

                        // writing remaining data to outputStream (not exceeding bodyLength)
                        byte[] chunk = new byte[Math.min(buffer.remaining(), bodyLength)];
                        buffer.get(chunk);
                        outputStream.write(chunk);
                        bodyReadied = chunk.length;
                    }
                    // Next chunks readings
                    else {

                        // write next body chunk to outputStream
                        byte[] chunk = new byte[Math.min(buffer.remaining(), bodyLength - bodyReadied)];
                        buffer.get(chunk);
                        outputStream.write(chunk);
                        bodyReadied += chunk.length;
                    }

                    // get whole single message
                    if (bodyReadied == bodyLength) {

                        String message = outputStream.toString();

                        log.info("IN: " + message);

                        // reset bodyReadied and outputStream
                        bodyReadied = -1;
                        outputStream.reset();

                        try {
                            // tell server about message
                            onMessage.accept(this, message);
                        }
                        // Internal Server error 500 alike ...
                        catch (Exception e) {

                            log.error("IOException: ", e);
                        }
                    }
                }
                // left shift remaining bytes in buffer
                buffer.compact();
            }
        }
        catch (ClosedChannelException ignored) {}
        catch (Exception e) {
            log.error("IOException: ", e);
        }
        finally {

            log.info("User " + this.getCid() + " disconnected");

            // forcibly disconnect user
            // (channel сам не делает channel.connected = false при насильственном разрыве связи)
            try {channel.close();} catch (Exception ignored) {}

            // Оповестим сервер о том, что клиент отключился
            onDisconnect.accept(this);
        }
        return null;
    }




    public void send(String message) {

        try {
            byte[] messageBytes = message.getBytes();

            // buffer
            ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + messageBytes.length);

            // write contentLength
            buffer.putInt(messageBytes.length);

            // write message bytes
            buffer.put(messageBytes);

            // prepare buffer to reading
            buffer.flip();

            // send to endpoint
            channel.write(buffer);

            log.info("OUT: " + message);
        }
        catch (IOException ignored) {}
    }


    public void close() {
        try {
            channel.close();
        }
        catch (IOException ignore) {}
    }


    public boolean isConnected() {
        return channel.isConnected();
    }

    // -------------------------------------------------------------


    public int getCid() {
        return cid;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Instant getConnectedTime() {
        return connectedTime;
    }

    public void addHandlers(BiConsumer<Connection,String> onMessage, Consumer<Connection> onDisconnect) {

        this.onMessage = onMessage;
        this.onDisconnect = onDisconnect;
    }

}
