[Coding Aider Plan - Checklist]

# User Roles Tab Implementation Checklist

## User Detail Component Modifications
- [x] Modify user-detail component to use a tab layout
- [x] Create a "Basic Info" tab for existing user information
- [x] Add a "Roles" tab that will contain the role management UI
- [x] Ensure proper routing and state management between tabs

## User Roles Component
- [x] Create a new user-roles component
- [x] Implement the UI for displaying available roles with checkboxes
- [x] Add logic to fetch user's current role assignments
- [x] Add logic to fetch all available roles
- [x] Implement selection/deselection of roles
- [x] Add save functionality to update role assignments
- [x] Implement proper loading states
- [x] Add error handling for API calls

## Service Layer
- [x] Add method to UsersService to get user roles
- [x] Add method to UsersService to update user roles
- [x] Create helper methods for transforming role data between API and UI formats

## Testing
- [ ] Test role display functionality
- [ ] Test role selection/deselection
- [ ] Test saving role changes
- [ ] Test error handling
- [ ] Test authorization checks

## UI/UX
- [x] Ensure consistent styling with the rest of the application
- [x] Add appropriate loading indicators
- [x] Implement user-friendly error messages
- [ ] Add confirmation for role changes if needed
