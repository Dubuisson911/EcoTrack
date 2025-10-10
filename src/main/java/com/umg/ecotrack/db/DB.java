package com.umg.ecotrack.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DB {

    // Ajusta a tus valores reales:
    private static final String URL  =
        "jdbc:sqlserver://DESKTOP-QVO2VV2;databaseName=EcoTrack;encrypt=true;trustServerCertificate=true";
    private static final String USER = "ecotrack_user";
    private static final String PASS = "EcoTrack911";

    /** Método oficial que usan los DAOs nuevos */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    /** Alias para compatibilidad con DAOs antiguos (Point/Collection) */
    public static Connection get() throws SQLException {
        return getConnection();
    }
}
