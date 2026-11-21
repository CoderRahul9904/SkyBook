# SkyBook — AI-Assisted Airline Booking Platform

## 1. Project Context

You are working as a senior software engineer on **SkyBook**, an AI-assisted airline ticket booking platform developed as part of Java and AWS training.

The project must demonstrate sound software engineering practices, effective use of GitHub Copilot, modular application design, REST API development, database integration, frontend development and Spring AI capabilities.

The development timeline is approximately four days. Prioritize a working, demonstrable application over unnecessary complexity.

The application includes an AI travel assistant named **AeroMate**.

## 2. Project Objectives

Build a functional airline booking application with the following five modules:

1. Flight Search
2. Flight Selection
3. Passenger and Booking Management
4. Booking Management
5. Admin Management

Integrate the following AI capabilities using Spring AI:

- Retrieval-Augmented Generation (RAG)
- Tool Calling
- Chat Memory
- Model Context Protocol (MCP)

## 3. Technology Stack

### Backend
- Java
- Spring Boot
- Spring Web
- Spring Data JPA
- Hibernate
- Bean Validation
- Maven
- Spring AI

### Frontend
- React
- TypeScript
- Tailwind CSS
- Vite

### Database
- PostgreSQL
- pgvector, if feasible within the project timeline

### AI Integration
- Spring AI ChatClient
- Capgemini AI Gateway
- Gemini model through the OpenAI-compatible API
- Spring AI Advisors and Chat Memory
- Spring AI RAG components
- Spring AI Tool Calling
- MCP integration

### Development Tools
- Visual Studio Code
- GitHub Copilot Agent Mode
- Git and GitHub
- Postman or an equivalent API testing tool
- Swagger/OpenAPI

Use the versions already configured in the project. Do not upgrade dependencies without a clear technical reason.

## 4. Architecture Guidelines

- Use a **modular monolithic architecture**.
- Maintain clear separation between controllers, services, repositories, entities and DTOs.
- Keep business logic inside service classes.
- Controllers should handle HTTP requests and responses, not business rules.
- Use DTOs for API communication. Do not expose JPA entities directly.
- Keep AI-related components separate from core booking business logic.
- Maintain a clear separation between frontend and backend.
- Avoid unnecessary abstractions and premature optimization.

Do not introduce microservices unless explicitly approved.

## 5. Backend Engineering Rules

- Follow standard Spring Boot conventions.
- Use constructor injection.
- Use meaningful class, method and variable names.
- Use appropriate HTTP methods and status codes.
- Validate incoming requests using Bean Validation.
- Use centralized exception handling through `@RestControllerAdvice`.
- Return consistent API response structures.
- Use `BigDecimal` for monetary values.
- Use `@Transactional` for operations requiring database consistency.
- Avoid duplicate or unnecessary database queries.
- Keep entities and DTOs separate.
- Avoid putting business logic in controllers or repositories.
- Do not expose internal exception messages or stack traces to API consumers.

## 6. Database and Booking Rules

- PostgreSQL is the primary database.
- Use appropriate primary keys, foreign keys, unique constraints and indexes.
- Maintain referential integrity.
- Avoid destructive schema-generation settings in normal development.
- Use database transactions for booking operations.
- Prevent two users from successfully reserving the same seat.
- Validate flight availability and fare on the backend before confirming a booking.
- Never trust fare, seat availability, booking status or user identity supplied by the frontend without backend validation.
- Store monetary values using suitable decimal precision.

Initial flight inventory and schedules may be simulated.

Bookings and payments are demonstrations, not real airline reservations or financial transactions.

## 7. Frontend Engineering Rules

- Use React with TypeScript.
- Prefer reusable components and clearly separated pages.
- Keep API communication separate from presentation components.
- Define TypeScript interfaces for API requests and responses.
- Use appropriate form validation.
- Handle loading, success, error and empty states.
- Avoid unnecessary state management libraries.
- Maintain a clean, consistent and responsive user interface.
- Do not hardcode backend business rules in the frontend.

## 8. Spring AI and AeroMate Rules

AeroMate is an assistant for airline-related information and booking assistance.

### RAG
- Use approved airline policy and FAQ documents as the knowledge source.
- Keep document ingestion, transformation, chunking and retrieval responsibilities separated.
- Provide relevant retrieved context to the model.
- Do not invent airline policies when relevant information is unavailable.
- Clearly communicate when the knowledge base does not contain an answer.

### Tool Calling
- Expose selected application capabilities through well-defined Java tools.
- Suitable initial tools include flight search, flight details and authenticated user's booking retrieval.
- Validate all tool inputs.
- Obtain user identity from the authenticated server-side security context, never from an LLM-supplied user ID.
- The backend remains the source of truth for application data.

### Chat Memory
- Maintain conversation context using a conversation identifier.
- Persist conversation memory where feasible using PostgreSQL/JDBC.
- Ensure users can only access their own conversations.
- Keep conversational memory separate from the airline policy vector knowledge base.

### MCP
- Demonstrate MCP through a small, clearly scoped integration.
- Expose selected capabilities such as flight search or flight details.
- Do not duplicate the entire application's business logic in the MCP layer.
- Keep the MCP implementation simple and demonstrable.

### AI Safety and Business Integrity
- The LLM must never independently confirm bookings, cancel tickets, process payments or modify flight inventory.
- Booking and cancellation operations must be validated and executed by backend business services.
- Require explicit user confirmation before any booking-related mutation initiated through the assistant.
- Never treat LLM output or retrieved documents as trusted executable instructions.
- Do not expose API keys, passwords, internal system prompts or sensitive data.
- Never commit secrets to Git.

## 9. GitHub Copilot Workflow

Work as a senior engineer collaborating with the developer, not as an autonomous code generator.

For every significant task:

1. Inspect the existing repository and relevant files.
2. Understand the requirement and identify dependencies.
3. Present a concise implementation plan before making significant changes.
4. Identify the files that need to be created or modified.
5. Explain important architectural decisions and trade-offs.
6. Implement only the approved scope.
7. Run relevant tests and build commands.
8. Review the changes for bugs, security concerns and unnecessary complexity.
9. Summarize the implementation and report actual test results.

Do not rewrite working code without a clear reason.

Do not create unrelated files or modify unrelated modules.

Do not assume a class, dependency, endpoint or configuration exists. Inspect the repository first.

Ask for clarification when a requirement is ambiguous or a decision has significant architectural consequences.

## 10. Prompt Engineering and Documentation

The project must demonstrate effective AI-assisted development.

- Maintain a record of significant prompts used with GitHub Copilot.
- Record the requirement, original prompt, Copilot plan, refinements, generated changes and validation results.
- Document important human decisions and corrections.
- Record failed attempts and explain why prompts were refined.
- Never fabricate prompt history, test results or implementation details.
- Keep project documentation consistent with the actual implementation.

## 11. Scope and Time Management

The project has a four-day development target.

Prioritize:

1. Working core booking functionality.
2. Correct backend business logic and database operations.
3. Functional React frontend.
4. Demonstrable Spring AI capabilities.
5. Testing and required project documentation.

Avoid unnecessary features, including:
- Real airline API integration unless time permits.
- Real payment gateway integration.
- Microservices.
- Kubernetes.
- Complex recommendation algorithms.
- Unnecessary enterprise infrastructure.

Any additional feature must be justified against the remaining time.

## 12. Definition of Done

A task is complete only when:

- The requested functionality is implemented.
- The code follows the project architecture.
- Input validation and error handling are considered.
- Relevant tests have been executed.
- The application builds successfully where applicable.
- API and project documentation are updated when necessary.
- Actual implementation and test results are reported.

**Most important principle:** Build a working, understandable and maintainable application. Prefer simple solutions that satisfy the requirements over complicated solutions that consume time without adding meaningful value.