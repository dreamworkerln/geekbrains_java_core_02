package ru.home.geekbrains.java.core_02.lesson06.server;

import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.security.InvalidParameterException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

class ClientWrapper {

    private static final Logger log = Logger.getLogger(MethodHandles.lookup().lookupClass());

    private int cid;
    private SocketChannel channel; // client channel

    BiConsumer<ClientWrapper, String> onMessage;
    Consumer<ClientWrapper> onDisconnect;



    public ClientWrapper(int cid, SocketChannel channel) {
        this.cid = cid;
        this.channel = channel;

    }


    /**
     * Handle client request
     */
    public Void start() {


        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             WritableByteChannel outChannel = Channels.newChannel(outputStream)) {

            int contentLength = -1; // длина body
            int bodyReadied = -1;   // сколько байт body прочитано
            int count;


            ByteBuffer buffer = ByteBuffer.allocate(64 * 1024);

            // Здесь в цикле будут вычитываться данные из пакетов в буффер.
            // по мере их поступления через сетевую подсистему

            // reading from buffer till all data readied (may need several cycles)
            // ClosedChannelException || count == -1 mean client disconnected
            while ((count = channel.read(buffer)) > 0) {


                buffer.flip(); // switch to read mode

                // Это первый пакет нового сообщения
                if (contentLength == -1) {

                    // Прочитали к-то мусор, должнобыть min 4 байта - длина сообщения
                    if (buffer.limit() < 4)
                        throw new InvalidParameterException("Reading contentLength bytes != 4");

                    // read body length (В первых 4 байтах первого пакета сообщения лежит длинна body)
                    contentLength = buffer.getInt();

                    // 2. Write remaining data to outChannel
                    outChannel.write(buffer);
                    bodyReadied = buffer.limit() - Integer.BYTES;
                }

                // Next readings
                // Это пришли следущие пакеты сообщения
                else {

                    // write next body chunk to outChannel
                    outChannel.write(buffer);
                    bodyReadied += buffer.limit();
                }

                // очищаем buffer, так как дальше может быть следущая итерация цикла while
                // и без очистки он начнет писать в конец буффера
                buffer.clear();

                // client finished transmission
                if (contentLength == bodyReadied) {

                    String message = outputStream.toString();

                    log.info("IN: " + message);

                    // reset buffer and outputStream
                    contentLength = -1;
                    outputStream.reset();

                    // tell server about message
                    onMessage.accept(this, message);
                }
            }

        } catch (Exception e) {
            log.error("IOException: ", e);
        }

        // disconnect client
        try {
            channel.close();
        } catch (Exception ignored) {}

        // Оповестим сервер о том, что клиент отключился
        onDisconnect.accept(this);

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

    // -------------------------------------------------------------


    public int getCid() {
        return cid;
    }


}
