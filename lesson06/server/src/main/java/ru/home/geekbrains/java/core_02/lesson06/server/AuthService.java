package ru.home.geekbrains.java.core_02.lesson06.server;

import java.sql.*;

public class AuthService {

    private static Connection connection;
    private static Statement stmt;

    public static void connect() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:userDB.db");
            stmt = connection.createStatement();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static boolean getNickByLoginAndPass(String login, String password) {

        boolean result = false;

        try {
            String sql = String.format("SELECT hash FROM auth WHERE login = '%1$s'", login);
            ResultSet rs = stmt.executeQuery(sql);

            if (rs.next()) {

                String hash = rs.getString(1);

                if (!hash.equals("")) {

                    result = BcryptAuth.check(password, hash);
                }
            }
        }
        catch (Exception ignored) {}

        return result;
    }


    public static void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
