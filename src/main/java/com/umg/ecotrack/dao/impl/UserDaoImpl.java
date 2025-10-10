package com.umg.ecotrack.dao.impl;

import com.umg.ecotrack.dao.UserDao;
import com.umg.ecotrack.model.User;
import com.umg.ecotrack.db.DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDaoImpl implements UserDao {

    private static final String SQL_AUTH =
        "SELECT id, username, role FROM users WHERE username = ? AND password = ?";

    @Override
    public User authenticate(String username, String password) {
        try (Connection cn = DB.getConnection();
             PreparedStatement ps = cn.prepareStatement(SQL_AUTH)) {

            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("role")
                    );
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
