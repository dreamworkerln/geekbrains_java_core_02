package ru.home.geekbrains.java.core_02.lesson06.client;

import com.sun.javafx.image.ByteToBytePixelConverter;
import org.apache.log4j.Logger;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
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

//    private Consumer<List<String>> onClientListUploaded;
//
//    private BiConsumer<String,Boolean> onClientListChanged;



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

        int bodyLength = -1;  // length of single message(body)
        int bodyReadied = -1; // currently readied body size
        int dataSize;         // total amount of bytes readied from server to buffer

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        //WritableByteChannel outChannel = Channels.newChannel(outputStream);

        try {



            ByteBuffer buffer = ByteBuffer.allocate(64 * 1024);
            //ByteBuffer buffer = ByteBuffer.allocate(4);


            // reading from buffer till all data readied (may need several cycles)
            // ClosedChannelException || count == -1   mean disconnected from server
            while ((dataSize = channel.read(buffer)) > 0) {


                buffer.flip(); // switch buffer

                // Here we have readied several messages to buffer
                // or readied only part of one message
                while (buffer.remaining() > 0) {

                    // Initial Read first 4 bytes - get body length
                    if (bodyReadied == -1) {

                        // Не хватает оставшихся байтов в буффере, чтобы прочесть из него int
                        // Начинаем дальше читать из сокета в буфер
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
                    // Next readings
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

                        // reset bodyReadied and outputStream
                        bodyReadied = -1;
                        outputStream.reset();

                        // Display message to user
                        // (handle incoming message if not empty)
                        if (!"".equals(message))
                            messageReceived(message);
                    }
                }
                // left shift remaining bytes in buffer
                buffer.compact();

            }

            // Signalling about server disconnected
            // remember, ClosedChannelException may occurred by it's own
            //  if (dataSize == -1)
            //      throw new ClosedChannelException();

        }
        catch (ClosedChannelException ignore) {}
        catch (IOException e) {
            log.error(e);
        }
        finally {

            // Sure that channel is closed
            // (channel сам не делает channel.connected = false при насильственном разрыве связи)
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

                    // Read messages in loop from server (till disconnected from server)
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

    /*
    public void addClientListUploadListener(Consumer<List<String>> onClientListUploaded) {

       this.onClientListUploaded = onClientListUploaded;

    }

    public void addClientListChangedListener(BiConsumer<String,Boolean> onClientListChanged) {

        this.onClientListChanged = onClientListChanged;
    }
    */





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