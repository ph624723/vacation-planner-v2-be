package com.ph.rest.webservices.restfulwebservices.controller;

import com.ph.rest.webservices.restfulwebservices.model.ListResponse;
import com.ph.rest.webservices.restfulwebservices.model.ResourceIdResponse;
import com.ph.rest.webservices.restfulwebservices.model.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public interface IRootController<T, IDtype> {
    @GetMapping("/")
    public <R extends ListResponse<T>> ResponseEntity<R> getAll(
            @RequestHeader("username") String username,
            @RequestHeader("password") String password
    );

    @GetMapping("/{id}")
    public <R extends Response<T>> ResponseEntity<R> get(
            @PathVariable IDtype id,
            @RequestHeader("username") String username,
            @RequestHeader("password") String password
    );

    @DeleteMapping("/{id}")
    public ResponseEntity<Response> delete(
            @PathVariable IDtype id,
            @RequestHeader("username") String username,
            @RequestHeader("password") String password
    );


    @PutMapping("/{id}")
    public ResponseEntity<ResourceIdResponse<IDtype>> update(
            @PathVariable IDtype id,
            @RequestBody T element,
            @RequestHeader("username") String username,
            @RequestHeader("password") String password
    );

    @PostMapping("/")
    public ResponseEntity<ResourceIdResponse<IDtype>> create(
            @RequestBody T element,
            @RequestHeader("username") String username,
            @RequestHeader("password") String password
    );
}
