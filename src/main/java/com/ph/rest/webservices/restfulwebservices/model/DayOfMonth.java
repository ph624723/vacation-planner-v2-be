package com.ph.rest.webservices.restfulwebservices.model;

import lombok.Getter;
import lombok.Setter;

public class DayOfMonth {

    @Getter
    private final String number;

    @Getter
    private final boolean weekend;

    public DayOfMonth(int number, boolean weekend){
        this.number = number+"";
        this.weekend = weekend;
    }
}
