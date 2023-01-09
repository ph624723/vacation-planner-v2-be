package com.ph.rest.webservices.restfulwebservices.controller;

import com.ph.persistence.model.UserEntity;
import com.ph.persistence.repository.UserJpaRepository;
import com.ph.rest.webservices.restfulwebservices.model.*;
import com.ph.service.AuthService;
import com.ph.service.HashService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Optional;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/auth")
public class AuthCotroller {

    @Autowired
    UserJpaRepository userRepository;

    @PostMapping("/token")
    @ApiOperation(value = "Retrieves a new token", notes = "Retrieves a new token if the passed user credentials are valid")
    public ResponseEntity<AuthTokenResponse> get(
            @RequestHeader("username")
            String username,
            @RequestHeader("password")
            String password){
        Optional<UserEntity> user = userRepository.findById(username);
        if(user.isPresent() &&
           user.get().getPassword().equals(HashService.MD5(password))){
            AuthToken token;
            try {
                token = AuthService.generateToken(password);
            }catch (ToManySessionsException e){
                AuthTokenResponse response = new AuthTokenResponse();
                response.setMessage(e.getMessage());
                response.setRespondeCode(RepsonseCode.MAX_CONCURRENT_SESSIONS_EXEEDED);
                return new ResponseEntity<>(response, HttpStatus.INSUFFICIENT_STORAGE);
            }
            AuthTokenResponse response = new AuthTokenResponse();
            response.setAuthToken(token);
            response.setRespondeCode(RepsonseCode.CREDENTIALS_ACCEPTED);
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        }else{
            AuthTokenResponse response = new AuthTokenResponse();
            response.setAuthToken(null);
            response.setRespondeCode(RepsonseCode.CREDENTIALS_DENIED);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/check")
    @ApiOperation(value = "Checks token for validity")
    public ResponseEntity<Response> authenticate(
            @ApiParam(value = "Authentication key")
            @RequestHeader("Authorization")
            String authKey
    ){
        System.out.println(authKey);
        Response response = new Response();
        if(AuthService.isTokenValid(authKey)) {
            response.setMessage("Key is valid");
            response.setRespondeCode(RepsonseCode.CREDENTIALS_ACCEPTED);
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        }else{
            response.setMessage("Key is invalid");
            response.setRespondeCode(RepsonseCode.TOKEN_DENIED);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }
}
