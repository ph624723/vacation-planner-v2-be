package com.ph.model;

import java.util.List;
import java.util.stream.Collectors;

public class EventNotFoundException extends Exception{

    public EventNotFoundException(Long id){
        super("No event was found for id: "+id);
    }
}
