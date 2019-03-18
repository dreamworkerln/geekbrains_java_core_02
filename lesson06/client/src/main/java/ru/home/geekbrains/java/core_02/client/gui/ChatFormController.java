package ru.home.geekbrains.java.core_02.client.gui;

import java.util.function.Consumer;

public interface ChatFormController {

    void addFormCloseListener(Consumer<Void> onFormClosed);
    void addSendPressed(Consumer<String> messageSendHandler);
    void addConnectPressed(Consumer<Void> connectHandler);


    void setIncomingMessage(String message);
    void setConnected(boolean connected);

}
