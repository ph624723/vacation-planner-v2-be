package com.ph.rest.webservices.restfulwebservices.model;

import lombok.Getter;
import lombok.Setter;

public class EventResponse extends Response<Event>{

    @Getter
    @Setter
    private Event event;
}
