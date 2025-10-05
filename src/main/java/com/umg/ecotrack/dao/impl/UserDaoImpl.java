package com.umg.ecotrack.dao.impl;

import com.umg.ecotrack.dao.UserDao;
import com.umg.ecotrack.db.DB;
import com.umg.ecotrack.model.User;

import java.sql.*;
import java.util.Optional;

public class UserDaoImpl implements UserDao {
    @Override
    public Optional<User> login(String username, String password) {
        String sql = "SELECT TOP 1 * FROM users WHERE username=? AND password=?";
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("role")
                    ));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return Optional.empty();
    }
}
