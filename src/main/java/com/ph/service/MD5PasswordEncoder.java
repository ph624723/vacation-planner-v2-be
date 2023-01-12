package com.ph.service;

import org.springframework.security.crypto.password.PasswordEncoder;

public class MD5PasswordEncoder implements PasswordEncoder {
    @Override
    public String encode(CharSequence plainTextPassword) {
        return HashService.MD5(plainTextPassword.toString());
    }

    @Override
    public boolean matches(CharSequence plainTextPassword, String pwInDatabase) {
        return HashService.MD5(plainTextPassword.toString()).equals(pwInDatabase);
    }
}
