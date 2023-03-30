package com.ph.rest.webservices.restfulwebservices.controller;

import com.ph.model.CommentNotFoundException;
import com.ph.model.EventNotFoundException;
import com.ph.model.PersonNotFoundException;
import com.ph.persistence.model.AbsenceEntity;
import com.ph.persistence.model.CommentEntity;
import com.ph.persistence.repository.CommentJpaRepository;
import com.ph.persistence.repository.EventJpaRepository;
import com.ph.persistence.repository.PersonJpaRepository;
import com.ph.rest.webservices.restfulwebservices.model.*;
import com.ph.service.AuthService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/events/comments")
@Api(tags = {"Events"})
public class CommentController implements IController<Comment, Long> {

    @Autowired
    CommentJpaRepository commentRepository;
    @Autowired
    private PersonJpaRepository personRepository;
    @Autowired
    private EventJpaRepository eventRepository;

    @Override
    public ResponseEntity<CommentListResponse> getAll(
            @ApiParam(value = "Bearer token for authentification", required = true)
            @RequestHeader("Authorization")
            String authKey
    ) {
        if(!AuthService.isTokenValid(authKey)){
            if(!AuthService.isTokenValid(authKey)){
                return AuthService.unauthorizedResponse(new CommentListResponse());
            }
        }

        List<Comment> results = commentRepository.findAll().stream().map(x -> Comment.fromEntity(x)).collect(Collectors.toList());
        CommentListResponse response = new CommentListResponse();
        response.setRespondeCode(RepsonseCode.OK);
        response.setList(results);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<CommentResponse> get(
            @ApiParam(value = "ID of the desired comment")
            Long id,
            @ApiParam(value = "Bearer token for authentification", required = true)
            @RequestHeader("Authorization")
            String authKey
    ) {
        if(!AuthService.isTokenValid(authKey)){
            return AuthService.unauthorizedResponse(new CommentResponse());
        }

        if(commentRepository.existsById(id)){
            Comment comment = Comment.fromEntity(commentRepository.findById(id).orElse(null));

            CommentResponse response = new CommentResponse();
            response.setRespondeCode(RepsonseCode.OK);
            response.setComment(comment);
            return new ResponseEntity(response,HttpStatus.OK);
        }else{
            CommentResponse response = new CommentResponse();
            response.setRespondeCode(RepsonseCode.UNKNOWN_ID);
            response.setComment(null);
            return new ResponseEntity(response,HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<Response> delete(
            @ApiParam(value = "ID of the desired comment")
            Long id,
            @ApiParam(value = "Bearer token for authentification", required = true)
            @RequestHeader("Authorization")
            String authKey) {
        if(!AuthService.isTokenValid(authKey)){
            return AuthService.unauthorizedResponse(new CommentResponse());
        }
        if(commentRepository.existsById(id)){
            for (CommentEntity replier : commentRepository.findById(id).get().getRepliedBy()) {
                replier.setReplyTo(null);
                commentRepository.save(replier);
            }

            commentRepository.deleteById(id);

            Response response = new Response();
            response.setRespondeCode(RepsonseCode.DELETE_SUCCESSFULL);
            response.setMessage("Deleted comment with ID: "+id);
            return new ResponseEntity<>(response,HttpStatus.OK);
        }else{
            Response response = new Response();
            response.setRespondeCode(RepsonseCode.UNKNOWN_ID);
            return new ResponseEntity(response,HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<ResourceIdResponse<Long>> update(
            @ApiParam(value = "ID of the desired comment")
            Long id,
            @ApiParam(value = "Comment information to be stored")
            Comment element,
            @ApiParam(value = "Bearer token for authentification", required = true)
            @RequestHeader("Authorization")
            String authKey) {
        if(!AuthService.isTokenValid(authKey)){
            return AuthService.unauthorizedResponse(new ResourceIdResponse<Long>());
        }

        if(element != null){
            Optional<CommentEntity> oldComment = commentRepository.findById(id);
            if(oldComment.isPresent()){
                return saveComment(element,oldComment.get());
            }else{
                Response response = new Response();
                response.setRespondeCode(RepsonseCode.UNKNOWN_ID);
                return new ResponseEntity(response,HttpStatus.BAD_REQUEST);
            }
        }else{
            ResourceIdResponse response = new ResourceIdResponse();
            response.setRespondeCode(RepsonseCode.UPDATE_FAILED);
            response.setMessage("Given comment was null");
            return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<ResourceIdResponse<Long>> create(
            @ApiParam(value = "Comment information to be stored")
            Comment element,
            @ApiParam(value = "Bearer token for authentification", required = true)
            @RequestHeader("Authorization")
            String authKey) {
        if(!AuthService.isTokenValid(authKey)){
            return AuthService.unauthorizedResponse(new ResourceIdResponse<Long>());
        }

        if(element != null){
            return saveComment(element, null);
        }else{
            ResourceIdResponse response = new ResourceIdResponse();
            response.setRespondeCode(RepsonseCode.UPDATE_FAILED);
            response.setMessage("Given comment was null");
            return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
        }
    }

    private ResponseEntity<ResourceIdResponse<Long>> saveComment(Comment comment, CommentEntity oldComment){
        try {
            CommentEntity commentEntity = comment.toEntity(oldComment, personRepository, eventRepository, commentRepository);

            CommentEntity commentUpdated = commentRepository.save(commentEntity);

            ResourceIdResponse<Long> response = new ResourceIdResponse();
            response.setResourceId(commentUpdated.getId());
            response.setRespondeCode(RepsonseCode.SAVE_SUCCESSFULL);
            response.setMessage("Saved comment with id: "+commentUpdated.getId());
            return new ResponseEntity<>(response, HttpStatus.OK);
        }catch (PersonNotFoundException | EventNotFoundException | CommentNotFoundException ex){
            ResourceIdResponse<Long> response = new ResourceIdResponse();
            response.setRespondeCode(RepsonseCode.SAVE_FAILED);
            response.setMessage(ex.getMessage());
            return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
        }
    }
}
