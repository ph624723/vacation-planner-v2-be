package com.ph.rest.webservices.restfulwebservices.viewmodel;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ph.model.EventNotFoundException;
import com.ph.model.PersonNotFoundException;
import com.ph.persistence.model.CommentEntity;
import com.ph.persistence.model.EventEntity;
import com.ph.persistence.model.PersonEntity;
import com.ph.persistence.repository.EventJpaRepository;
import com.ph.persistence.repository.PersonJpaRepository;
import com.ph.rest.webservices.restfulwebservices.model.Person;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Optional;

public class CommentViewModel {

    @Getter
    @Setter
    private String id;
    @Getter
    @Setter
    private Person person;

    @Getter
    @Setter
    private String content;
    @Getter
    @Setter
    private CommentViewModel replyTo;
    @Getter
    @Setter
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Date created;
    @Getter
    @Setter
    private boolean editable = false;

    public static CommentViewModel fromEntity(CommentEntity entity){
        CommentViewModel comment = new CommentViewModel();
        comment.setId(entity.getId().toString());
        comment.setContent(entity.getContent());
        comment.setCreated(entity.getCreated());
        comment.setPerson(Person.fromEntity(entity.getPerson()));
        if(entity.getReplyTo() != null)
            comment.setReplyTo(CommentViewModel.fromEntity(entity.getReplyTo()));
        return comment;
    }
}
