[Coding Aider Plan]

# Tenant Groups Implementation

## Overview
Implement tenant support using Keycloak groups where each tenant is modeled as a top-level group. Client roles will serve as blueprints for creating subgroups within each tenant group, enabling tenant-specific permissions for users.

## Problem Description
Currently, the system lacks a structured way to manage tenant-specific permissions. While Keycloak supports groups and roles, there's no automated mechanism to:
1. Create tenant groups with standardized subgroups based on client roles
2. Maintain consistency between client roles and tenant subgroups
3. Handle tenant-specific permissions through group membership

## Goals
1. Implement automatic creation of tenant subgroups based on client roles
2. Ensure synchronization between client roles and tenant subgroups
3. Support tenant-specific permissions through group membership
4. Maintain consistency when client roles are added or removed
5. Provide APIs to manage tenants and their permissions

## Additional Notes and Constraints
- Tenant groups will be top-level groups in Keycloak
- Each client role will correspond to a subgroup within tenant groups
- When new client roles are added, all existing tenant groups need to be updated
- Group naming convention:
  - Tenant groups: "tenant_{name}"
  - Permission subgroups: "{client_role_name}"
- Need to handle:
  - Tenant creation/deletion
  - Client role additions/removals
  - Group structure maintenance
  - Permission inheritance

## References
- [GroupDto](../keycloak-wrapper/src/main/kotlin/com/example/keycloak_wrapper/dto/GroupDto.kt)
- [GroupService](../keycloak-wrapper/src/main/kotlin/com/example/keycloak_wrapper/service/GroupService.kt)
- [KeycloakGroupFacade](../keycloak-wrapper/src/main/kotlin/com/example/keycloak_wrapper/facade/KeycloakGroupFacade.kt)
[Coding Aider Plan]

# Tenant Groups Implementation

## Overview
Implement tenant support using Keycloak groups where each tenant is modeled as a top-level group. Client roles will serve as blueprints for creating subgroups within each tenant group, enabling tenant-specific permissions for users.

## Problem Description
Currently, the system lacks a structured way to manage tenant-specific permissions. While Keycloak supports groups and roles, there's no automated mechanism to:
1. Create tenant groups with standardized subgroups based on client roles
2. Maintain consistency between client roles and tenant subgroups
3. Handle tenant-specific permissions through group membership

## Goals
1. Implement automatic creation of tenant subgroups based on client roles
2. Ensure synchronization between client roles and tenant subgroups
3. Support tenant-specific permissions through group membership
4. Maintain consistency when client roles are added or removed
5. Provide APIs to manage tenants and their permissions

## Additional Notes and Constraints
- Tenant groups will be top-level groups in Keycloak
- Each client role will correspond to a subgroup within tenant groups
- When new client roles are added, all existing tenant groups need to be updated
- Group naming convention:
  - Tenant groups: "tenant_{name}"
  - Permission subgroups: "{client_role_name}"
- Need to handle:
  - Tenant creation/deletion
  - Client role additions/removals
  - Group structure maintenance
  - Permission inheritance

## References
- [GroupDto](../keycloak-wrapper/src/main/kotlin/com/example/keycloak_wrapper/dto/GroupDto.kt)
- [GroupService](../keycloak-wrapper/src/main/kotlin/com/example/keycloak_wrapper/service/GroupService.kt)
- [KeycloakGroupFacade](../keycloak-wrapper/src/main/kotlin/com/example/keycloak_wrapper/facade/KeycloakGroupFacade.kt)
