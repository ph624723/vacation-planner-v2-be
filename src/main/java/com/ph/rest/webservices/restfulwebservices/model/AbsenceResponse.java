package com.ph.rest.webservices.restfulwebservices.model;

import lombok.Getter;
import lombok.Setter;

public class AbsenceResponse extends Response<Absence>{

    @Getter
    @Setter
    private Absence absence;
}
