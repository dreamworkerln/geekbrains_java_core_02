package ru.home.geekbrains.java.core_02.lesson06.client.gui;

import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.log4j.Logger;
import ru.home.geekbrains.java.core_02.lesson06.client.EchoClient;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Controller {


    private static final Logger log = Logger.getLogger(MethodHandles.lookup().lookupClass());

    @FXML
    public VBox main;

    @FXML
    public Label label;

    @FXML
    public TextField loginField;

    @FXML
    public PasswordField passwordField;

    @FXML
    public HBox AuthPanel;

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

    @FXML
    ListView<String> clientList;

    // =========================================================================================

    private EchoClient client = null;

    private String currentSelected = null;

    public Controller() {


    }



    // Btn send pressed
    public void sendMsgHandler() {

        String message = textField.getText();

        //textArea.appendText( message + "\n");
        textField.clear();
        textField.requestFocus();

        if (currentSelected != null) {
            message = "/p" + " " + currentSelected + " " + message;
        }

        client.send(message);
    }


    // Btn connectHandler pressed
    public void connectHandler(ActionEvent actionEvent) {

        client.setCredentals(loginField.getText(), passwordField.getText());
        client.connect();
    }


    // Form closed
    public void shutdown() {
        log.info("Closing ChatForm ...");
        client.close();

        //System.exit(0)
    }




    // ==================================================================================


    private void setIncomingMessage(String message) {

        log.trace("setIncomingMessage: " + message);

        if (message.contains("/clientlist")) {

            List<String> tmp = new ArrayList<>(Arrays.asList(message.split("\\s")));
            tmp.remove(0);
            setClientListUpload(tmp);
            return;
        }

        if (message.contains("/clear")) {

            javafx.application.Platform.runLater(()->
                    textArea.clear());
            return;
        }



        // https://stackoverflow.com/a/31444897
        // FX textarea must be accessed on FX thread.
        javafx.application.Platform.runLater(
                () -> textArea.appendText( message + "\n"));

    }

    private void setConnected(boolean connected) {

        // NotImplemented - change btnConnect color
        // Change Scroll colour to more dark

        log.trace("setConnected " + Boolean.toString(connected));

        // https://stackoverflow.com/a/31444897
        // FX textarea must be accessed on FX thread.
        javafx.application.Platform.runLater(
                () -> {
                    textArea.appendText("Server connected: " + Boolean.toString(connected) + "\n");
                    AuthPanel.setVisible(!connected);
                    AuthPanel.setManaged(!connected);
                    clientList.getItems().clear();
                });

    }



    private void setClientListUpload(List<String> list) {


        try {
            javafx.application.Platform.runLater(() -> {

                currentSelected = clientList.getSelectionModel().getSelectedItem();

                clientList.getItems().clear();

                for (String cl : list) {
                    clientList.getItems().add(cl);
                }

                if (currentSelected != null)
                    clientList.getSelectionModel().select(currentSelected);
            });

        } catch (Exception e) {
            log.error(e);
        }


    }

//    private void setClientListChanged(String name, Boolean actDel) {
//
//
//        try {
//
//            if (actDel) {
//                clientList.getItems().remove(name);
//            }
//            else {
//                clientList.getItems().add(name);
//            }
//
//        } catch (Exception e) {
//            log.error(e);
//        }
//    }



    public void listViewMouseClickHandler(MouseEvent mouseEvent) {

        log.info(mouseEvent);


        javafx.application.Platform.runLater(() -> {

            String s = clientList.getSelectionModel().getSelectedItem();


            if (s == null ||
                s.equals(currentSelected)) {

                currentSelected = null;
                clientList.getSelectionModel().clearSelection();
                textField.setPromptText("Enter message ...");

            } else {

                currentSelected = s;
                clientList.getSelectionModel().select(currentSelected);
                textField.setPromptText("Enter message to '" + s + "'...");
            }


        });

    }


    void startup() {

        try {

//            for (int i =0; i< 1; i++)
//                textArea.appendText("\n");

//            for (int i =0; i< 1; i++) {
//                clientList.getItems().add("AAAAAAAAA");
//            }

            // create new tcp client
            client = new EchoClient("localhost", 4321);

            // connectHandler signals from tcp client to ChatForm
            client.addMessageListener(this::setIncomingMessage);
            client.addConnectionStateListener(this::setConnected);
//            client.addClientListUploadListener(this::setClientListUpload);
//            client.addClientListChangedListener(this::setClientListChanged);


            // start tcp client
            client.start();

        } catch (IOException e) {
            log.error(e);
        }

    }

}