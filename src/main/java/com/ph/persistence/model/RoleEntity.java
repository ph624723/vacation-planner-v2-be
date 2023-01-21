package com.ph.persistence.model;

import lombok.Getter;
import lombok.Setter;

import javax.management.relation.Role;
import javax.persistence.*;

@Entity
@Table(name = "role")
public class RoleEntity {

    @Id
    @Getter
    @Setter
    private String name;

    public RoleEntity(){}

    public RoleEntity (String name){
        this.name = name;
    }
}
