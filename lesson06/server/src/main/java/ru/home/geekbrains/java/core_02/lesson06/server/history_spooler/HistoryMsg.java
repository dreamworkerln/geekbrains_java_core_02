package ru.home.geekbrains.java.core_02.lesson06.server.history_spooler;


public class HistoryMsg {

    private int id;
    private String login;
    private String message;


    public HistoryMsg(int id, String login, String message) {
        this.id = id;
        this.login = login;
        this.message = message;
    }

    public int getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public String getMessage() {
        return message;
    }
}
