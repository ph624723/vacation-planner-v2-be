package com.ph.rest.webservices.restfulwebservices.model;

import lombok.Getter;
import lombok.Setter;

public class PersonResponse extends Response<Person>{

    @Getter
    @Setter
    private Person person;
}
