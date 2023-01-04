package com.ph.model;

public class UserNotFoundException extends Exception{
    public UserNotFoundException(String userName){
        super("No user was found for name: "+userName);
    }
}
