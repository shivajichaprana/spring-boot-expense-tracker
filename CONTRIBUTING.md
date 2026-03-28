# Contributing to Expense Tracker API

Thanks for your interest in contributing! Here's how to get started.

## Development Setup

### Prerequisites

- Java 17+
- Docker & Docker Compose
- Maven 3.9+ (or use the included `./mvnw` wrapper)

### Local Development

```bash
# Start PostgreSQL and Redis
docker compose up -d postgres redis

# Run the application
./mvnw spring-boot:run

# Run tests
./mvnw test

# Run with coverage
./mvnw verify
```

### API Documentation

Once the app is running, Swagger UI is available at:
- http://localhost:8080/swagger-ui.html

## Code Style

- Follow standard Java conventions
- Use meaningful variable and method names
- Write Javadoc for public API methods
- Keep methods short and focused (< 30 lines)

## Commit Messages

We follow [Conventional Commits](https://www.conventionalcommits.org/):

```
feat: add monthly analytics endpoint
fix: handle null category in expense filter
docs: update API examples in README
test: add integration tests for auth flow
refactor: extract JWT logic into provider class
```

## Pull Request Process

1. Fork the repo and create a feature branch from `main`
2. Write tests for new functionality
3. Ensure all tests pass: `./mvnw verify`
4. Update the README if you add new endpoints
5. Open a PR with a clear description

## Testing Guidelines

- **Unit tests**: Use Mockito for service layer tests
- **Controller tests**: Use `@WebMvcTest` with MockMvc
- **Repository tests**: Use `@DataJpaTest` with H2
- **Integration tests**: Use Testcontainers for full stack tests

## Reporting Issues

Please include:
- Steps to reproduce
- Expected vs. actual behavior
- Java version and OS
- Relevant log output
