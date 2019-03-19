package ru.home.geekbrains.java.core_02.lesson06.client.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.apache.log4j.Logger;
import ru.home.geekbrains.java.core_02.lesson06.client.EchoClient;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

public class Controller {


    private static final Logger log = Logger.getLogger(MethodHandles.lookup().lookupClass());

    @FXML
    public VBox main;

    @FXML
    public Label label;

    @FXML
    ImageView imageView;

    @FXML
    Button btnConnect;

    @FXML
    TextArea textArea;

    @FXML
    TextField textField;

    @FXML
    Button btnSend;

    // =========================================================================================

    private EchoClient client = null;

    public Controller() {


    }



    // Btn send pressed
    public void sendMsg() {

        String message = textField.getText();

        //textArea.appendText( message + "\n");
        textField.clear();
        textField.requestFocus();

        client.send(message);
    }


    // Btn connect pressed
    public void connect(ActionEvent actionEvent) {

        client.reconnect();
    }


    // Form closed
    public void shutdown() {
        log.info("Closing ChatForm ...");
        client.close();

        //System.exit(0)
    }




    // ==================================================================================


    public void setIncomingMessage(String message) {

        log.trace("setIncomingMessage: " + message);

        // https://stackoverflow.com/a/31444897
        // FX textarea must be accessed on FX thread.
        javafx.application.Platform.runLater(
                () -> textArea.appendText( message + "\n"));

    }

    public void setConnected(boolean connected) {

        // NotImplemented - change btnConnect color
        // Change Scroll colour to more dark

        log.trace("setConnected " + Boolean.toString(connected));

        // https://stackoverflow.com/a/31444897
        // FX textarea must be accessed on FX thread.
        javafx.application.Platform.runLater(
                () -> textArea.appendText("Server connected: " + Boolean.toString(connected) + "\n"));


    }


    public void startup() {

        try {

//            for (int i =0; i< 100; i++)
//                textArea.appendText("\n");

            // create new tcp client
            client = new EchoClient("localhost", 4321);

            // connect signals from tcp client to ChatForm
            client.addMessageListener(this::setIncomingMessage);
            client.addConnectionStateListener(this::setConnected);

            // start tcp client
            client.start();

        } catch (IOException e) {
            log.error(e);
        }

    }
}