package com.ph.rest.webservices.restfulwebservices.model;

import lombok.Getter;
import lombok.Setter;

public class Response<T> {

    @Getter
    @Setter
    private String message;

    @Getter
    @Setter
    private RepsonseCode respondeCode;

}
