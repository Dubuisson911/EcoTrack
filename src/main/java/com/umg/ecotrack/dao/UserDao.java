package com.umg.ecotrack.dao;

import com.umg.ecotrack.model.User;

public interface UserDao {
    /**
     * Autentica contra la tabla `users` (texto plano según tu script actual).
     * Retorna el User con id, username y role si es válido; de lo contrario, null.
     */
    User authenticate(String username, String password);
}
