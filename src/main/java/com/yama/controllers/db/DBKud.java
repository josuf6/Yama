package com.yama.controllers.db;

import com.yama.Main;

import java.sql.*;

public class DBKud {

    private static final DBKud nDBKud = new DBKud();
    Connection conn = null;

    public static DBKud getDBKud(){
        return nDBKud;
    }

    private DBKud() {
        this.conOpen(Main.class.getResource("DB/yama.db").getPath());
    }

    private void conOpen(String dbpath) {
        try {
            String url = "jdbc:sqlite:"+ dbpath ;
            conn = DriverManager.getConnection(url);

            System.out.println("Database connection established");
        } catch (Exception e) {
            System.err.println("Cannot connect to database server " + e);
        }
    }

    private void conClose() {

        if (conn != null)
            try {
                conn.close();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        System.out.println("Database connection terminated");

    }

    public ResultSet execSQL(String query, Object[] datuak) throws SQLException {
        int count = 0;
        PreparedStatement s = null;
        ResultSet rs = null;

        s = conn.prepareStatement(query);
        for (int i = 0; i < datuak.length; i++) {
            s.setObject(i + 1, datuak[i]);
        }
        if (query.toLowerCase().indexOf("select") == 0) {
            // select agindu bat
            rs = s.executeQuery();

        } else {
            // update, delete, update agindu bat
            count = s.executeUpdate();
            System.out.println(count + " rows affected");
        }

        return rs;
    }
}
