[Coding Aider Plan]

# Add User Roles Tab to User Details View

## Overview
This plan outlines the implementation of a new tab in the user details view that allows administrators to view and modify the roles assigned to a user. This feature will enhance the user management capabilities of the application by providing a dedicated interface for role management within the user context.

## Problem Description
Currently, the user details view only allows editing basic user information such as name, email, and enabled status. There is no way to view or modify the roles assigned to a user directly from the user interface. Administrators need to be able to easily see what roles a user has and modify these assignments as needed.

## Goals
1. Add a new "Roles" tab to the user detail view
2. Display all available roles with checkboxes indicating which ones are assigned to the user
3. Allow administrators to modify role assignments by checking/unchecking roles
4. Save changes to the backend when the user submits the form
5. Show appropriate loading states and error messages
6. Ensure proper authorization checks for role management

## Implementation Details
1. Modify the user-detail component to include a tab layout
2. Create a new user-roles component for the roles tab content
3. Implement a service method to fetch available roles and user role assignments
4. Create the UI for displaying and selecting roles
5. Implement save functionality to update role assignments
6. Add proper error handling and loading indicators

## Additional Notes and Constraints
- Only users with the 'ADMIN' role should be able to modify role assignments
- The implementation should use the existing API endpoints for role management
- The UI should be consistent with the existing application design
- Consider performance implications when loading all available roles

## References
- Existing user-detail component
- RoleControllerService API
- UsersService API
- AuthService for permission checks
