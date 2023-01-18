package com.ph.rest.webservices.restfulwebservices.model;

import com.ph.persistence.model.PersonEntity;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
import java.util.List;

public class EventPlannerConfig {

    @Getter
    @Setter
    private List<Long> personIds;

    @Getter
    @Setter
    private int ignoreAbsenceToLevel;

    @Getter
    @Setter
    private Date start;

    @Getter
    @Setter
    private Date end;
}
