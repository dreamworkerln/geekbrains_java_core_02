package ru.home.geekbrains.java.core_02.lesson06.client.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ChatForm extends Application {

    private static Logger log;

    private Controller controller;

    @Override
    public void start(Stage primaryStage) throws Exception{

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ChatForm/sample.fxml"));
        Parent root = loader.load();
        controller = loader.getController();

        primaryStage.setTitle("Chat 0197019.M03    -= The Emperor protects =-");
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.setOnShown(e -> controller.startup());
        primaryStage.setOnHidden(e -> controller.shutdown());
        primaryStage.show();

        // restrict minimal size
        primaryStage.setMinWidth(primaryStage.getWidth());
        primaryStage.setMinHeight(primaryStage.getHeight());
    }



    public static void main(String[] args) throws Exception {

        Thread.sleep(1000); // fuckin idea debugger
        setupLog4J();

        // Create ChatForm, and block current thread till form closed
        launch();
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
