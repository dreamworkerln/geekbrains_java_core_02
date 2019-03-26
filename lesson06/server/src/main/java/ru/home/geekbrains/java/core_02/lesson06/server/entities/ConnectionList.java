package ru.home.geekbrains.java.core_02.lesson06.server.entities;

import org.apache.log4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

public class ConnectionList implements Iterable<Map.Entry<Integer, Connection>> {

    private static final Logger log = Logger.getLogger(MethodHandles.lookup().lookupClass());

    // index by cid
    private Map<Integer,Connection> list = new ConcurrentSkipListMap<>();

    // index by login
    private Map<String,Connection> loginIndex = new ConcurrentSkipListMap<>();


    public void put(Connection connection) {

        list.put(connection.getCid(), connection);

        User user = connection.getUser();

        if (user != null)
            loginIndex.put(user.getLogin(), connection);
    }


    public Connection get(int cid) {

        return list.get(cid);
    }


    public Connection getByLogin(String login) {

        return loginIndex.get(login);
    }


    public boolean containsKey(int cid) {

        return list.containsKey(cid);
    }


    public boolean containsLogin(String login) {

        return loginIndex.containsKey(login);
    }


    public void remove(int cid) {

        Connection connection = list.get(cid);

        if (connection != null) {

            list.remove(cid);
            User user = connection.getUser();
                loginIndex.remove(user.getLogin());
        }
    }

    @Override
    public Iterator<Map.Entry<Integer, Connection>> iterator() {

        return list.entrySet().iterator();
    }

    /**
     * Iterable by login
     * @return Iterable
     */
    public Iterable<Map.Entry<String,Connection>> getLoginIterable() {

        return loginIndex.entrySet();
    }



    
}
