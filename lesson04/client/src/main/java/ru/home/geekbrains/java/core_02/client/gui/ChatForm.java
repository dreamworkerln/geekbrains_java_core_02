package ru.home.geekbrains.java.core_02.client.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ChatForm extends Application {

    private static final Logger log = Logger.getLogger(MethodHandles.lookup().lookupClass());

    private static Controller controller;

    private static Consumer<ChatFormController> controllerRequest;


    @Override
    public void start(Stage primaryStage) throws Exception{

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ChatForm/sample.fxml"));
        Parent root = loader.load();
        controller = loader.getController();

        if (controllerRequest!= null)
            controllerRequest.accept(controller);


        primaryStage.setTitle("Chat 0197019.M03    -= The Emperor protects =-");
        primaryStage.setScene(new Scene(root, 400, 400));
        primaryStage.setOnHidden(e -> controller.shutdown());
        primaryStage.show();
    }


    public static void show(){
        launch();
    }


    public static void getController(Consumer<ChatFormController> request) {
        controllerRequest = request;
    }




}
