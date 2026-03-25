package com.sg.mongo.controller;

// ...existing imports...
import com.sg.mongo.dto.UserRequest;
import com.sg.mongo.model.User;
import com.sg.mongo.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<User> createUser(@Valid @RequestBody UserRequest request) {
        log.info("Received create request for user: {} {}", request.getFirstName(), request.getLastName());
        return userService.create(request);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<User> getAllUsers() {
        log.info("Received getAll users request");
        return userService.getAll();
    }

    @GetMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<User> getUserById(@PathVariable("id") String id) {
        log.info("Received get user by id request: {}", id);
        return userService.getById(id);
    }

    @PutMapping(value = "{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<User> updateUser(@PathVariable("id") String id, @Valid @RequestBody UserRequest request) {
        log.info("Received update request for user: {}", id);
        return userService.update(id, request);
    }

    @DeleteMapping(value = "{id}")
    public Mono<ResponseEntity<Void>> deleteUser(@PathVariable("id") String id) {
        log.info("Received delete request for user: {}", id);
        return userService.delete(id)
                .then(Mono.just(ResponseEntity.noContent().build()));
    }
}

