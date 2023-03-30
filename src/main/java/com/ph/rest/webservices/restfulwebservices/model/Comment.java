package com.ph.rest.webservices.restfulwebservices.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ph.model.CommentNotFoundException;
import com.ph.model.EventNotFoundException;
import com.ph.model.PersonNotFoundException;
import com.ph.persistence.model.CommentEntity;
import com.ph.persistence.model.EventEntity;
import com.ph.persistence.model.PersonEntity;
import com.ph.persistence.repository.CommentJpaRepository;
import com.ph.persistence.repository.EventJpaRepository;
import com.ph.persistence.repository.PersonJpaRepository;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Optional;

@Data
public class Comment {

    @Getter
    @Setter
    private Long id;
    @Getter
    @Setter
    private long personId;
    @Getter
    @Setter
    private long eventId;
    @Getter
    @Setter
    private Long replyToId;
    @Getter
    @Setter
    private String content;
    @Getter
    @Setter
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Date created;

    public static Comment fromEntity(CommentEntity entity){
        Comment comment = new Comment();
        comment.setId(entity.getId());
        comment.setContent(entity.getContent());
        comment.setCreated(entity.getCreated());
        comment.setPersonId(entity.getPerson().getId());
        comment.setEventId(entity.getEvent().getId());
        comment.setReplyToId(entity.getReplyTo() != null ? entity.getReplyTo().getId() : null);
        return comment;
    }

    public CommentEntity toEntity(CommentEntity oldComment, PersonJpaRepository personJpaRepository, EventJpaRepository eventJpaRepository, CommentJpaRepository commentJpaRepository) throws PersonNotFoundException, EventNotFoundException, CommentNotFoundException {
        CommentEntity entity = oldComment == null ?
                new CommentEntity() :
                oldComment;
        entity.setContent(content);
        Optional<PersonEntity> personEntity = personJpaRepository.findById(personId);
        if(personEntity.isPresent()) {
            entity.setPerson(personEntity.get());
        }else{
            throw new PersonNotFoundException(personId);
        }
        Optional<EventEntity> eventEntity = eventJpaRepository.findById(eventId);
        if(eventEntity.isPresent()) {
            entity.setEvent(eventEntity.get());
        }else{
            throw new EventNotFoundException(eventId);
        }
        if(replyToId != null){
            Optional<CommentEntity> commentEntity = commentJpaRepository.findById(replyToId);
            if(commentEntity.isPresent()) {
                entity.setReplyTo(commentEntity.get());
            }else{
                throw new CommentNotFoundException(replyToId);
            }
        }else{
            entity.setReplyTo(null);
        }

        return entity;
    }
}
