package com.ph.rest.webservices.restfulwebservices.controller;

import com.ph.rest.webservices.restfulwebservices.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
public interface IController<T, IDtype> {
    @GetMapping("/")
    public List<T> getAll();

    @GetMapping("/{id}")
    public T get(@PathVariable IDtype id);

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable IDtype id);


    @PutMapping("/{id}")
    public ResponseEntity<T> update(
            @PathVariable IDtype id, @RequestBody T element);

    @PostMapping("/")
    public ResponseEntity<Void> create(@RequestBody T element);
}
