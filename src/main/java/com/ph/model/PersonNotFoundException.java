package com.ph.model;

import java.util.List;
import java.util.stream.Collectors;

public class PersonNotFoundException extends Exception{

    public PersonNotFoundException(String contact){
        super("No person was found for mail address: "+contact);
    }
    public PersonNotFoundException(Long id){
        super("No person was found for id: "+id);
    }

    public PersonNotFoundException(List<Long> ids){
        super("No person was found for ids: "+ids.stream().map(x -> x.toString()).collect(Collectors.joining(", ")));
    }
}
