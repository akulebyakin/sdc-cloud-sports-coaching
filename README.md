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

| Method | Endpoint                    | Description            |
|--------|-----------------------------|------------------------|
| GET    | `/health`                   | Health check           |
| GET    | `/api/coaches`              | List all coaches       |
| POST   | `/api/coaches`              | Create new coach       |
| GET    | `/api/coaches/{id}`         | Get coach by ID        |
| PUT    | `/api/coaches/{id}`         | Update coach           |
| DELETE | `/api/coaches/{id}`         | Delete coach           |
| POST   | `/api/coaches/rating`       | Update coach rating    |
| POST   | `/api/coaches/status`       | Update coach status    |
| GET    | `/api/users`                | List all users         |
| POST   | `/api/users`                | Create new user        |
| GET    | `/api/users/{id}`           | Get user by ID         |
| PUT    | `/api/users/{id}`           | Update user            |
| DELETE | `/api/users/{id}`           | Delete user            |
| GET    | `/api/sessions`             | List all sessions      |
| POST   | `/api/sessions`             | Create new session     |
| GET    | `/api/sessions/{id}`        | Get session by ID      |
| PUT    | `/api/sessions/{id}`        | Update session         |
| DELETE | `/api/sessions/{id}`        | Delete session         |
| POST   | `/api/sessions/{id}/rating` | Rate completed session |


## 2. Technologies Used

| Technology         | Version | Purpose                |
|--------------------|---------|------------------------|
| Java               | 17      | Programming language   |
| Spring Boot        | 3.2.1   | Application framework  |
| Spring Data JPA    | 3.2.1   | Database persistence   |
| Hibernate          | 6.x     | ORM framework          |
| Flyway             | 9.22.3  | Database migrations    |
| Azure SQL Database | -       | Cloud database service |
| MS SQL Server JDBC | 12.4.2  | Database driver        |
| Maven              | 3.x     | Build tool             |
| Lombok             | -       | Boilerplate reduction  |

---

## Database Schema

### Entity Relationship Diagram

```
┌─────────────────┐       ┌─────────────────┐       ┌─────────────────┐
│     Coach       │       │     Session     │       │    App_User     │
├─────────────────┤       ├─────────────────┤       ├─────────────────┤
│ coach_id (PK)   │──────<│ coach_id (FK)   │>──────│ user_id (PK)    │
│ first_name      │       │ user_id (FK)    │       │ first_name      │
│ last_name       │       │ session_id (PK) │       │ last_name       │
│ rating          │       │ session_date    │       │ sessions_taken  │
│ strike_count    │       │ session_status  │       └─────────────────┘
│ coach_status    │       │ rating          │
└─────────────────┘       └─────────────────┘
```

### Table Definitions

**Coach Table:**
- `coach_id` - Primary key, auto-increment
- `first_name` - VARCHAR(100), not null
- `last_name` - VARCHAR(100), not null
- `rating` - DECIMAL(3,2), range 0-10
- `strike_count` - INT, default 0
- `coach_status` - VARCHAR(20), ACTIVE/DEACTIVATED

**App_User Table:**
- `user_id` - Primary key, auto-increment
- `first_name` - VARCHAR(100), not null
- `last_name` - VARCHAR(100), not null
- `sessions_taken` - INT, default 0

**Session Table:**
- `session_id` - Primary key, auto-increment
- `session_date_time` - DATETIME, not null
- `session_status` - VARCHAR(20), SCHEDULED/COMPLETED/CANCELLED
- `coach_id` - Foreign key to Coach
- `user_id` - Foreign key to App_User
- `rating` - DECIMAL(3,2), nullable

### Connection Configuration

```yaml
spring:
  datasource:
    url: jdbc:sqlserver://${AZURE_SQL_SERVER}:1433;database=${AZURE_SQL_DATABASE};encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;
    username: ${AZURE_SQL_USERNAME}
    password: ${AZURE_SQL_PASSWORD}
    driver-class-name: com.microsoft.sqlserver.jdbc.SQLServerDriver
```

## Testing

```bash
mvn test
```

### Test Configuration

Tests use H2 in-memory database with SQL Server compatibility mode:

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MSSQLServer
```
