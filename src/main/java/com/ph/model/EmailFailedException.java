package com.ph.model;

import lombok.Getter;

public class EmailFailedException extends Exception{

    @Getter
    private final String to;

    public EmailFailedException(String to){
        super("Email could not be sent.");
        this.to = to;
    }
}
