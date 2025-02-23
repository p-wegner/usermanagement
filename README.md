# Keycloak User Management

A Gradle multimodule project that provides a user management interface for Keycloak, consisting of:
- Kotlin Spring Boot backend providing a REST API that delegates calls to Keycloak
- Angular frontend for user interaction

## Features

- User Management: Create, edit, delete, and search users with pagination
- Group Management: Create, edit, delete, and search groups with pagination
- Role Management: Create, edit, delete, and search roles (including composite roles) with pagination
- Access Control: Assign roles to users and groups

## Not yet implemented
- Role Assignment: Assign Roles to users and groups

## Prerequisites

- Java 21 or higher
- Node.js 18 or higher
- Docker and Docker Compose
- Gradle 8.5 or higher

## Setup

1. Clone the repository:
```shell
git clone <repository-url>
cd usermanagement
```

2. Start Keycloak using Docker Compose:
```shell
docker-compose up -d
```

3. Wait for Keycloak to be ready (this may take a few minutes)
   - Check status: http://localhost:8081/health
   - Default admin credentials: 
     - Username: admin
     - Password: admin

4. Build and start the backend:
```shell
cd keycloak-wrapper
./gradlew bootRun
```

5. Build and start the frontend:
```shell
cd ../um
npm install
npm start
```

## Development

### Backend Development

- Access Swagger UI: http://localhost:8080/swagger-ui/index.html
- To use Swagger UI:
  1. Click the "Authorize" button at the top
  2. Use these credentials:
     - Username: admin
     - Password: admin
  3. Click "Authorize" to log in
  4. Close the authorization dialog
  5. You can now test the API endpoints
- Generate OpenAPI documentation:
```shell
./gradlew generateOpenApiJson
```

### Frontend Development

- The Angular dev server runs at: http://localhost:4200
- API calls are proxied to the backend at: http://localhost:8080

## Testing

1. Run backend tests:
```shell
cd keycloak-wrapper
./gradlew test
```

2. Run frontend tests:
```shell
cd um
npm test
```

## Docker Commands

### Basic Docker Compose Operations

- Start all services:
```shell
docker-compose up -d
```

- Stop all services:
```shell
docker-compose down
```

- View logs:
```shell
docker-compose logs -f
```

### Building Docker Images

Build the backend Docker image using Jib:
```shell
cd keycloak-wrapper
./gradlew jibDockerBuild
```

This will create a Docker image named `keycloak-wrapper:latest` locally.

To push to a remote registry (requires configuration):
```shell
./gradlew jib
```

You can customize the image name and registry in `build.gradle.kts` under the `jib` configuration.

## Useful Resources

- [Keycloak Documentation](https://www.keycloak.org/documentation)
- [Spring Boot Docker Compose Support](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.docker-compose)
- [SpringDoc OpenAPI Gradle Plugin](https://github.com/springdoc/springdoc-openapi-gradle-plugin)
