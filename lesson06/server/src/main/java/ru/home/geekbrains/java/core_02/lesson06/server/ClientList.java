package ru.home.geekbrains.java.core_02.lesson06.server;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

public class ClientList implements Iterable<Map.Entry<Integer, ClientWrapper>> {

    private Map<Integer,ClientWrapper> list = new ConcurrentSkipListMap<>();

    private Map<String,ClientWrapper> loginIndex = new ConcurrentSkipListMap<>();


    public void put(int cid, ClientWrapper client) {

        list.put(cid, client);

        if (client.getLogin() != null)
            loginIndex.put(client.getLogin(), client);
    }

    public ClientWrapper get(int cid) {

        return list.get(cid);
    }

    public ClientWrapper getByLogin(String login) {

        return loginIndex.get(login);
    }

    public boolean containsKey(int cid) {

        return list.containsKey(cid);
    }

    public boolean containsLogin(String login) {

        return loginIndex.containsKey(login);
    }


    public void remove(int cid) {

        ClientWrapper client = list.get(cid);

        if (client != null) {

            list.remove(cid);
            if (client.getLogin() != null)
                loginIndex.remove(client.getLogin());
        }
    }

    /**
     * Update login
     * @param cid
     */
    public void updateLogin(int cid) {

        ClientWrapper client = list.get(cid);

        if (client != null)
            loginIndex.put(client.getLogin(), client);
    }








    @Override
    public Iterator<Map.Entry<Integer, ClientWrapper>> iterator() {

        return list.entrySet().iterator();
    }

    /**
     * Iterable by login
     * @return Iterable
     */
    public Iterable<Map.Entry<String,ClientWrapper>> getLoginIterable() {

        return loginIndex.entrySet();
    }



    
}
