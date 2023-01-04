package com.ph.rest.webservices.restfulwebservices.controller;

import com.ph.rest.webservices.restfulwebservices.model.ListResponse;
import com.ph.rest.webservices.restfulwebservices.model.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public interface IController<T, IDtype> {
    @GetMapping("/")
    public <R extends ListResponse<T>> ResponseEntity<R> getAll(
            @RequestHeader("Authorization")
            String authKey
    );

    @GetMapping("/{id}")
    public <R extends Response<T>> ResponseEntity<R> get(
            @PathVariable
            IDtype id,
            @RequestHeader("Authorization")
            String authKey);

    @DeleteMapping("/{id}")
    public ResponseEntity<Response> delete(
            @PathVariable
            IDtype id,
            @RequestHeader("Authorization")
            String authKey);


    @PutMapping("/{id}")
    public ResponseEntity<Response> update(
            @PathVariable
            IDtype id,
            @RequestBody
            T element,
            @RequestHeader("Authorization")
            String authKey);

    @PostMapping("/")
    public ResponseEntity<Response> create(
            @RequestBody
            T element,
            @RequestHeader("Authorization")
            String authKey);
}
