package com.umg.ecotrack.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DB {

    private static final String URL =
        "jdbc:sqlserver://DESKTOP-QVO2VV2;databaseName=EcoTrack;encrypt=true;trustServerCertificate=true";

    // Usuario creado especialmente para EcoTrack (no usa 'sa')
    private static final String USER = "ecotrack_user";
    private static final String PASS = "EcoTrack123$";

    public static Connection get() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}

