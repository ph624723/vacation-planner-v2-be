package com.ph.rest.webservices.restfulwebservices.controller;

import com.ph.model.EmailFailedException;
import com.ph.model.PersonNotFoundException;
import com.ph.model.UserNameInUseException;
import com.ph.model.UserNotFoundException;
import com.ph.persistence.model.PersonEntity;
import com.ph.persistence.model.UserEntity;
import com.ph.persistence.repository.PersonJpaRepository;
import com.ph.persistence.repository.UserJpaRepository;
import com.ph.rest.webservices.restfulwebservices.Service.UserService;
import com.ph.rest.webservices.restfulwebservices.model.*;
import com.ph.service.AuthService;
import com.ph.service.EmailServiceImpl;
import com.ph.service.HashService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class UserController implements IRootController<User,String>{
	
	@Autowired
	private UserJpaRepository repository;

	@Autowired
	private UserService userService;

	private final String rootUserName = "root";
	private final String rootUserErrorText = "Root privileges required to access user data.";

	@ApiOperation(value = "Gets all stored users", notes="root access only")
	public ResponseEntity<UserListResponse> getAll(
			String username,
			String password
	){
		if(!authenticateRootUser(username,password)){
			UserListResponse response = new UserListResponse();
			response.setRespondeCode(RepsonseCode.CREDENTIALS_DENIED);
			response.setMessage(rootUserErrorText);
			response.setList(null);
			return new ResponseEntity<>(response,HttpStatus.UNAUTHORIZED);
		}

		List<User> results = repository.findAll().stream().map(x -> User.fromEntity(x)).collect(Collectors.toList());
		UserListResponse response = new UserListResponse();
		response.setRespondeCode(RepsonseCode.OK);
		response.setList(results);
		return new ResponseEntity<>(response,HttpStatus.OK);
	}

	@ApiOperation(value = "Get a single user by name")
	public ResponseEntity<UserResponse> get(String id,
											String username,
											String password){
		if(!(authenticateRootUser(username,password) || authenticateOwnUser(username,password,id))){
			UserResponse response = new UserResponse();
			response.setRespondeCode(RepsonseCode.CREDENTIALS_DENIED);
			response.setMessage("Only the owned user can be edited without root privileges");
			response.setUser(null);
			return new ResponseEntity<>(response,HttpStatus.UNAUTHORIZED);
		}

		if(repository.existsById(id)){
			User user = User.fromEntity(repository.findById(id).orElse(null));
			if(!authenticateRootUser(username,password)){
				user.setPassword("");
			}

			UserResponse response = new UserResponse();
			response.setRespondeCode(RepsonseCode.OK);
			response.setUser(user);
			return new ResponseEntity(response,HttpStatus.OK);
		}else{
			UserResponse response = new UserResponse();
			response.setRespondeCode(RepsonseCode.UNKNOWN_ID);
			response.setUser(null);
			return new ResponseEntity(response,HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Delete a single user by ID", notes="root access only")
	public ResponseEntity<Response> delete(String id,
										   String username,
										   String password) {
		if(!authenticateRootUser(username,password)){
			Response response = new Response();
			response.setRespondeCode(RepsonseCode.CREDENTIALS_DENIED);
			response.setMessage(rootUserErrorText);
			return new ResponseEntity<>(response,HttpStatus.UNAUTHORIZED);
		}

		if(repository.existsById(id)){
			repository.deleteById(id);

			Response response = new Response();
			response.setRespondeCode(RepsonseCode.DELETE_SUCCESSFULL);
			response.setMessage("Deleted user with ID: "+id);
			return new ResponseEntity(response,HttpStatus.OK);
		}else{
			Response response = new Response();
			response.setRespondeCode(RepsonseCode.UNKNOWN_ID);
			return new ResponseEntity(response,HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Update a single user by ID",
			notes="Will use the URI specified ID and ignore message body (if a different ID is present there). The same is true for person information")
	public ResponseEntity<ResourceIdResponse<String>> update(String id,
										   User user,
										   String username,
										   String password){
		if(!(authenticateRootUser(username,password) || authenticateOwnUser(username,password,id))){
			ResourceIdResponse response = new ResourceIdResponse();
			response.setRespondeCode(RepsonseCode.CREDENTIALS_DENIED);
			if (!(username.equals(id) || username.equals(rootUserName))) response.setMessage("Only the owned user can be edited without root privileges");
			return new ResponseEntity<>(response,HttpStatus.UNAUTHORIZED);
		}

		if(user != null){
			if(repository.existsById(id)){
				UserEntity oldUser = repository.findById(id).get();
				user.setName(id);
				if(user.getPerson() == null){
					ResourceIdResponse response = new ResourceIdResponse();
					response.setRespondeCode(RepsonseCode.UPDATE_FAILED);
					response.setMessage("Given person was null");
					return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
				}
				return new ResponseEntity<>(userService.saveUser(user, oldUser), HttpStatus.OK);
			}else{
				Response response = new Response();
				response.setRespondeCode(RepsonseCode.UNKNOWN_ID);
				return new ResponseEntity(response,HttpStatus.BAD_REQUEST);
			}
		}else{
			ResourceIdResponse response = new ResourceIdResponse();
			response.setRespondeCode(RepsonseCode.UPDATE_FAILED);
			response.setMessage("Given user was null");
			return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Create a new user+person combination", notes="root access only")
	public ResponseEntity<ResourceIdResponse<String>> create(User user,
										   String username,
										   String password){
		if(!authenticateRootUser(username,password)){
			ResourceIdResponse response = new ResourceIdResponse();
			response.setRespondeCode(RepsonseCode.CREDENTIALS_DENIED);
			response.setMessage(rootUserErrorText);
			return new ResponseEntity<>(response,HttpStatus.UNAUTHORIZED);
		}

		if(user.getPerson() == null){
			ResourceIdResponse response = new ResourceIdResponse();
			response.setRespondeCode(RepsonseCode.SAVE_FAILED);
			response.setMessage("Given person was null");
			return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
		}
				
		return new ResponseEntity<>(userService.saveUser(user,null), HttpStatus.OK);
	}

	@ApiOperation(value = "Create a new user+person combination", notes="token access")
	@PostMapping("/register")
	public ResponseEntity<ResourceIdResponse<String>> register(
			@RequestBody
			@ApiParam(value = "Credentials to create user from")
			RegisterCredentials credentials,
			@RequestHeader("Authorization")
			@ApiParam(value = "Bearer token for authentication", required = true)
			String authKey){
		if(!AuthService.isTokenValid(authKey)){
			ResourceIdResponse response = new ResourceIdResponse();
			response.setMessage("Authorization key is invalid");
			response.setRespondeCode(RepsonseCode.TOKEN_DENIED);
			return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
		}
		if(credentials.getUsername().length() < 4){
			ResourceIdResponse response = new ResourceIdResponse();
			response.setMessage("Username has to have at least 4 characters");
			response.setRespondeCode(RepsonseCode.REGISTER_DENIED);
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}

		try {
			return new ResponseEntity<>(userService.registerUser(credentials), HttpStatus.CREATED);
		}catch (EmailFailedException e){
			ResourceIdResponse response = new ResourceIdResponse();
			response.setMessage("Confirmation e-mail could not be sent. Please check contact field.");
			response.setRespondeCode(RepsonseCode.SAVE_FAILED);
			return new ResponseEntity<>(response, HttpStatus.EXPECTATION_FAILED);
		}catch (UserNameInUseException e){
			ResourceIdResponse response = new ResourceIdResponse();
			response.setMessage("Username already in use");
			response.setRespondeCode(RepsonseCode.REGISTER_DENIED);
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
	}

	private boolean authenticateRootUser(String username, String password){
		Optional<UserEntity> rootUser = repository.findById(rootUserName);
		if(rootUser.isPresent()){
			return username.equals(rootUserName) &&
					rootUser.get().getPassword().equals(HashService.MD5(password));
		}else return true;
	}

	private boolean authenticateOwnUser(String username, String password, String id){
		Optional<UserEntity> user = repository.findById(username);

			return user.isPresent() &&
					username.equals(id) &&
					user.get().getPassword().equals(HashService.MD5(password));
	}
}
