package com.sg.mongo.service.impl;

import com.sg.mongo.dto.UserRequest;
import com.sg.mongo.dto.AddressRequest;
import com.sg.mongo.model.User;
import com.sg.mongo.model.Address;
import com.sg.mongo.repository.UserRepository;
import com.sg.mongo.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public Mono<User> create(UserRequest request) {
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .address(toAddress(request.getAddress()))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        log.info("Creating user: {} {}", request.getFirstName(), request.getLastName());
        return userRepository.save(user);
    }

    @Override
    public Mono<User> getById(String id) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")));
    }

    @Override
    public Flux<User> getAll() {
        return userRepository.findAll();
    }

    @Override
    public Mono<User> update(String id, UserRequest request) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")))
                .flatMap(existing -> {
                    existing.setFirstName(request.getFirstName());
                    existing.setLastName(request.getLastName());
                    existing.setEmail(request.getEmail());
                    existing.setAddress(toAddress(request.getAddress()));
                    existing.setUpdatedAt(Instant.now());
                    log.info("Updating user {}", id);
                    return userRepository.save(existing);
                });
    }

    @Override
    public Mono<Void> delete(String id) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")))
                .flatMap(existing -> {
                    log.info("Deleting user {}", id);
                    return userRepository.delete(existing);
                });
    }

    private Address toAddress(AddressRequest a) {
        if (a == null) return null;
        return Address.builder()
                .street(a.getStreet())
                .city(a.getCity())
                .state(a.getState())
                .zip(a.getZip())
                .country(a.getCountry())
                .build();
    }
}
