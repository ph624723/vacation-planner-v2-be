package com.ph.rest.webservices.restfulwebservices.controller;

import com.ph.model.UserNotFoundException;
import com.ph.persistence.model.PersonEntity;
import com.ph.persistence.model.UserEntity;
import com.ph.persistence.repository.PersonJpaRepository;
import com.ph.persistence.repository.UserJpaRepository;
import com.ph.rest.webservices.restfulwebservices.model.*;
import com.ph.service.AuthService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/persons")
@Api(tags = {"Persons"}, description = "Register of persons that can own absences or participate in events.")
public class PersonController {
	
	@Autowired
	private PersonJpaRepository repository;

	@Autowired
	private UserJpaRepository userRepository;

	@GetMapping("/")
	@ApiOperation(value = "Gets all stored persons")
	public ResponseEntity<PersonListResponse> getAll(
			@ApiParam(value = "Bearer token for authentication", required = true)
			@RequestHeader("Authorization")
			String authKey){
		if(!AuthService.isTokenValid(authKey)){
			return AuthService.unauthorizedResponse(new PersonListResponse());
		}


		List<Person> results = repository.findAll().stream().map(x -> Person.fromEntity(x)).collect(Collectors.toList());
		PersonListResponse response = new PersonListResponse();
		response.setRespondeCode(RepsonseCode.OK);
		response.setList(results);
		return new ResponseEntity<>(response,HttpStatus.OK);
	}

	@GetMapping("/{id}")
	@ApiOperation(value = "Get a single person by ID")
	public ResponseEntity<PersonResponse> get(
			@ApiParam(value = "ID of the desired person")
			@PathVariable
			Long id,
			@ApiParam(value = "Bearer token for authentication", required = true)
			@RequestHeader("Authorization")
			String authKey){
		if(!AuthService.isTokenValid(authKey)){
			return AuthService.unauthorizedResponse(new PersonResponse());
		}

		if(repository.existsById(id)){
			Person person = Person.fromEntity(repository.findById(id).orElse(null));

			PersonResponse response = new PersonResponse();
			response.setRespondeCode(RepsonseCode.OK);
			response.setPerson(person);
			return new ResponseEntity(response,HttpStatus.OK);
		}else{
			PersonResponse response = new PersonResponse();
			response.setRespondeCode(RepsonseCode.UNKNOWN_ID);
			response.setPerson(null);
			return new ResponseEntity(response,HttpStatus.BAD_REQUEST);
		}
	}

	@PutMapping("/{id}")
	@ApiOperation(value = "Update a single person by ID",
			notes="Will use the URI specified ID and ignore message body (if a different ID is present there). Linked userId cannot be changed.")
	public ResponseEntity<ResourceIdResponse<Long>> update(
			@PathVariable
			Long id,
			@RequestBody
			Person person,
			@ApiParam(value = "Bearer token for authentication", required = true)
			@RequestHeader("Authorization")
			String authKey){
		if(!AuthService.isTokenValid(authKey)){
			return AuthService.unauthorizedResponse(new ResourceIdResponse<Long>());
		}

		if(person != null){
			if(repository.existsById(id)){
				PersonEntity oldPerson = repository.findById(id).get();
				if(person.getUserId() != null && !person.getUserId().equals(oldPerson.getUser().getName())){
					ResourceIdResponse response = new ResourceIdResponse();
					response.setRespondeCode(RepsonseCode.UPDATE_FAILED);
					response.setMessage("Unexpected username. OneToOne user assignment cannot be changed. "+
										"Ideally, no userId should be passed.");
					return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
				}

				return savePerson(person, oldPerson);
			}else{
				Response response = new Response();
				response.setRespondeCode(RepsonseCode.UNKNOWN_ID);
				return new ResponseEntity(response,HttpStatus.BAD_REQUEST);
			}
		}else{
			ResourceIdResponse response = new ResourceIdResponse();
			response.setRespondeCode(RepsonseCode.UPDATE_FAILED);
			response.setMessage("Given person was null");
			return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
		}
	}

	private ResponseEntity<ResourceIdResponse<Long>> savePerson(Person person, PersonEntity oldPerson){
		PersonEntity personEntity = person.toEntity(oldPerson);

		PersonEntity personUpdated = repository.save(personEntity);

		ResourceIdResponse<Long> response = new ResourceIdResponse();
		response.setResourceId(personUpdated.getId());
		response.setRespondeCode(RepsonseCode.SAVE_SUCCESSFULL);
		response.setMessage("Saved person with id: "+personUpdated.getId());
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
