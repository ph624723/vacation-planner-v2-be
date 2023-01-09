package com.ph.model;

public class ToManySessionsException extends Exception{
    public ToManySessionsException(){
        super("The maximum number of concurrent sessions has been reached. No further authentications will be issued momentarily.");
    }
}
