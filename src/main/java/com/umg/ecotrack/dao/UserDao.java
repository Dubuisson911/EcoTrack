package com.umg.ecotrack.dao;

import com.umg.ecotrack.model.User;
import java.util.Optional;

public interface UserDao {
    Optional<User> login(String username, String password);
}
