package com.ph.model;

import lombok.Getter;

public class UserNameInUseException extends Exception{

    @Getter
    private final String username;

    public UserNameInUseException(String username){
        super("Username already in use.");
        this.username = username;
    }
}
