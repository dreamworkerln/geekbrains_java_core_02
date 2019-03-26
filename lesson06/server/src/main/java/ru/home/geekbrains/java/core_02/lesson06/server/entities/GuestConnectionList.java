package ru.home.geekbrains.java.core_02.lesson06.server.entities;

import org.apache.log4j.Logger;
import ru.home.geekbrains.java.core_02.lesson06.server.entities.Connection;

import java.lang.invoke.MethodHandles;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

public class GuestConnectionList implements Iterable<Map.Entry<Instant, Connection>> {


    private class LogoutTimer extends Thread {

        @Override
        public void run() {

            try {

                //noinspection InfiniteLoopStatement
                while(true) {

                    Instant now = Instant.now();

                    SortedMap<Instant,Connection> toDisconnect = list.headMap(now.minus(1000, ChronoUnit.MILLIS));

                    toDisconnect.forEach((k,v)->{

                        list.remove(k);
                        v.close();

                    });

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignore) {}
                }

            }
            catch (Exception e) {
                log.error(e);
            }
        }
    }


    private static final Logger log = Logger.getLogger(MethodHandles.lookup().lookupClass());

    private NavigableMap<Instant,Connection> list = new ConcurrentSkipListMap<>();


    public GuestConnectionList() {

        //start auth timeout autologout thread
        LogoutTimer lgt = new LogoutTimer();
        lgt.setDaemon(true);
        lgt.start();
    }

    public void put(Connection connection) {

        list.put(connection.getConnectedTime(), connection);
    }

    public Connection get(Instant t) {

        return list.get(t);
    }

    public boolean containsKey(Instant t) {

        return list.containsKey(t);
    }

    public void remove(Connection connection) {

        list.remove(connection.getConnectedTime());
    }



    @Override
    public Iterator<Map.Entry<Instant, Connection>> iterator() {

        return list.entrySet().iterator();
    }


}
