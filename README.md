# Expense Tracker API

[![CI](https://github.com/shivajichaprana/spring-boot-expense-tracker/actions/workflows/ci.yml/badge.svg)](https://github.com/shivajichaprana/spring-boot-expense-tracker/actions/workflows/ci.yml)
[![Java](https://img.shields.io/badge/Java-17-orange)](https://openjdk.org/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4-green)](https://spring.io/projects/spring-boot)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

Production-quality REST API for personal expense tracking, built with Spring Boot 3. Features JWT authentication with refresh tokens, full CRUD with validation, monthly/yearly analytics with Redis caching, Flyway migrations, and comprehensive test coverage.

## Architecture

```
┌──────────────────────────────────────────────────────────┐
│                      Client (Postman / Swagger UI)       │
└──────────────────────┬───────────────────────────────────┘
                       │  HTTP + JWT Bearer Token
                       ▼
┌──────────────────────────────────────────────────────────┐
│                    Spring Security Filter Chain           │
│  ┌─────────────────┐  ┌───────────────────────────────┐  │
│  │ JwtAuthFilter    │→│ SecurityConfig (permit/auth)   │  │
│  └─────────────────┘  └───────────────────────────────┘  │
└──────────────────────┬───────────────────────────────────┘
                       ▼
┌──────────────────────────────────────────────────────────┐
│                    REST Controllers                       │
│  ┌──────────────┐ ┌────────────────┐ ┌────────────────┐  │
│  │ AuthController│ │ExpenseController│ │AnalyticsCtrl   │  │
│  │ /api/v1/auth │ │ /api/v1/expenses│ │/api/v1/analytics│ │
│  └──────┬───────┘ └───────┬────────┘ └───────┬────────┘  │
└─────────┼─────────────────┼──────────────────┼───────────┘
          ▼                 ▼                  ▼
┌──────────────────────────────────────────────────────────┐
│                    Service Layer                          │
│  ┌──────────────┐ ┌────────────────┐ ┌────────────────┐  │
│  │ AuthService   │ │ ExpenseService │ │AnalyticsService│  │
│  │ (UserDetails) │ │ (CRUD + cache) │ │ (@Cacheable)   │  │
│  └──────┬───────┘ └───────┬────────┘ └───────┬────────┘  │
└─────────┼─────────────────┼──────────────────┼───────────┘
          ▼                 ▼                  ▼
┌─────────────────────┐  ┌──────────────────────────────────┐
│   JPA Repositories  │  │          Redis Cache              │
│  ┌───────────────┐  │  │  ┌────────────────────────────┐  │
│  │UserRepository │  │  │  │ analytics cache (TTL: 30m) │  │
│  │ExpenseRepo    │  │  │  └────────────────────────────┘  │
│  └───────┬───────┘  │  └──────────────────────────────────┘
└──────────┼──────────┘
           ▼
┌──────────────────────┐
│   PostgreSQL 16      │
│  ┌────────────────┐  │
│  │ users          │  │
│  │ expenses       │  │
│  │ (Flyway mgmt)  │  │
│  └────────────────┘  │
└──────────────────────┘
```

## Tech Stack

| Technology | Purpose |
|-----------|---------|
| Java 17 | LTS with modern features (records, sealed classes, pattern matching) |
| Spring Boot 3.4 | Web framework, dependency injection, auto-configuration |
| Spring Security 6 | JWT authentication with access + refresh tokens |
| Spring Data JPA | Repository abstraction over Hibernate |
| PostgreSQL 16 | Production relational database |
| Redis 7 | Response caching for analytics endpoints |
| Flyway | Versioned database migrations |
| SpringDoc OpenAPI | Auto-generated Swagger UI from annotations |
| JUnit 5 + Mockito | Unit and controller testing |
| Testcontainers | Integration tests with real PostgreSQL |
| Docker Compose | Local development environment |
| JaCoCo | Code coverage reporting |

## API Endpoints

### Authentication (public)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/auth/register` | Create a new account |
| POST | `/api/v1/auth/login` | Login and receive JWT tokens |
| POST | `/api/v1/auth/refresh` | Refresh expired access token |

### Expenses (authenticated)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/expenses` | Create a new expense |
| GET | `/api/v1/expenses` | List expenses (paginated, filterable) |
| GET | `/api/v1/expenses/{id}` | Get expense by ID |
| PUT | `/api/v1/expenses/{id}` | Update an expense |
| DELETE | `/api/v1/expenses/{id}` | Delete an expense |

**Query Parameters:** `category`, `startDate`, `endDate`, `page`, `size`, `sort`

### Analytics (authenticated, cached)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/analytics?startDate=...&endDate=...` | Custom date range analytics |
| GET | `/api/v1/analytics/monthly` | Current month summary |
| GET | `/api/v1/analytics/yearly` | Current year summary |

## Quick Start

### Prerequisites

- Java 17+
- Docker & Docker Compose

### Run with Docker Compose

```bash
# Start everything (PostgreSQL + Redis + App)
docker compose up -d

# App is available at http://localhost:8080
# Swagger UI at http://localhost:8080/swagger-ui.html
```

### Run for Development

```bash
# Start dependencies
docker compose up -d postgres redis

# Run the application
./mvnw spring-boot:run

# Run tests
./mvnw test

# Run tests with coverage report
./mvnw verify
# Report at: target/site/jacoco/index.html
```

## Sample Requests

### Register

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Shivaji",
    "email": "shivaji@example.com",
    "password": "password123"
  }'
```

### Create Expense

```bash
curl -X POST http://localhost:8080/api/v1/expenses \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <access_token>" \
  -d '{
    "amount": 25.50,
    "category": "FOOD",
    "description": "Lunch",
    "date": "2026-03-15"
  }'
```

### Get Monthly Analytics

```bash
curl http://localhost:8080/api/v1/analytics/monthly \
  -H "Authorization: Bearer <access_token>"
```

**Response:**

```json
{
  "totalAmount": 12450.00,
  "totalTransactions": 47,
  "averageAmount": 264.89,
  "byCategory": {
    "HOUSING": 8000.00,
    "FOOD": 2500.00,
    "TRANSPORT": 1200.00,
    "UTILITIES": 750.00
  },
  "monthlyTrends": [
    { "year": 2026, "month": 3, "total": 12450.00, "count": 47 }
  ]
}
```

## Project Structure

```
spring-boot-expense-tracker/
├── src/
│   ├── main/java/com/shivaji/expensetracker/
│   │   ├── config/           # Security, Redis, OpenAPI configuration
│   │   ├── controller/       # REST endpoints (Auth, Expense, Analytics)
│   │   ├── dto/              # Request/Response DTOs with validation
│   │   ├── exception/        # Global exception handler
│   │   ├── model/            # JPA entities (User, Expense, Category)
│   │   ├── repository/       # Spring Data JPA repositories
│   │   ├── security/         # JWT provider and authentication filter
│   │   └── service/          # Business logic layer
│   ├── main/resources/
│   │   ├── application.yml        # Main configuration
│   │   ├── application-dev.yml    # Development overrides
│   │   ├── application-test.yml   # Test configuration (H2)
│   │   └── db/migration/         # Flyway SQL migrations
│   └── test/java/com/shivaji/expensetracker/
│       ├── controller/       # MockMvc controller tests
│       ├── service/          # Unit tests with Mockito
│       └── repository/       # @DataJpaTest repository tests
├── postman/                  # Importable Postman collection
├── docker-compose.yml        # PostgreSQL + Redis + App
├── Dockerfile                # Multi-stage build
├── .github/workflows/ci.yml  # GitHub Actions CI pipeline
└── pom.xml                   # Maven build with JaCoCo coverage
```

## Testing Strategy

| Layer | Approach | Framework |
|-------|----------|-----------|
| Service | Unit tests with mocked dependencies | JUnit 5 + Mockito |
| Controller | Slice tests with MockMvc | @WebMvcTest |
| Repository | Database tests with H2 | @DataJpaTest |
| Integration | Full stack with real database | Testcontainers |

## Categories

`FOOD` · `TRANSPORT` · `UTILITIES` · `ENTERTAINMENT` · `HEALTHCARE` · `SHOPPING` · `EDUCATION` · `HOUSING` · `INSURANCE` · `SAVINGS` · `PERSONAL` · `OTHER`

## License

[MIT](LICENSE)
