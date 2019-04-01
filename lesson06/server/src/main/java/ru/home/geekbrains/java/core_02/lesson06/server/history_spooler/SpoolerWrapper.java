package ru.home.geekbrains.java.core_02.lesson06.server.history_spooler;

import org.apache.log4j.Logger;

import java.lang.invoke.MethodHandles;
import java.sql.SQLException;

/**
 * Fake singleton
 */
public enum SpoolerWrapper {

    INSTANCE;

    private final Logger log = Logger.getLogger(MethodHandles.lookup().lookupClass());

    private Spooler spooler;

    SpoolerWrapper() {

        spooler = new Spooler();

    }

    public Spooler getSpooler() {

        return spooler;
    }
}
