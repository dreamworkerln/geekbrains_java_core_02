package ru.home.geekbrains.java.core_02.client;

import org.apache.log4j.Logger;
import ru.home.geekbrains.java.core_02.client.gui.ChatForm;
import ru.home.geekbrains.java.core_02.client.gui.ChatFormController;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Hello world!
 *
 */
public class App 
{

    static class WindowThread extends Thread {

        @Override
        public void run() {
            // OuterClassName.this

            // get ChatForm controller
            ChatForm.getController(App::onGetController);

            // display form
            ChatForm.show();

        }

    }




    private static Logger log;

    private static EchoClient client;

    private static ChatFormController controller;




    public static void main( String[] args ) throws Exception {

        Thread.sleep(1000); // fuckin idea debugger

        setupLog4J();

        new WindowThread().start();

    }


    /**
     * On get ChatForm controller
     * @param controller
     */
    private static void onGetController(ChatFormController controller) {

        App.controller = controller;
        reconnect();
    }


    private static void reconnect() {

        try {

            // create new tcp client
            client = new EchoClient("localhost", 4321);

            // connect signals from ChatForm to tcp client
            controller.addConnectPressed((a) -> {
                if (client == null ||
                    !client.isAlive()) {

                    reconnect();
                }
            });
            controller.addSendPressed((message) -> client.send(message));
            controller.addFormCloseListener((a) -> System.exit(0));


            // connect signals from  tcp client to ChatForm
            client.addMessageListener(controller::setIncomingMessage);
            client.addConnectionStateListener(controller::setConnected);

            // start tcp client
            client.start();

        } catch (IOException e) {
            log.error(e);
        }


    }









    // --------------------------------------------------------------------------


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
