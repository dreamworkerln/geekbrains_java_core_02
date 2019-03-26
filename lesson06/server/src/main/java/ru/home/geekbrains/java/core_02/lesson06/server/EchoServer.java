package ru.home.geekbrains.java.core_02.lesson06.server;

import org.apache.log4j.Logger;
import ru.home.geekbrains.java.core_02.lesson06.server.entities.User;
import ru.home.geekbrains.java.core_02.lesson06.server.entities.Connection;
import ru.home.geekbrains.java.core_02.lesson06.server.entities.ConnectionList;
import ru.home.geekbrains.java.core_02.lesson06.server.entities.GuestConnectionList;
import ru.home.geekbrains.java.core_02.lesson06.server.utils.DAOCrutch;
import ru.home.geekbrains.java.core_02.lesson06.server.utils.jobpool.AsyncJobPool;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntUnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EchoServer  {

    private static final Logger log = Logger.getLogger(MethodHandles.lookup().lookupClass());


    private static final int MAX_POOL_SIZE = 100;

    private AsyncJobPool<Void> requestPool = new AsyncJobPool<>(null);
    private AsyncJobPool<Void> pushPool = new AsyncJobPool<>(null);

    // Non-negative AtomicInteger incrementator
    private static IntUnaryOperator AtomicNonNegativeIntIncrementator = (i) -> i == Integer.MAX_VALUE ? 0 : i + 1;
    // connection id generator
    private static final AtomicInteger connectionIdGen =  new AtomicInteger();

    // connection list
    private ConnectionList connectionList = new ConnectionList();

    // Connection list with unauthenticated clients
    private GuestConnectionList guestList = new GuestConnectionList();

    // Server socket
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

                // Generate unique cid (connection id) for connection
                int cid = connectionIdGen.getAndUpdate(AtomicNonNegativeIntIncrementator);

                Connection connection = new Connection(cid, channel);
                connection.addHandlers(this::requestHandler, this::disconnectHandler);

                // Do not accept connection due to MAX_POOL_SIZE
                if (requestPool.size() > EchoServer.MAX_POOL_SIZE) {

                    connection.send("GTFO");
                    connection.close();

                    continue;
                }

                // Do not add unauthenticated clients connections to connectionList
                // Instead add them to GuestList
                guestList.put(connection);

                // Proceed client request in requestPool thread
                // maybe недо-client will want to authenticate
                requestPool.add(connection::start);
            }
        }
        // on connection disconnected
        catch(ClosedByInterruptException ignored) {}
        catch (Exception e) {
            log.error("Can't accept client connection ", e);
        }
        finally {
            close();
        }
    }





    private String makeClientList(Connection source) {

        StringBuilder sb = new StringBuilder();

        sb.append("/clientlist ");

        for (Map.Entry<String,Connection> entry : connectionList.getLoginIterable()) {
            sb.append(entry.getKey()+ "\n");
        }

        return sb.toString();
    }




    // Broadcasting
    private Void broadcast(Connection source, String message) {

        for (Map.Entry<Integer,Connection> entry : connectionList) {

            User recipient = entry.getValue().getUser();
            User sender = source.getUser();

            // Не посылаем сообщение получателю, если он добавил отправителя в черный список
            if (!recipient.getBlackList().contains(sender.getLogin())) {

                entry.getValue().send(message);
            }
        }
        return null;
    }


    /**
     * Closing server
     */
    private void close() {
        try {
            serverChannel.close();
        }
        catch (IOException ignored) {}
    }






    private void requestHandler(Connection connection, String message) {

        try {

            // 1. Auth
            String regex = "^/auth\\s(\\w+)\\s(\\w+)$";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(message);

            if (matcher.find()) {
                log.trace("Full match: " + matcher.group(0));

                if (matcher.groupCount() == 2) {

                    String login = matcher.group(1);
                    String password = matcher.group(2);

                    User user = DAOCrutch.loadUser(login, password);

                    if (  user!= null &&
                          !connectionList.containsLogin(login) && //запретить множественный вход
                          connection.isConnected()) { // случайно не сдох по пути аутентификации

                        connection.setUser(user);

                        // move user connection from guestList to connectionList
                        guestList.remove(connection);
                        connectionList.put(connection);

                        broadcast(connection, "connected: " + user.getLogin());
                        broadcast(connection, makeClientList(connection)); // send user list
                    }
                    // Unauthenticated - disconnect connection
                    else {
                        connectionList.remove(connection.getCid());
                        connection.send("NOT AUTHENTICATED");
                        connection.close(); // immediately disconnect
                    }
                }
                return;
            }

            // Other requests

            // if not authenticated - no more commands allowed - disconnect
            if (connection.getUser() == null) {
                connection.send("NOT AUTHENTICATED");
                connection.close();
                return;
            }


            // 2. Unicast - private message
            regex = "^/p\\s(\\w+)\\s(.*)$";
            pattern = Pattern.compile(regex);
            matcher = pattern.matcher(message);

            if (matcher.find()) {
                log.trace("Full match: " + matcher.group(0));

                if (matcher.groupCount() == 2) {

                    String login = matcher.group(1);
                    String msg = matcher.group(2);

                    Connection dest = connectionList.getByLogin(login);

                    if (dest != null) {

                        User sender = connection.getUser();
                        User recipient = dest.getUser();

                        // Не посылаем сообщение получателю, если он добавил отправителя в черный список
                        if (!recipient.getBlackList().contains(sender.getLogin())) {

                            msg = connection.getUser().getLogin() + ": " + msg;
                            dest.send(msg);
                        }
                        else {
                            msg = "YOU ARE BANNED GTFO ASSHOLE!";
                            connection.send(msg);
                        }
                    }

                }
                return;
            }




            // 2. blacklist work
            regex = "^/(ban|uban)\\s(\\w+)$";
            pattern = Pattern.compile(regex);
            matcher = pattern.matcher(message);

            if (matcher.find()) {
                log.trace("Full match: " + matcher.group(0));

                if (matcher.groupCount() == 2) {

                    String action = matcher.group(1);
                    String login = matcher.group(2);

                    User user = connection.getUser();

                    if (action.equals("ban")) {
                        user.getBlackList().add(login);
                        DAOCrutch.banUser(user, login);
                        connection.send(makeClientList(connection)); // send user list

                        // PERMABAN
                        if (user.getLogin().equals(login)) {
                            connection.send("PERMABANNED");
                            connection.close();
                        }


                        //

                    }
                    else if (action.equals("uban")) {
                        user.getBlackList().remove(login);
                        DAOCrutch.unBanUser(user, login);
                        connection.send(makeClientList(connection)); // send user list
                    }
                }
                return;
            }





            // 3. default: broadcast message
            // Broadcast in different ThreadPool due to slowloris clients
            final String tmp = connection.getUser().getLogin() + ": " + message;
            pushPool.add(() -> broadcast(connection, tmp));

        }
        catch(Exception e) {
            log.error(e);
        }
    }



    private void disconnectHandler(Connection connection) {

        if (connection.getUser() != null) {
            connectionList.remove(connection.getCid());
            broadcast(connection, "disconnected: " + connection.getUser().getLogin());
            broadcast(connection, makeClientList(connection));

        }

        // guestList has self-vacuuming feature (with auto disconnection by login timeout)
        // no need to disconnect / remove from guestList unauthenticated clients
    }

}