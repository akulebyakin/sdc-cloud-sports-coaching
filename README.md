# Online Sports Coaching Platform

Multi-module Spring Boot microservices application for managing online sports coaching sessions with Azure SQL Database and Azure Service Bus.

## Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  Review Service │───▶│ Azure Service   │───▶│ Session Service │
│     (8082)      │    │   Bus Queue     │    │     (8080)      │
└─────────────────┘    └─────────────────┘    └────────┬────────┘
                                                       │
                                                  REST │
                                                       ▼
                                              ┌─────────────────┐
                                              │  Coach Service  │
                                              │     (8081)      │
                                              └─────────────────┘
```

### Services

| Service | Port | Description |
|---------|------|-------------|
| **Session Service** | 8080 | User & Session management, receives reviews via Service Bus |
| **Coach Service** | 8081 | Coach management, rating & status updates |
| **Review Service** | 8082 | Review submission, sends to Session Service via Service Bus |

## Tech Stack

- Java 17
- Spring Boot 3.2.1
- Spring Data JPA / Hibernate
- Flyway migrations
- Azure SQL Database (or H2 for local dev)
- Azure Service Bus
- SpringDoc OpenAPI (Swagger)
- Docker & Docker Compose
- Lombok
- JUnit 5 / Mockito

## Project Structure

```
sdc-cloud-sports-coaching/
├── pom.xml                    # Parent POM
├── docker-compose.yml         # Azure deployment
├── docker-compose.local.yml   # Local development
├── common/                    # Shared DTOs, exceptions
├── session-service/           # Users & Sessions
├── coach-service/             # Coaches
└── review-service/            # Reviews
```

## Running Locally

### Option 1: Run each service individually with H2

```bash
# Build all modules
mvn clean install

# Run Coach Service (in terminal 1)
cd coach-service
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Run Session Service (in terminal 2)
cd session-service
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Run Review Service (in terminal 3)
cd review-service
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

### Option 2: Run with Docker Compose (local SQL Server)

```bash
docker-compose -f docker-compose.local.yml up --build
```

## Running with Azure

1. Copy `.env.example` to `.env` and configure:
```bash
cp .env.example .env
# Edit .env with your Azure credentials
```

2. Run with Docker Compose:
```bash
docker-compose up --build
```

## API Endpoints

### Session Service (port 8080)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/health` | Health check |
| GET | `/api/users` | List all users |
| POST | `/api/users` | Create new user |
| GET | `/api/users/{id}` | Get user by ID |
| PUT | `/api/users/{id}` | Update user |
| DELETE | `/api/users/{id}` | Delete user |
| GET | `/api/sessions` | List all sessions |
| POST | `/api/sessions` | Create new session |
| GET | `/api/sessions/{id}` | Get session by ID |
| PUT | `/api/sessions/{id}` | Update session |
| DELETE | `/api/sessions/{id}` | Delete session |
| POST | `/api/sessions/{id}/rating` | Rate completed session |

### Coach Service (port 8081)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/health` | Health check |
| GET | `/api/coaches` | List all coaches |
| POST | `/api/coaches` | Create new coach |
| GET | `/api/coaches/{id}` | Get coach by ID |
| PUT | `/api/coaches/{id}` | Update coach |
| DELETE | `/api/coaches/{id}` | Delete coach |
| POST | `/api/coaches/rating` | Update coach rating |
| POST | `/api/coaches/status` | Update coach status |

### Review Service (port 8082)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/health` | Health check |
| POST | `/api/reviews` | Submit session review |

## Swagger UI

- Session Service: http://localhost:8080/swagger-ui.html
- Coach Service: http://localhost:8081/swagger-ui.html
- Review Service: http://localhost:8082/swagger-ui.html

## Database Schema

### Session Service Database (session_db)

**app_users:**
- `user_id` - Primary key
- `first_name`, `last_name` - User name
- `sessions_taken` - Number of completed sessions

**sessions:**
- `session_id` - Primary key
- `session_date_time` - Session timestamp
- `session_status` - SCHEDULED/COMPLETED/CANCELLED
- `coach_id` - Reference to coach (in coach_db)
- `user_id` - Foreign key to app_users
- `rating` - Session rating (0-10)

### Coach Service Database (coach_db)

**coaches:**
- `coach_id` - Primary key
- `first_name`, `last_name` - Coach name
- `rating` - Average rating (0-10)
- `strike_count` - Low rating strikes
- `coach_status` - ACTIVE/DEACTIVATED

## Azure Service Bus Setup

### Using Azure CLI

```bash
# Login to Azure
az login

# Create resource group
az group create --name sdc-cloud-rg --location westeurope

# Create Service Bus namespace
az servicebus namespace create \
    --resource-group sdc-cloud-rg \
    --name sdc-sports-coaching-bus \
    --location westeurope \
    --sku Standard

# Create queue
az servicebus queue create \
    --resource-group sdc-cloud-rg \
    --namespace-name sdc-sports-coaching-bus \
    --name reviews-queue

# Get connection string
az servicebus namespace authorization-rule keys list \
    --resource-group sdc-cloud-rg \
    --namespace-name sdc-sports-coaching-bus \
    --name RootManageSharedAccessKey \
    --query primaryConnectionString \
    --output tsv
```

### Using Azure Portal

1. Go to [Azure Portal](https://portal.azure.com)
2. Create a resource → Search "Service Bus"
3. Create namespace:
   - Name: `sdc-sports-coaching-bus`
   - Pricing tier: Standard
4. Create queue:
   - Go to namespace → Queues → + Queue
   - Name: `reviews-queue`
5. Get connection string:
   - Go to Shared access policies → RootManageSharedAccessKey
   - Copy Primary Connection String

## Testing

```bash
# Run all tests
mvn test

# Run tests for specific module
mvn test -pl coach-service
mvn test -pl session-service
mvn test -pl review-service
```

## Business Logic

### Coach Rating System
- Coaches have a rating from 0-10
- Ratings below 2 result in a "strike"
- After 5 strikes, coach is automatically deactivated

### Review Flow
1. User submits review via Review Service (`POST /api/reviews`)
2. Review Service sends message to Azure Service Bus
3. Session Service receives message and updates session rating
4. Session Service calculates new coach average rating
5. Session Service sends rating update to Coach Service
6. Coach Service updates coach rating and checks for strikes
