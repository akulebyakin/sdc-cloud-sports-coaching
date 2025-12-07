# Online Sports Coaching Platform

Spring Boot application for managing online sports coaching sessions with Azure SQL Database.

## Features

- User management (CRUD operations)
- Coach management with rating and status tracking
- Session scheduling and rating
- Automatic coach deactivation after 5 low-rating strikes
- Health check endpoint

## Tech Stack

- Java 17
- Spring Boot 3.2.1
- Spring Data JPA / Hibernate
- Flyway migrations
- Azure SQL Database (or H2 for local dev)
- Lombok
- JUnit 5 / Mockito

## Database Schema

```
User (app_user)
├── userId (PK)
├── firstName
├── lastName
└── sessionsTaken

Coach
├── coachId (PK)
├── firstName
├── lastName
├── rating (0-10)
├── strikeCount
└── coachStatus (ACTIVE/DEACTIVATED)

Session
├── sessionId (PK)
├── sessionDateTime
├── sessionStatus (SCHEDULED/COMPLETED/CANCELLED)
├── coachId (FK)
├── userId (FK)
└── rating (0-10)
```

## Running Locally

```bash
# With H2 in-memory database
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

## Running with Azure SQL

```bash
export AZURE_SQL_SERVER=your-server.database.windows.net
export AZURE_SQL_DATABASE=sports_coaching
export AZURE_SQL_USERNAME=your-username
export AZURE_SQL_PASSWORD=your-password
mvn spring-boot:run
```

## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/health` | GET | Health check |
| `/api/users` | GET, POST | List/Create users |
| `/api/users/{id}` | GET, PUT, DELETE | User CRUD |
| `/api/coaches` | GET, POST | List/Create coaches |
| `/api/coaches/{id}` | GET, PUT, DELETE | Coach CRUD |
| `/api/coaches/rating` | POST | Update coach rating |
| `/api/coaches/status` | POST | Update coach status |
| `/api/sessions` | GET, POST | List/Create sessions |
| `/api/sessions/{id}` | GET, PUT, DELETE | Session CRUD |
| `/api/sessions/{id}/rating` | POST | Rate completed session |

## Testing

```bash
mvn test
```
