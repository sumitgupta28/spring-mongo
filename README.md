# spring-mongo

A small Spring Boot (Reactive) sample that uses MongoDB. This repository contains:

- A reactive CRUD API for `User` (controller, service, repository).
- `application.yml` containing MongoDB configuration.
- `docker-compose.yml` to run MongoDB and Mongo Express locally.

---

## Prerequisites

- Java 21 (project toolchain set in `build.gradle`)
- Gradle (the included Gradle wrapper `./gradlew` is recommended)
- Docker & Docker Compose (for running MongoDB + Mongo Express)

---

## Quick start (recommended)

1. Start MongoDB and Mongo Express with Docker Compose (from project root):

```bash
# Docker Compose v2+ (recommended)
docker compose up -d

# Or with the older docker-compose command
# docker-compose up -d
```

This brings up:
- MongoDB on host port `27017`
- Mongo Express on host port `8081` (web UI)

2. Start the Spring Boot app (connects to local Mongo by default):

```bash
./gradlew bootRun
```

If you want the app to connect to the `mongo` container when running inside the same Compose network, run:

```bash
SPRING_DATA_MONGODB_URI=mongodb://mongo:27017/springdb ./gradlew bootRun
```

3. Alternatively build and run the fat JAR:

```bash
./gradlew bootJar
java -jar build/libs/*.jar
```

You can pass the Mongo URI as an environment variable if needed:

```bash
SPRING_DATA_MONGODB_URI=mongodb://mongo:27017/springdb java -jar build/libs/*.jar
```

---

## API Endpoints

Base path: `/api/users`

- Create user: POST `/api/users`
  - Content-Type: application/json
  - Body example:

```json
{
  "firstName": "Alice",
  "lastName": "Doe",
  "email": "alice@example.com",
  "address": {
    "street": "123 Main St",
    "city": "Anytown",
    "state": "CA",
    "zip": "12345",
    "country": "USA"
  }
}
```

- Get all users: GET `/api/users`
- Get user by id: GET `/api/users/{id}`
- Update user: PUT `/api/users/{id}` (JSON body same as create)
- Delete user: DELETE `/api/users/{id}`

Example curl calls:

```bash
# Create
curl -s -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Alice","lastName":"Doe","email":"alice@example.com","address":{"street":"123 Main St","city":"Anytown","state":"CA","zip":"12345","country":"USA"}}' | jq

# List
curl -s http://localhost:8080/api/users | jq

# Get by id
curl -s http://localhost:8080/api/users/<id> | jq

# Update
curl -s -X PUT http://localhost:8080/api/users/<id> -H "Content-Type: application/json" -d '{"firstName":"Alice","lastName":"Smith","email":"alice.smith@example.com","address":{"street":"10 Market St","city":"OtherCity","state":"NY","zip":"67890","country":"USA"}}' | jq

# Delete
curl -s -X DELETE http://localhost:8080/api/users/<id> -I
```

Replace `<id>` with a real document id returned by the create or list calls.

---

## OpenAPI / Swagger

This project exposes OpenAPI documentation and a Swagger UI via Springdoc.

Default endpoints (when the app runs on port 8080):

- OpenAPI JSON (raw spec): `http://localhost:8080/v3/api-docs`
- Swagger UI (interactive): `http://localhost:8080/swagger-ui.html`
  - The UI may also be available at `/swagger-ui/index.html` depending on springdoc version and mapping.

Quick curl example to fetch API spec:

```bash
curl -s http://localhost:8080/v3/api-docs | jq
```

Notes and tips:
- The generated documentation includes the `UserController` endpoints and request/response schemas inferred from your DTOs. The `UserRequest` now contains a nested `address` (type `AddressRequest`) which maps to the `Address` embedded object in the `User` document.
- You can customize metadata (title, description, contact) in `src/main/java/com/sg/mongo/config/OpenApiConfig.java`.
- To change the docs/UI paths, update `springdoc.api-docs.path` and `springdoc.swagger-ui.path` in `src/main/resources/application.yml`.
- For security, consider disabling the Swagger UI in production or restricting access with Spring Security. Example (in `application.yml`):

```yaml
springdoc:
  swagger-ui:
    enabled: false
```

- If the app is running inside Docker Compose and not on the host, replace `localhost` in the above URLs with the container hostname or use port forwarding.

---

## Configuration

Primary configuration file: `src/main/resources/application.yml`.

By default it contains (connects to local Mongo with the default Compose credentials):

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://root:example@localhost:27017/springdb?authSource=admin
      database: springdb
server:
  port: 8080
```

When running inside Docker Compose, update the URI to use the service name `mongo` (the Compose file already includes a commented example):

```bash
SPRING_DATA_MONGODB_URI=mongodb://root:example@mongo:27017/springdb?authSource=admin
```

You can override any config with environment variables (e.g. `SPRING_DATA_MONGODB_URI`) or JVM args (`--spring.data.mongodb.uri=...`).

---

## Troubleshooting

Common issues and how to resolve them.

- Mongo Connection Refused / Cannot connect
  - Ensure Docker is running and `docker compose up -d` succeeded.
  - If running the app locally (not in Compose), ensure Mongo is accessible at `localhost:27017`.
  - If using Compose, set `SPRING_DATA_MONGODB_URI=mongodb://root:example@mongo:27017/springdb?authSource=admin` so the app resolves the `mongo` container name.
  - If your Mongo requires authentication, use a URI with correct credentials and `authSource` where appropriate.

- Port conflicts (8080 or 8081 in use)
  - If `8080` is already used, either stop the process using that port or change `server.port` in `application.yml` or pass `--server.port=XXXX`.
  - If `8081` is used, Mongo Express won't start on that host port; change it in `docker-compose.yml` or free the port.

- Lombok issues in IDE (generates compile errors in the editor)
  - Ensure your IDE has the Lombok plugin enabled and annotation processing is turned on. The project compiles with Gradle even if the IDE shows warnings.

- Validation errors (400 responses)
  - The controller validates payloads (firstName, lastName, email). Make sure JSON fields are present and valid; inspect response body for validation messages.
  - For the nested `address` object, required fields are: `street`, `city`, `zip`, and `country` — ensure they're included when creating/updating users.

- Unexpected 404 when fetching by id
  - Ensure you used the correct document id returned during creation (Mongo ObjectId strings). Check Mongo Express to inspect the `users` collection.

- Mongo Express shows empty DB/collections
  - Confirm you're looking at the same database (the Compose file uses `springdb`).
  - If running a separate Mongo instance, ensure the app writes to the database you're inspecting.

- Slow or blocking calls
  - This project uses Spring WebFlux (reactive). Avoid calling blocking code on reactive threads. If you introduce blocking libraries, wrap them in the appropriate scheduler.

---

## Next steps / suggestions

- Add the Spring Boot app as a service to `docker-compose.yml` for full "one command" startup.
- Add integration tests using the reactive test slices included in the `build.gradle`.
- Add a `application-docker.yml` profile for dockerized defaults and mount configuration with environment variables.

---

If you want, I can:
- Add the app to `docker-compose.yml` and wire the environment variables so `docker compose up -d` starts the full stack (app + mongo + mongo-express).
- Add an example Postman collection or more automated integration tests.
