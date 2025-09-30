package com.devsu.customer.infrastructure.security;

import com.devsu.customer.usecase.port.outbound.SecurityUtil;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;

@Component
public class CryptoServiceAdapter implements SecurityUtil {
    @Override
    public String hashPassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password must not be null or blank");
        }
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
}
