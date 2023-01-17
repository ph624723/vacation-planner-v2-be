package com.ph.rest.webservices.restfulwebservices.model;

import lombok.Getter;
import lombok.Setter;

public class CalendarEvent {
    @Getter
    @Setter
    private long duration;

    @Getter
    @Setter
    private String name;

    public CalendarEvent(long d){
        duration = d;
    }

    public CalendarEvent(long d, String n){
        this(d);
        name = n;
    }
}
