package com.ph.model;

public class PersonNotFoundException extends Exception{
    public PersonNotFoundException(Long id){
        super("No person was found for id: "+id);
    }
}
