# Keycloak User Management

A Gradle multimodule project that provides a comprehensive user management interface for Keycloak, consisting of:
- Kotlin Spring Boot backend providing a REST API that delegates calls to Keycloak
- Angular frontend for user interaction

## Features

- User Management: Create, edit, delete, and search users with pagination
- Group Management: Create, edit, delete, and search groups with pagination
- Role Management: Create, edit, delete, and search roles (including composite roles) with pagination
- Access Control: Assign roles to users and groups
- Tenant Management: Create and manage multi-tenant environments
- Permission Management: Fine-grained access control for tenant administrators
- Resource Isolation: Customer admins can only see and manage their respective client roles, users, and groups

## Multi-Tenant Features

- Tenant Creation: Create isolated tenant environments
- Tenant Administration: Assign tenant administrators with limited privileges
- Role-Based Access Control: System admins vs. tenant admins
- User Isolation: Users can only see and manage resources within their tenant

## Architecture

The application follows a multi-layered architecture:

- **Frontend**: Angular-based SPA with role-based UI components
- **Backend**: Spring Boot application with the following layers:
  - Controllers: REST API endpoints
  - Services: Business logic
  - Facades: Interaction with Keycloak Admin API
  - Mappers: Convert between DTOs and Keycloak representations
  - Security: Role-based access control and tenant isolation

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

6. Access the application:
   - Frontend: http://localhost:4200
   - Backend API: http://localhost:8080
   - Keycloak Admin Console: http://localhost:8081/admin

7. Initial login:
   - Use the default admin credentials (admin/admin)
   - You'll be redirected to the dashboard after successful authentication

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

### User Roles

The application supports the following user roles:

1. **System Administrator** (`ROLE_ADMIN`):
   - Full access to all features
   - Can create and manage tenants
   - Can assign tenant administrators

2. **Tenant Administrator** (`ROLE_TENANT_ADMIN`):
   - Can manage users within their assigned tenant(s)
   - Limited access to role and group management
   - Can only see and manage client roles, users, and groups within their tenant
   - Cannot create or manage other tenants

3. **Regular User**:
   - Limited access based on assigned roles
   - Can only view their own profile

## Tenant Management

### Creating a Tenant

1. Log in as a system administrator
2. Navigate to the "Tenants" section
3. Click "Create Tenant"
4. Enter a unique name and display name
5. Submit the form

### Assigning Tenant Administrators

1. Navigate to the tenant details page
2. Click "Manage Administrators"
3. Search for users to assign as administrators
4. Select users and click "Assign"

### Managing Users within a Tenant

1. Log in as a tenant administrator
2. Navigate to the "Users" section
3. You'll only see users within your tenant
4. Create, edit, or delete users as needed

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

3. Run integration tests:
```shell
cd keycloak-wrapper
./gradlew integrationTest
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
- [Angular Security Best Practices](https://angular.io/guide/security)
- [Keycloak Admin REST API](https://www.keycloak.org/docs-api/23.0.0/rest-api/index.html)
