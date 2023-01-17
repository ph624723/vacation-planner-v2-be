package com.ph.rest.webservices.restfulwebservices.model;

import lombok.Getter;
import lombok.Setter;

public class DayOfMonth {

    @Getter
    private final String number;

    public DayOfMonth(int number){
        this.number = number+"";
    }
}
