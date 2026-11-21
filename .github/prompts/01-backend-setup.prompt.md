# SkyBook — Backend Project Setup

## Objective

Initialize the SkyBook backend as a clean, maintainable Spring Boot Maven application, following the approved project documentation.

## Context to Review

Before making changes, read:

- `AGENTS.md`
- Approved Project Proposal
- Approved SRS
- UML Class Diagram
- ER Diagram
- Database Design documentation

Also inspect the existing repository structure and identify whether the `backend/` directory already contains any files.

Treat the approved documentation as the source of truth. Do not invent new requirements.

## Technology Requirements

- Language: Java
- Framework: Spring Boot
- Build tool: Maven
- Database: PostgreSQL
- Persistence: Spring Data JPA / Hibernate
- Validation: Jakarta Bean Validation
- Testing: JUnit 5 and Spring Boot Test

Select compatible Java and Spring Boot versions based on the installed JDK and project requirements. Document the selected versions.

## Implementation Scope

### 1. Initialize the Spring Boot application

Create the backend project inside the existing `backend/` directory.

Use a clear package structure:

`com.skybook`

Create the main application class:

`SkyBookApplication.java`

### 2. Configure Maven dependencies

Add only the dependencies needed for the initial backend foundation:

- Spring Boot Starter Web
- Spring Boot Starter Data JPA
- Spring Boot Starter Validation
- PostgreSQL JDBC Driver
- Spring Boot Starter Test

Do not add Spring Security, Spring AI, MCP, Swagger, Lombok, Redis or other optional dependencies at this stage. They will be introduced in their respective implementation milestones.

### 3. Establish the initial package structure

Create a minimal modular-monolith package structure:

- `config`
- `common`
  - `exception`
  - `response`
- `auth`
- `airport`
- `flight`
- `booking`
- `admin`
- `ai`

Do not create entities, repositories, controllers or business logic yet. Those belong to later milestones.

### 4. Application configuration

Create appropriate application configuration files.

Requirements:

- Support environment-based configuration.
- Prepare PostgreSQL connection properties using environment variables.
- Do not hardcode credentials.
- Use a development profile where appropriate.
- Keep Hibernate DDL behavior safe; do not automatically drop or recreate database tables.
- Keep SQL logging and debugging settings suitable for local development.

Do not assume that the database already exists or that its connection has been verified.

### 5. Git and project hygiene

- Inspect existing `.gitignore` files before modifying them.
- Ensure Maven build output such as `target/` is ignored.
- Ensure local environment files containing secrets are ignored.
- Do not ignore project instructions, prompt files or documentation.
- Add a concise `backend/README.md` explaining prerequisites, configuration and how to run the application.

### 6. Basic application verification

- Add a minimal application context test.
- Verify Maven compilation and tests.
- Verify that the application can start without requiring a live PostgreSQL database, if the initial configuration permits it.
- Do not add business APIs or sample domain data.

## Engineering Constraints

1. Follow `AGENTS.md` and approved project documentation.
2. Do not modify the frontend or database documentation.
3. Do not implement database schema, entities or business features.
4. Do not introduce microservices or unnecessary abstractions.
5. Do not duplicate existing configuration or files.
6. Keep the implementation understandable for a junior Java developer.
7. Explain important technical decisions.
8. Report actual build and test outcomes. Never invent successful results.

## Acceptance Criteria

- [ ] Spring Boot Maven project exists under `backend/`.
- [ ] Main application class is created.
- [ ] Required initial dependencies are configured.
- [ ] Package structure is established.
- [ ] Environment-based configuration is prepared.
- [ ] No credentials are committed.
- [ ] Backend README is available.
- [ ] Maven compilation succeeds.
- [ ] Automated tests execute successfully.
- [ ] No database entities, business APIs or AI features are implemented in this milestone.

## Expected Completion Report

After implementation, report:

1. Files created or modified.
2. Java, Spring Boot and Maven versions.
3. Dependencies added and their purposes.
4. Project structure.
5. Environment variables required.
6. Commands executed.
7. Actual compilation and test results.
8. Any unresolved issues or assumptions.

Stop after completing this milestone. Wait for my review and approval before implementing the database.