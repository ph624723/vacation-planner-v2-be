package com.ph.rest.webservices.restfulwebservices.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class CalendarPerson {

    @Getter
    @Setter
    private Person person;

    @Getter
    @Setter
    private List<CalendarEvent> calendarEvents;
}
