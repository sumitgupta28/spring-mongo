package com.sg.mongo.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sg.mongo.repository.UserRepository;
import com.sg.mongo.test.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerIntegrationTest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void cleanup() {
        // clear collection before each test
        userRepository.deleteAll().block();
    }

    private WebTestClient webTestClient() {
        return WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    void createUser_withoutAddress_shouldPersistWithNullAddress() throws Exception {
        Map<String, Object> payload = Map.of(
                "firstName", "John",
                "lastName", "Doe",
                "email", "john.doe@example.com"
        );

        String body = webTestClient().post()
                .uri("/api/users")
                .bodyValue(payload)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        System.out.println("CREATE WITHOUT ADDRESS RESPONSE: " + body);

        assertThat(body).isNotNull();
        JsonNode node = objectMapper.readTree(body);
        assertThat(node.path("id").isMissingNode()).isFalse();
        assertThat(node.path("firstName").asText()).isEqualTo("John");
        // path(...) returns a MissingNode or NullNode; .isNull() covers both null and absent cases
        assertThat(node.path("address").isNull()).isTrue();
    }

    @Test
    void createUser_withAddress_shouldPersistAddress() throws Exception {
        Map<String, Object> address = Map.of(
                "street", "123 Main St",
                "city", "Anytown",
                "state", "CA",
                "zip", "12345",
                "country", "USA"
        );

        Map<String, Object> payload = Map.of(
                "firstName", "Alice",
                "lastName", "Smith",
                "email", "alice.smith@example.com",
                "address", address
        );

        String body = webTestClient().post()
                .uri("/api/users")
                .bodyValue(payload)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        System.out.println("CREATE WITH ADDRESS RESPONSE: " + body);

        assertThat(body).isNotNull();
        JsonNode node = objectMapper.readTree(body);
        assertThat(node.path("id").isMissingNode()).isFalse();
        JsonNode a = node.path("address");
        assertThat(a.isNull()).isFalse();
        assertThat(a.path("street").asText()).isEqualTo("123 Main St");
        assertThat(a.path("city").asText()).isEqualTo("Anytown");
        assertThat(a.path("zip").asText()).isEqualTo("12345");
    }

    @Test
    void updateUser_addAddress_thenRemoveAddress_shouldBehaveAsExpected() throws Exception {
        // create without address
        Map<String, Object> createPayload = Map.of(
                "firstName", "Bob",
                "lastName", "Builder",
                "email", "bob.builder@example.com"
        );

        String createBody = webTestClient().post()
                .uri("/api/users")
                .bodyValue(createPayload)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        System.out.println("CREATE RESPONSE: " + createBody);

        JsonNode createdNode = objectMapper.readTree(createBody);
        assertThat(createdNode.path("id").isMissingNode()).isFalse();
        String id = createdNode.path("id").asText();

        // update to add address
        Map<String, Object> address = Map.of(
                "street", "10 Market St",
                "city", "OtherCity",
                "state", "NY",
                "zip", "67890",
                "country", "USA"
        );
        Map<String, Object> updatePayload = Map.of(
                "firstName", "Bob",
                "lastName", "Builder",
                "email", "bob.builder@example.com",
                "address", address
        );

        String updatedBody = webTestClient().put()
                .uri(uriBuilder -> uriBuilder.path("/api/users/{id}").build(id))
                .bodyValue(updatePayload)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        System.out.println("UPDATED RESPONSE: " + updatedBody);
        assertThat(updatedBody).isNotNull();

        JsonNode updatedNode = objectMapper.readTree(updatedBody);
        assertThat(updatedNode.path("address").isNull()).isFalse();
        assertThat(updatedNode.path("address").path("city").asText()).isEqualTo("OtherCity");

        // update to remove address (send address = null)
        Map<String, Object> removeAddressPayload = new HashMap<>();
        removeAddressPayload.put("firstName", "Bob");
        removeAddressPayload.put("lastName", "Builder");
        removeAddressPayload.put("email", "bob.builder@example.com");
        removeAddressPayload.put("address", null);

        String afterRemoveBody = webTestClient().put()
                .uri(uriBuilder -> uriBuilder.path("/api/users/{id}").build(id))
                .bodyValue(removeAddressPayload)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        System.out.println("AFTER REMOVE RESPONSE: " + afterRemoveBody);
        assertThat(afterRemoveBody).isNotNull();

        JsonNode afterRemoveNode = objectMapper.readTree(afterRemoveBody);
        assertThat(afterRemoveNode.path("address").isNull()).isTrue();
    }
}
