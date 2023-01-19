package com.ph.rest.webservices.restfulwebservices.model;

import lombok.Getter;
import lombok.Setter;

import java.sql.Date;

public class CalendarEvent {
    @Getter
    @Setter
    private long duration;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private Date start;

    @Getter
    @Setter
    private Date end;

    public CalendarEvent(long d){
        duration = d;
    }

    public CalendarEvent(long d, String n){
        this(d);
        name = n;
    }

    public CalendarEvent(long d, String n, Date start, Date end){
        this(d,n);
        this.start = start;
        this.end = end;

    }
}
