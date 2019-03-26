package ru.home.geekbrains.java.core_02.lesson06.server.utils;

import org.apache.log4j.Logger;
import ru.home.geekbrains.java.core_02.lesson06.server.entities.User;

import java.lang.invoke.MethodHandles;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class DAOCrutch {

    private static final Logger log = Logger.getLogger(MethodHandles.lookup().lookupClass());

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

    public static User loadUser(String login, String password) {

        User result = null;

        boolean authenticated = false;
        int id = -1;

        try {
            String sql = String.format("SELECT id, hash FROM user WHERE login = '%1$s'", login);

            ResultSet rs = stmt.executeQuery(sql);

            if (rs.next()) {
                String hash = rs.getString(2);
                id = rs.getInt(1);

                if (!hash.equals("")) {
                    authenticated = BcryptAuth.check(password, hash);
                }
            }

            // Client successfully authenticated - loading blacklist
            if(authenticated) {

                result = new User(id, login, password);

                sql = String.format("SELECT user.login FROM user " +
                                    "LEFT JOIN blacklist ON user.id = blacklist.blocked_id " +
                                    "WHERE blacklist.user_id = '%1$d'", id);
                rs = stmt.executeQuery(sql);

                Set<String> blacklist = new HashSet<>();


                while(rs.next()) {
                    String tmp = rs.getString(1);

                    // PERMABAN
                    if (tmp.equals(result.getLogin()))
                        return null;

                    blacklist.add(tmp);
                }

                result.setBlackList(blacklist);
            }
        }
        catch (Exception e) {
            log.error(e);
        }

        return result;
    }


    public static void banUser(User user, String banLogin) {

        try {

            String sql = String.format("INSERT INTO blacklist (user_id, blocked_id) " +
                                       "SELECT %1$d, id FROM user WHERE login = '%2$s'",  user.getUid(), banLogin);

            stmt.execute(sql);
        }
        catch (Exception e) {
            log.error(e);
        }
    }

    public static void unBanUser(User user ,String unBanLogin) {

        try {

            String sql = String.format("DELETE FROM blacklist " +
                                       "WHERE user_id = %1$d AND " +
                                       "blocked_id IN " +
                                       "(SELECT id FROM user WHERE " +
                                       "login = '%2$s')", user.getUid(), unBanLogin);

            stmt.execute(sql);
        }
        catch (Exception e) {
            log.error(e);
        }
    }


    public static void disconnect() {
        try {
            connection.close();
        }
        catch (Exception ignored) {}
    }

}
