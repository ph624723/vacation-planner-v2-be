package com.ph.rest.webservices.restfulwebservices.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class AbsenceList {

    @Getter
    @Setter
    private Long personId;

    @Getter
    @Setter
    private List<Absence> absences;
}
