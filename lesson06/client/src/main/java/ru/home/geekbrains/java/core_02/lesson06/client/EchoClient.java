package ru.home.geekbrains.java.core_02.lesson06.client;

import org.apache.log4j.Logger;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.security.InvalidParameterException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class EchoClient extends Thread  {


    // =======================================================================




    private static final Logger log = Logger.getLogger(MethodHandles.lookup().lookupClass());

    private String ip;
    private int port;
    private SocketChannel channel = SocketChannel.open();
    private String login = "";
    private String password = "";

    private final Lock lock = new ReentrantLock();
    private final Condition isWaiting = lock.newCondition();


    private Consumer<String> onMessage;
    private Consumer<Boolean> onConnectionState;



    public EchoClient(String ip, int port) throws IOException {
        this.ip = ip;
        this.port = port;
    }


    /*
    @Override
    public void start() {

        reconnect();

        super.start();
    }
    */

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




    private void readLoop() {

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

                    // readLoop contentLength
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

                // clear buffer
                buffer.clear();

                // server finished transmission
                if (contentLength == bodyReadied) {
                    String message = outputStream.toString();

                    // reset buffer and outputStream
                    contentLength = -1;
                    outputStream.reset();

                    // Display message to user
                    // (handle incoming message if not empty)
                    if (!"".equals(message))
                        messageReceived(message);
                }
            }

            // Signalling about server disconnected
            // remember, ClosedChannelException may occurred by it's own
            if (count == -1)
                throw new ClosedChannelException();

        }
        catch (ClosedChannelException e) {

        }
        catch (IOException e) {
            log.error(e);
        }
        finally {

            // Sure that channel is closed
            // (channel сам не выставляет channel.connected = false при падении сервера)
            try {channel.close();} catch (IOException ignore) {}

            log.info("Disconnected from server");
            connectionStateChange(false);
        }

    }




    @Override
    public void run() {

        try {

            //noinspection InfiniteLoopStatement
            while (!isInterrupted()) {


                // await if not connected
                if(!channel.isConnected()) {
                    lock.lock();
                    isWaiting.await(); // waiting here
                    lock.unlock();
                }

                boolean isConnected = connect_internal();
                connectionStateChange(isConnected);

                // Ок, подключились
                if (channel.isConnected()) {

                    // Authenticate on server
                    authenticate();

                    // Read awaiting amd readLoop messages in loop from server (til disconnect)
                    // (will block here)
                    readLoop();
                }
            }
        }
        catch(InterruptedException ignore){
            log.trace("Thread " + Thread.currentThread().getName() + " has been interrupted");
        }

        catch(Throwable e) {
            log.error(e);
        }


    }



    public void connect() {

        // Told thread to reconnect if needed
        lock.lock();
        isWaiting.signal();
        lock.unlock();
    }

    public void close() {
        try {

            interrupt();
            channel.close();
            // ???

        } catch (Exception ignore) {}

    }


    public void setCredentals(String login, String password) {

        this.login = login;
        this.password = password;
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



    /**
     * Connect to server
     */
    private boolean connect_internal() {

        boolean result = false;

        try {

            channel = SocketChannel.open();
            channel.connect(new InetSocketAddress(ip, port));

            result = true;

        }
        catch (IOException ignored) {}


        return result;
    }


    private void authenticate() {

        send(String.format("/auth %1$s %2$s", login, password));
    }



}