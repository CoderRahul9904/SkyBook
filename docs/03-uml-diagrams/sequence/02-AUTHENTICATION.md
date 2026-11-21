# Sequence Diagram — User Authentication

## Mermaid Sequence Diagram

```mermaid
sequenceDiagram
    actor User as Guest or Returning User
    participant UI as React Frontend
    participant Controller as AuthController
    participant AuthService as AuthService
    participant UserRepo as UserRepository
    participant DB as PostgreSQL
    participant JWT as JwtAuthenticationService

    User->>UI: Enter email and password
    UI->>Controller: POST /api/v1/auth/login
    Controller->>AuthService: login(email, password)
    AuthService->>UserRepo: Find user by unique email
    UserRepo->>DB: Query user account

    alt Account not found or credential invalid
        DB-->>UserRepo: No matching account or account record
        UserRepo-->>AuthService: Not found or authentication failure
        AuthService-->>Controller: Generic authentication failure
        Controller-->>UI: Unauthorized response
        UI-->>User: Display invalid credentials
    else Account found and credential valid
        DB-->>UserRepo: User record
        UserRepo-->>AuthService: User
        AuthService->>AuthService: Read server-side CUSTOMER or ADMIN role
        AuthService->>JWT: generateToken(user)
        JWT-->>AuthService: Signed JWT
        AuthService-->>Controller: JWT and authentication result
        Controller-->>UI: Login success with JWT
        UI-->>User: Open role-appropriate interface

        opt Admin login
            JWT-->>UI: ADMIN role claim or server authorization context
        end
    end

    alt Missing or invalid protected request token
        User->>Controller: Protected request without valid JWT
        Controller->>JWT: validateToken(token)
        JWT-->>Controller: Invalid or missing token
        Controller-->>UI: Unauthorized or forbidden response
    end
```

## Workflow Explanation

The frontend submits login credentials to the authentication controller. `AuthService` retrieves the account through the repository and validates the password. Invalid credentials produce a generic authentication response so the system does not disclose account existence. A successful login creates a JWT containing server-controlled identity and role information. Protected requests validate the JWT and enforce CUSTOMER or ADMIN authorization.

## Consistency and Assumptions

- Uses the approved `/api/v1/auth/login` operation.
- Registration follows the same server-side account and unique-email rules but is not part of this login sequence.
- Password policy, JWT expiry, refresh tokens and logout behavior remain unresolved in the SRS.
- No client-supplied role or identity is trusted.
