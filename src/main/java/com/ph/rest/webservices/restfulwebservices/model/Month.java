package com.ph.rest.webservices.restfulwebservices.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class Month {

    @Getter
    private List<DayOfMonth> days;

    @Getter
    @Setter
    private String name;

    @Getter
    private int length;

    public void setDays(List<DayOfMonth> dList){
        days = dList;
        length = dList != null ? dList.size() : 0;
    }
}
