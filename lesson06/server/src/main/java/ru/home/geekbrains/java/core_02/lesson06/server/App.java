package ru.home.geekbrains.java.core_02.lesson06.server;

import org.apache.log4j.Logger;
import ru.home.geekbrains.java.core_02.lesson06.server.utils.DAOCrutch;

import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Hello world!
 *
 */
public class App 
{

    private static Logger log;

    public static void main(String[] args) throws Exception {

        Thread.sleep(1000); // fuckin idea debugger

        setupLog4J();

        // Users:
        //
        // q1/qwerty1
        // q2/qwerty2
        // q3/qwerty3

        DAOCrutch.connect();

        new EchoServer(4321).start();

        DAOCrutch.disconnect();

        log.info("\n\n************************* STOP *************************");


    }


    private static void setupLog4J() {

        try {

            String path = System.getProperty("user.dir") + "/" + "log/";

            //Appending a trailing / if needed
            path = path.endsWith("/") ? path : path + "/";
            if (!Files.exists(Paths.get(path)))
                Files.createDirectories(Paths.get(path));

            // DON'T CALL LOGGERS BEFORE log.name SET BELOW
            System.setProperty("log.name.debug", path + "debug.log");
            System.setProperty("log.name.log", path + "log.log");
            System.setProperty("log.name.error", path + "error.log");

            log = Logger.getLogger(MethodHandles.lookup().lookupClass());

            log.info("\n\n************************* START *************************");

            //Test();
        }
        catch (Exception e) {
            throw new RuntimeException("Can't setup logging", e);
        }
    }
}
