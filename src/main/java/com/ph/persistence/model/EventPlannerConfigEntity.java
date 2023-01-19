package com.ph.persistence.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.sql.Date;
import java.util.List;

@Entity
public class EventPlannerConfigEntity {

    @Id
    @GeneratedValue
    @Getter
    @Setter
    private Long id;

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
