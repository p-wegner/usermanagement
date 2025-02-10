[Coding Aider Plan]

## Overview
Integrate Keycloak with the existing Kotlin backend using Docker Compose and Spring Boot 3's TestContainers support for managing Docker containers. This will provide a consistent development environment and simplified testing setup.

## Problem Description
Currently, the application assumes a manually configured Keycloak instance. This creates several issues:
1. Developers need to manually set up Keycloak
2. Configuration may differ between environments
3. No automated test environment setup
4. Potential version mismatches between environments

## Goals
1. Create a Docker Compose configuration for Keycloak
2. Configure Spring Boot to manage the Docker container lifecycle
3. Update application properties to connect to the dockerized Keycloak
4. Provide development and test configurations
5. Document the setup process

## Additional Notes and Constraints
- Must support both development and test environments
- Should provide reasonable defaults for development
- Must maintain existing Keycloak wrapper functionality
- Should support easy configuration changes
- Must handle container lifecycle appropriately

## References
- Spring Boot 3 Docker Compose Support: https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.docker-compose
- Keycloak Docker Image: https://hub.docker.com/r/keycloak/keycloak
- TestContainers: https://java.testcontainers.org/
