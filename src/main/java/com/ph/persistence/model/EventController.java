package com.ph.persistence.model;

import com.ph.model.PersonNotFoundException;
import com.ph.persistence.repository.EventJpaRepository;
import com.ph.persistence.repository.PersonJpaRepository;
import com.ph.rest.webservices.restfulwebservices.controller.IController;
import com.ph.rest.webservices.restfulwebservices.model.*;
import com.ph.service.AuthService;
import io.swagger.annotations.ApiOperation;
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
@RequestMapping("/events")
public class EventController implements IController<Event, Long> {

    @Autowired
    private EventJpaRepository repository;
    @Autowired
    private PersonJpaRepository personRepository;

    @Override
    @ApiOperation(value = "Gets all stored events")
    public ResponseEntity<EventListResponse> getAll(
            @ApiParam(value = "Bearer token for authentification", required = true)
            @RequestHeader("Authorization")
            String authKey) {
        if(!AuthService.isTokenValid(authKey)){
            return AuthService.unauthorizedResponse(new EventListResponse());
        }
        System.out.println("----------------");
        List<Event> results = repository.findAll().stream().map(x -> Event.fromEntity(x)).collect(Collectors.toList());
        EventListResponse response = new EventListResponse();
        response.setRespondeCode(RepsonseCode.OK);
        response.setList(results);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    @Override
    public ResponseEntity<EventResponse> get(
            @ApiParam(value = "ID of the desired event")
            Long id,
            @ApiParam(value = "Bearer token for authentication", required = true)
            @RequestHeader("Authorization")
            String authKey) {
        if(!AuthService.isTokenValid(authKey)){
            return AuthService.unauthorizedResponse(new EventResponse());
        }

        if(repository.existsById(id)){
            Event event = Event.fromEntity(repository.findById(id).get());

            EventResponse response = new EventResponse();
            response.setRespondeCode(RepsonseCode.OK);
            response.setEvent(event);
            return new ResponseEntity(response,HttpStatus.OK);
        }else{
            EventResponse response = new EventResponse();
            response.setRespondeCode(RepsonseCode.UNKNOWN_ID);
            return new ResponseEntity(response,HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<Response> delete(
            @ApiParam(value = "ID of the target event")
            Long id,
            @ApiParam(value = "Bearer token for authentication", required = true)
            @RequestHeader("Authorization")
            String authKey) {
        if(!AuthService.isTokenValid(authKey)){
            return AuthService.unauthorizedResponse(new Response());
        }

        if(repository.existsById(id)){
            repository.deleteById(id);

            Response response = new Response();
            response.setRespondeCode(RepsonseCode.DELETE_SUCCESSFULL);
            response.setMessage("Deleted event with ID: "+id);
            return new ResponseEntity<>(response,HttpStatus.OK);
        }else{
            Response response = new Response();
            response.setRespondeCode(RepsonseCode.UNKNOWN_ID);
            return new ResponseEntity(response,HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<ResourceIdResponse<Long>> update(
            @ApiParam(value = "ID of the target event")
            Long id,
            @ApiParam(value = "Event information to be stored")
            Event event,
            @ApiParam(value = "Bearer token for authentication", required = true)
            @RequestHeader("Authorization")
            String authKey) {
        if(!AuthService.isTokenValid(authKey)){
            return AuthService.unauthorizedResponse(new ResourceIdResponse<Long>());
        }

        if(event != null){
            Optional<EventEntity> oldEvent = repository.findById(id);
            if(oldEvent.isPresent()){
                return saveEvent(event,oldEvent.get());
            }else{
                Response response = new Response();
                response.setRespondeCode(RepsonseCode.UNKNOWN_ID);
                return new ResponseEntity(response,HttpStatus.BAD_REQUEST);
            }
        }else{
            ResourceIdResponse response = new ResourceIdResponse();
            response.setRespondeCode(RepsonseCode.UPDATE_FAILED);
            response.setMessage("Given event was null");
            return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<ResourceIdResponse<Long>> create(
            @ApiParam(value = "Event information to be stored")
            Event event,
            @ApiParam(value = "Bearer token for authentication", required = true)
            @RequestHeader("Authorization")
            String authKey) {
        if(!AuthService.isTokenValid(authKey)){
            return AuthService.unauthorizedResponse(new ResourceIdResponse<Long>());
        }

        if(event != null){
            return saveEvent(event, null);
        }else{
            ResourceIdResponse response = new ResourceIdResponse();
            response.setRespondeCode(RepsonseCode.UPDATE_FAILED);
            response.setMessage("Given event was null");
            return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
        }
    }

    private ResponseEntity<ResourceIdResponse<Long>> saveEvent(Event event, EventEntity oldEvent){
        try {
            EventEntity eventEntity = event.toEntity(oldEvent, personRepository);

            EventEntity eventUpdated = repository.save(eventEntity);

            ResourceIdResponse<Long> response = new ResourceIdResponse();
            response.setResourceId(eventUpdated.getId());
            response.setRespondeCode(RepsonseCode.SAVE_SUCCESSFULL);
            response.setMessage("Saved event with id: "+eventUpdated.getId());
            return new ResponseEntity<>(response, HttpStatus.OK);
        }catch (PersonNotFoundException ex){
            ResourceIdResponse<Long> response = new ResourceIdResponse();
            response.setRespondeCode(RepsonseCode.SAVE_FAILED);
            response.setMessage(ex.getMessage());
            return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
        }
    }
}
