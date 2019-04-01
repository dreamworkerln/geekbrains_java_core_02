package ru.home.geekbrains.java.core_02.lesson06.server.entities.dao;

import org.apache.log4j.Logger;
import ru.home.geekbrains.java.core_02.lesson06.server.history_spooler.HistoryMsg;

import java.lang.invoke.MethodHandles;
import java.sql.*;
import java.util.Map;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class HistoryDAO {

    private static final Logger log = Logger.getLogger(MethodHandles.lookup().lookupClass());

/*  Oracle strongly discourages sharing a database connection among multiple threads.
    Avoid allowing multiple threads to access a connection simultaneously.
    If multiple threads must share a connection, use a disciplined begin-using/end-using protocol.*/

    private static Connection connection;
    //private static Statement stmt;


    static  {

        // Using own Statement (when perform new query then old statement resultSet has been erased)
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:userDB.db");
            connection.setAutoCommit(false);
        } catch (Exception e) {

            log.error(e);

        }
    }


    public static void saveHistory(ConcurrentNavigableMap<Integer, HistoryMsg> history) throws SQLException {

        String sql = "INSERT INTO history (id, login, message) VALUES(?,?,?)";
        PreparedStatement pstmt = connection.prepareStatement(sql);

        for(Map.Entry<Integer, HistoryMsg> entry : history.entrySet()) {

            pstmt.setInt(1, entry.getValue().getId());
            pstmt.setString(2, entry.getValue().getLogin());
            pstmt.setString(3, entry.getValue().getMessage());
            pstmt.executeUpdate();

        }
        connection.commit();
    }



    public static ConcurrentNavigableMap<Integer, HistoryMsg> load()  {

        ConcurrentNavigableMap<Integer, HistoryMsg> result = new ConcurrentSkipListMap<>();


        try {

            try (Statement st = connection.createStatement()) {

                String sql = "SELECT id, time, login, message FROM history";

                ResultSet rs = st.executeQuery(sql);
                while (rs.next()) {
                    int id = rs.getInt(1);
                    String login = rs.getString(3);
                    String message = rs.getString(4);

                    result.put(id, new HistoryMsg(id, login, message));
                }
            }
        }
        catch (Exception e) {
            log.error(e);
        }

        return result;
    }



    public static int getLastId()  {

        int result = -1;


        try {

            try (Statement st = connection.createStatement()) {

                String sql = "SELECT MAX(id) FROM history";
                ResultSet rs = st.executeQuery(sql);
                if (rs.next()) {
                    result = rs.getInt(1);
                }
            }
        }
        catch (Exception e) {
            log.error(e);
        }

        return result;
    }

}
