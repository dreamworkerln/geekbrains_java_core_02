package ru.home.geekbrains.java.core_02.client;

import org.apache.log4j.Logger;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.util.function.Consumer;

public class EchoClient extends Thread  {

    private static final Logger log = Logger.getLogger(MethodHandles.lookup().lookupClass());

    private String ip;
    private int port;
    private SocketChannel channel;

    private Consumer<String> onMessage;
    private Consumer<Boolean> onConnectionState;



    public EchoClient(String ip, int port) throws IOException {
        this.ip = ip;
        this.port = port;
    }



    @Override
    public void start() {
        try {

            channel = SocketChannel.open();
            channel.connect(new InetSocketAddress(ip, port));


            connectionStateChange(true);

        }
        // will trigger exception in read()
        catch (IOException ignored) {}



        super.start();
    }




    public void send(String message) {

        if (!channel.isConnected())
            return;


        try {

            byte[] messageBytes = message.getBytes();

            // buffer
            ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + messageBytes.length);

            // contentLength
            buffer.putInt(messageBytes.length);

            // message bytes
            buffer.put(messageBytes);

            // get ready for readin
            buffer.flip();

            // send to endpoint
            channel.write(buffer);

            //return new String(outputStream.toByteArray(), StandardCharsets.UTF_8 );

        } catch (Exception ignore) {}
    }


    private String read() {

        int contentLength = -1;
        int bodyReadied = -1;
        int count;

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {

            WritableByteChannel outChannel = Channels.newChannel(outputStream);

            ByteBuffer buffer = ByteBuffer.allocate(64 * 1024);
            //ByteBuffer buffer = ByteBuffer.allocate(4);


            // reading from buffer till all data readied (may need several cycles)
            // ClosedChannelException || count == -1   mean disconnected from server
            while ((count = channel.read(buffer)) > 0) {

                buffer.flip(); // switch buffer

                // Initial Read first 4 bytes - get body length
                if (contentLength == -1) {

                    if (buffer.limit() < 4)
                        throw new InvalidParameterException("Reading contentLength bytes != 4");

                    // read contentLength
                    contentLength = buffer.getInt();

                    // write remaining data to outChannel
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

                // server finished transmission
                if (contentLength == bodyReadied) {
                    return outputStream.toString();
                }
            }

            // signalling about server disconnected
            // ClosedChannelException may occurred by it's own
            if (count == -1)
                throw new ClosedChannelException();

        }
        catch (ClosedChannelException e) {

            log.info("Disconnected from server");
            connectionStateChange(false);
            interrupt();
        }
        catch (IOException e) {
            log.error(e);
        }

        return outputStream.toString();
    }

    @Override
    public void run() {

        //noinspection InfiniteLoopStatement
        while (!interrupted()) {
            String message = read();

            // exit in middle
            if (interrupted())
                break;

            messageReceived(message);
        }
    }

    public void disconnect() {
        try {

            interrupt();
            channel.close();
            // ???
            
        } catch (Exception ignore) {}

    }


    /**
     * Add onMessage listener
     * @param onMessage Consumer<String>
     */
    public void addMessageListener(Consumer<String> onMessage) {

        this.onMessage = onMessage;

    }


    /**
     * Add onConnectionStateChanged listener
     * @param onConnectionState Consumer<Boolean>
     */
    public void addConnectionStateListener(Consumer<Boolean> onConnectionState) {

        this.onConnectionState = onConnectionState;

    }


    // ----------------------------------------------------------------------------

    private void connectionStateChange(boolean state) {

        if (onConnectionState != null)
            onConnectionState.accept(state);
    }


    private void messageReceived(String message) {

        if (onMessage != null)
            onMessage.accept(message);
    }





}