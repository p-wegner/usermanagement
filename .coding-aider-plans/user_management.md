[Coding Aider Plan]

## Overview
Implement a comprehensive user management interface in Angular that allows administrators to manage users, groups, and permissions. The system will provide CRUD operations for users, user collections, permission groups, and individual permissions.

## Problem Description
The application needs a user management system with these key features:
- User management (create, read, update, delete)
- User collections management
- Permission groups management
- Individual permission assignments
- Search functionality across all lists
- Consistent UI/UX using Angular Material
- Reusable components for list views and detail forms

## Goals
1. Create a modular, maintainable user management system
2. Implement consistent UI patterns using Angular Material
3. Create reusable components for common patterns
4. Ensure good user experience with search and filtering
5. Support basic CRUD operations for all entities
6. Implement proper routing and navigation

## Additional Notes and Constraints
- Use Angular Material components for consistency
- Implement proper form validation
- Ensure responsive design
- Follow Angular best practices
- Use TypeScript interfaces for data models
- Implement proper error handling
- Consider accessibility requirements

## References
- Angular Material: https://material.angular.io/
- Angular Router: https://angular.io/guide/router
- Angular Forms: https://angular.io/guide/forms-overview

## Subplans
<!-- SUBPLAN:user_management_shared -->
[Subplan: Shared Components](user_management_shared.md)
<!-- END_SUBPLAN -->

<!-- SUBPLAN:user_management_users -->
[Subplan: Users Management](user_management_users.md)
<!-- END_SUBPLAN -->

<!-- SUBPLAN:user_management_groups -->
[Subplan: Groups Management](user_management_groups.md)
<!-- END_SUBPLAN -->

<!-- SUBPLAN:user_management_permissions -->
[Subplan: Permissions Management](user_management_permissions.md)
<!-- END_SUBPLAN -->
