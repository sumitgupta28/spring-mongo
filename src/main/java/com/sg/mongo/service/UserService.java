package com.sg.mongo.service;

import com.sg.mongo.dto.UserRequest;
import com.sg.mongo.model.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserService {
    Mono<User> create(UserRequest request);
    Mono<User> getById(String id);
    Flux<User> getAll();
    Mono<User> update(String id, UserRequest request);
    Mono<Void> delete(String id);
}

