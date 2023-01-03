package com.ph.rest.webservices.restfulwebservices.controller;

import com.ph.rest.webservices.restfulwebservices.model.User;
import com.ph.rest.webservices.restfulwebservices.repository.UserJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController implements IController<User,String>{
	
	@Autowired
	private UserJpaRepository repository;

	public List<User> getAll(){
		return repository.findAll();
	}

	public User get(String id){
		return repository.findById(id).orElse(null);
	}

	public ResponseEntity<Void> delete(String id) {

		repository.deleteById(id);

		return ResponseEntity.noContent().build();
	}

	public ResponseEntity<User> update(String id, User user){
		
		User userUpdated = repository.save(user);
		
		return new ResponseEntity<User>(userUpdated, HttpStatus.OK);
	}

	public ResponseEntity<Void> create(User user){
				
		User createdUser = repository.save(user);
		
		//Location
		//Get current resource url
		///{id}
		URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}").buildAndExpand(createdUser.getName()).toUri();
		
		return ResponseEntity.created(uri).build();
	}
}
