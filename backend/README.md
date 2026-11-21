# SkyBook Backend Foundation

This directory contains the initial Spring Boot backend setup for SkyBook.

## Prerequisites

- Java 21+ installed and available on PATH
- Internet access for first Maven wrapper dependency download

## Run with Maven Wrapper (No Global Maven Install)

Use the local wrapper alias command:

```powershell
.\.mvn.cmd spring-boot:run
```

Equivalent direct wrapper command:

```powershell
.\mvnw.cmd spring-boot:run
```

## Build and Test

```powershell
.\.mvn.cmd clean test
.\.mvn.cmd clean compile
```

## Profiles and Configuration

- Default profile: `local` (starts without requiring a live PostgreSQL connection)
- Development profile: `dev` (uses PostgreSQL settings from environment variables)

Required environment variables for `dev` profile:

- `SKYBOOK_DB_URL` (example: `jdbc:postgresql://localhost:5432/skybook`)
- `SKYBOOK_DB_USERNAME`
- `SKYBOOK_DB_PASSWORD`

Run using `dev` profile:

```powershell
.\.mvn.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```

## Scope of This Milestone

- Spring Boot application bootstrap
- Maven dependency baseline
- Initial package structure only
- Environment-based configuration
- Context-load test

No entities, repositories, controllers, or business APIs are implemented in this milestone.
