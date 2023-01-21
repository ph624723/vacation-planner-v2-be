package com.ph.rest.webservices.restfulwebservices.model;

import com.ph.persistence.model.PersonEntity;
import com.ph.persistence.model.RoleEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

public class Person {
    @ApiModelProperty(position = 0, value = "", required = false)
    @Getter
    @Setter
    private Long id;
    @ApiModelProperty(position = 1, required = true)
    @Getter
    @Setter
    private String name;
    @ApiModelProperty(position = 2, required = false)
    @Getter
    @Setter
    private String contact;
    @Getter
    @Setter
    private String userId;

    @Getter
    @Setter
    private List<String> roleNames;

    public static Person fromEntity(PersonEntity entity){
        Person person = new Person();
        person.setId(entity.getId());
        person.setName(entity.getName());
        person.setContact(entity.getContact());
        person.setUserId(entity.getUser() != null ? entity.getUser().getName() : "");
        person.setRoleNames(entity.getRoles().stream().map(x -> x.getName()).collect(Collectors.toList()));
        //person.setEventIds(entity.getEvents().stream().map(x -> x.getId()).collect(Collectors.toList()));
        return person;
    }

    public PersonEntity toEntity(PersonEntity oldPerson) {
        PersonEntity entity = oldPerson == null ?
                new PersonEntity() :
                oldPerson;
        //entity.setEvents(eventIds.);
        entity.setName(name);
        entity.setContact(contact);
        if(roleNames != null){
            entity.setRoles(roleNames.stream().map(x -> new RoleEntity(x)).collect(Collectors.toList()));
        }
        return entity;
    }

    @Override
    public String toString(){
        return "[\n"+
                "ID:"+id+"\n"+
                "name:"+name+"\n"+
                "contact:"+contact+"\n"+
                "userId:"+userId+
                "\n]";
    }
}
