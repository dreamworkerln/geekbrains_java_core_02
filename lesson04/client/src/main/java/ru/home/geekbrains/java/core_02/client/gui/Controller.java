package ru.home.geekbrains.java.core_02.client.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import org.apache.log4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.function.Consumer;

public class Controller implements ChatFormController{


    private static final Logger log = Logger.getLogger(MethodHandles.lookup().lookupClass());

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



    private Consumer<Void> onFormClosed;
    private Consumer<String> onMessageSendPressed;
    private Consumer<Void> onConnectPressed;





    

    public void sendMsg() {

        String message = textField.getText();

        //textArea.appendText( message + "\n");
        textField.clear();
        textField.requestFocus();

        if (onMessageSendPressed != null)
            onMessageSendPressed.accept(message);


    }

    public void connect(ActionEvent actionEvent) {


        if (onConnectPressed != null)
            onConnectPressed.accept(null);


    }


    public void shutdown() {

        log.info("Closing ChatForm ...");

        if (onFormClosed!= null)
            onFormClosed.accept(null);

    }




    // ==================================================================================

    @Override
    public void addFormCloseListener(Consumer<Void> onFormClosed) {

        this.onFormClosed = onFormClosed;
    }



    @Override
    public void addSendPressed(Consumer<String> onMessageSendPressed) {

        this.onMessageSendPressed = onMessageSendPressed;
    }

    @Override
    public void addConnectPressed(Consumer<Void> onConnectPressed) {

        this.onConnectPressed = onConnectPressed;
    }

    // ------------------------------------------------------------------


    @Override
    public void setIncomingMessage(String message) {
        
        textArea.appendText( message + "\n");

    }

    @Override
    public void setConnected(boolean connected) {

        // NotImplemented - change btnConnect color

        // Change Scroll colour to more dark

        textArea.appendText("Server connected: " + Boolean.toString(connected) + "\n");

    }
}