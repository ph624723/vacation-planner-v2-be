package com.ph.persistence.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ph.rest.webservices.restfulwebservices.model.Absence;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "person")
public class PersonEntity {

    @Id
    @GeneratedValue
    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String contact;

    @Getter
    @Setter
    @OneToOne(mappedBy = "personData")
    private UserEntity user;

    @Getter
    @Setter
    @OneToMany(mappedBy = "person")
    @Cascade(org.hibernate.annotations.CascadeType.ALL)
    private List<AbsenceEntity> absences;

    @Getter
    @Setter
    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;
    @Getter
    @Setter
    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date updated;

    @Override
    public String toString(){
        return "[\n"+
                "ID:"+id+"\n"+
                "name:"+name+"\n"+
                "contact:"+contact+"\n"+
                "userId:"+user.getName()+
                "\n]";
    }
}
