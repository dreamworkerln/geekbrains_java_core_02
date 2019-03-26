package ru.home.geekbrains.java.core_02.lesson06.server.entities;

import java.util.HashSet;
import java.util.Set;

public class User {

    private int uid;
    private String login;
    private String password;
    private Set<String> blackList = new HashSet<>();

    public User(int uid, String login, String password) {
        this.uid = uid;
        this.login = login;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public Set<String> getBlackList() {
        return blackList;
    }

    public void setBlackList(Set<String> blackList) {
        this.blackList = blackList;
    }

    public int getUid() {
        return uid;
    }

    //    public void setCredentials(String login, String password) {
//
//        this.login = login;
//        this.password = password;
//    }
}
