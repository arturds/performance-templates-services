package com.example.template.security;

import jakarta.enterprise.context.ApplicationScoped;
import org.mindrot.jbcrypt.BCrypt;

@ApplicationScoped
public class PasswordService {

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD_HASH = BCrypt.hashpw("admin", BCrypt.gensalt());

    public boolean authenticate(String username, String password) {
        return ADMIN_USERNAME.equals(username) && BCrypt.checkpw(password, ADMIN_PASSWORD_HASH);
    }

    public boolean userExists(String username) {
        return ADMIN_USERNAME.equals(username);
    }
}
