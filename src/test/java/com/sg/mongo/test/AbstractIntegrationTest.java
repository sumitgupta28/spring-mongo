package com.sg.mongo.test;

import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Reusable base class that starts Testcontainers once for the entire test suite.
 * Other integration tests should extend this class to inherit the running MongoDB container
 * and the dynamic property registration for Spring Data MongoDB.
 */
@Testcontainers
public abstract class AbstractIntegrationTest {

    @Container
    static final MongoDBContainer mongoContainer = new MongoDBContainer("mongo:6.0");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoContainer::getReplicaSetUrl);
    }
}

