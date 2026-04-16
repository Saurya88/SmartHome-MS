# Step 4 Migration Change Log

Project: Smart Home Management Platform microservices  
Course: 420-N45-LA Web Services and Distributed Computing  
Step 4 option selected: Option 2, integrate the classes and interfaces from the Lab webservice into the microservices project.

## Initial Prompt

The project needed to be checked against the assignment requirements and completed as a Spring Boot microservices architecture. The main goal was to migrate the original smart home webservice functionality into three independent lower-level microservices plus an API Gateway, while preserving the Lab functionality and satisfying the Docker, Gradle, testing, exception handling, and REST requirements.

## Migration Strategy

The original domain was split using the updated DDD model. The resulting bounded contexts are:

- Device Registry: owns smart devices, device status, capabilities, network information, sensor data, maintenance records, and firmware version data.
- User Permissions: owns users, roles, access levels, permissions, account status, and email value objects.
- Automation Routine Management: owns scenes, scene actions, trigger rules, schedules, conditions, and scene activation behavior.

Each bounded context was implemented as an independent Spring Boot service with its own controller layer, service layer, persistence layer, database schema, and exception handling. The API Gateway was kept separate and is responsible for exposing the public REST API on port 8080, forwarding requests to the lower-level services, returning proper HTTP responses, and adding HATEOAS links.

## Microservice Split

### Device Registry Service

Module: `device-service`

Migrated functionality:

- Device aggregate and related domain objects.
- Device CRUD operations.
- Device status updates.
- Device capability data.
- Device repository persistence.
- Device-specific request and response DTOs.
- Device-specific exception handling.

Reason:

Device data changes independently from users and automation routines. It also has its own lifecycle, database tables, and business rules, so it fits as a supporting subdomain with its own microservice.

### User Permissions Service

Module: `user-permissions-service`

Migrated functionality:

- User aggregate and related role, permission, email, status, and access-level domain objects.
- User CRUD operations.
- User repository persistence.
- User-specific request and response DTOs.
- User-specific exception handling.

Reason:

User permission data is security and access related, and it should not share a database with device or automation data. Separating it lets the rest of the system reference users by identifier without owning user permission rules.

### Automation Service

Module: `automation-service`

Migrated functionality:

- Scene aggregate.
- Trigger rule aggregate.
- Device action value objects.
- Schedule and condition domain objects.
- Scene CRUD operations.
- Trigger rule CRUD operations.
- Scene activation behavior.
- Automation repository persistence.
- Automation-specific request and response DTOs.
- Automation-specific exception handling.

Reason:

Automation is the core domain because it coordinates behavior across the smart home. It references device identifiers but does not own device records. This keeps automation behavior independent from the Device Registry service while still allowing scenes and rules to target devices.

### API Gateway

Module: `api-gateway`

Migrated and added functionality:

- Public REST endpoints under `/api/v1`.
- Gateway controllers for devices, users, scenes, and trigger rules.
- HTTP client calls to lower-level services.
- Gateway DTOs.
- Global gateway exception handling.
- HATEOAS links on gateway responses.
- `ResponseEntity` return types for correct status codes.

Reason:

The lower-level services remain independently deployable and do not expose their ports publicly through Docker Compose. The API Gateway is the single public entry point and provides consistent REST behavior for clients.

## Manual Changes Made and Reasons

### Multi-Project Gradle Setup

Changed:

- Created/used a root Gradle project with these included modules:
  - `api-gateway`
  - `device-service`
  - `user-permissions-service`
  - `automation-service`

Reason:

The assignment requires a multi-project Gradle setup that can build and test all services from the root using `gradlew build` or `gradlew clean build test`.

### Docker Compose Setup

Changed:

- Added Docker Compose services for the API Gateway and all three lower-level microservices.
- Added one database container per lower-level microservice:
  - User Permissions uses MySQL.
  - Device Registry uses PostgreSQL.
  - Automation uses MySQL.
- Added service dependencies and database health checks.
- Configured only the API Gateway as the public HTTP entry point on port 8080.

Reason:

The assignment requires the Docker Compose setup to bring up the complete system and all databases as containers. Each lower-level microservice also needs its own database.

### Docker Profiles and Database Configuration

Changed:

- Added `application-docker.yml` files for the lower-level services.
- Configured Docker-specific JDBC URLs, database credentials, and Hibernate behavior.
- Added runtime database drivers:
  - MySQL driver for User Permissions.
  - PostgreSQL driver for Device Registry.
  - MySQL driver for Automation.

Reason:

The services need local/test configuration for Gradle tests and separate Docker configuration for container execution. The Docker profile prevents hard-coded local database URLs from breaking container startup.

### Domain Model Adjustments

Changed:

- Updated the DDD model to match the implemented bounded contexts.
- Kept firmware version as part of the Device aggregate instead of a separate firmware bounded context.
- Modeled Automation as the core domain and Device Registry/User Permissions as supporting domains.

Reason:

The current implementation has three lower-level microservices, not four. A separate firmware bounded context would not match the code or Docker architecture.

### REST Controllers and DTOs

Changed:

- Added or completed controller methods for create, read all, read by id, update, and delete operations.
- Used DTOs at service boundaries instead of exposing JPA entities directly.
- Returned appropriate HTTP codes such as 200, 201, 204, 404, and 409.

Reason:

The rubric checks the endpoints through the API Gateway and expects RESTful behavior, DTO responses, and correct HTTP status codes.

### API Gateway HATEOAS

Changed:

- Added Spring HATEOAS support to the API Gateway.
- Wrapped gateway responses in `EntityModel` or `CollectionModel`.
- Added self links and collection links for gateway resources.

Reason:

The API Gateway requirements state that the public API must be RESTful and implement HATEOAS. HATEOAS was placed in the gateway because it is the public API boundary.

### API Gateway Error Handling

Changed:

- Added gateway-level global exception handling.
- Handled downstream HTTP errors from lower-level services.
- Handled HTTP client connectivity errors.
- Returned structured error responses with appropriate HTTP status codes.

Reason:

The gateway must not leak raw downstream errors or return incorrect status codes when a lower-level service rejects a request or is unavailable.

### API Gateway Domain Client Layer

Changed:

- Moved gateway downstream HTTP client classes into `domainclientlayer` packages.
- Kept gateway controllers in `presentationlayer`.
- Kept lower-level service calls inside the gateway domain client layer instead of calling repositories or databases directly.

Reason:

The API Gateway requirements explicitly require a Domain Client Layer. The package naming now makes that layer visible in the source code and separates public REST controllers from downstream microservice clients.

### Subdomain-Specific Exceptions

Changed:

- Added `DuplicateDeviceNameException` in Device Registry.
- Added `DuplicateUsernameException` in User Permissions.
- Added `InvalidSceneActivationException` in Automation.
- Used these exceptions in service-layer business rules.
- Mapped them to proper HTTP error responses.

Reason:

The implementation requirements require each lower-level microservice to include at least one subdomain-specific exception and use it in service code.

### Repository Integration Tests

Changed:

- Added repository-based integration tests for all three lower-level microservices.
- Covered positive and negative persistence paths.

Reason:

The testing requirements require repository-based integration testing for all three lower-level microservices.

### Controller Integration Tests

Changed:

- Added controller-based integration tests for all three lower-level microservices using JUnit Jupiter and WebTestClient.
- Covered positive and negative controller paths.
- Added tests for the subdomain-specific exception paths.

Reason:

The testing requirements require controller integration tests using WebTestClient, including both positive and negative paths and exception-specific paths.

### Jacoco Coverage

Changed:

- Added Jacoco to the lower-level microservice Gradle builds.
- Added a 90 percent coverage verification rule.
- Added focused mapper and exception-handler tests where needed.
- Excluded application bootstrap classes, domain model classes, and DTO classes from coverage verification.

Reason:

The assignment requires all three lower-level microservices to have Jacoco code coverage of 90 percent or more. The exclusions keep the coverage gate focused on executable application logic instead of simple data structures.

### C4 and DDD Documentation

Changed:

- Added a C4 Level 1 system context diagram.
- Added a C4 Level 2 container diagram.
- Updated the DDD domain model diagram.

Reason:

The design requirements require C4-PlantUML diagrams for Level 1 and Level 2, plus an updated DDD model using the DDD-PlantUML style.

## Verification Completed

The following checks were run successfully after the migration work:

- `./gradlew clean build test`
- `docker compose build`
- `docker compose up -d`
- API Gateway health check through `http://localhost:8080/actuator/health`
- Gateway endpoint checks through `http://localhost:8080/api/v1`
- Jacoco verification for all three lower-level services

Test result summary from the root Gradle build:

- Total tests: 62
- Passed: 62
- Failed: 0
- Errors: 0
- Skipped: 0

## Known Remaining Notes

- The databases are created by Docker Compose and deterministic Docker seed data is loaded through the `docker` Spring profile.
- The Docker Compose file includes phpMyAdmin on port `8088` and pgAdmin on port `5050` for database verification.
- The implemented rubric endpoint checks use GET, POST, PUT, and DELETE. PATCH endpoints are not implemented because the rubric endpoint table does not require PATCH.
