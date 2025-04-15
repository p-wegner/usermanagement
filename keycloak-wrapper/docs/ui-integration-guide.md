# UI Integration Guide for Multitenancy

## Overview

This document provides guidelines for integrating the UI with the multitenancy backend, ensuring that the UI only displays and manages permitted users, roles, and groups based on the current user's access level.

## User Access Levels

The system has three main access levels:

1. **System Administrators** (`ADMIN` role)
   - Can access and manage all tenants, users, roles, and groups
   - Have full access to all functionality

2. **Tenant Administrators** (`TENANT_ADMIN` role)
   - Can only access and manage users, roles, and groups within their assigned tenants
   - Cannot modify system roles or access other tenants

3. **Regular Users**
   - Can only access resources within their assigned tenant(s)
   - Cannot manage other users or assign roles

## UI Components and Access Control

### Navigation Menu

- **System Admin View**: Show all menu items including system configuration
- **Tenant Admin View**: Show only tenant-specific menu items for tenants they manage
- **Regular User View**: Show only basic user functionality

Example implementation:

```typescript
// In a navigation component
import { AuthService } from '../core/auth/auth.service';

@Component({
  selector: 'app-navigation',
  template: `
    <nav>
      <!-- Always visible -->
      <a routerLink="/dashboard">Dashboard</a>
      
      <!-- Only for admins and tenant admins -->
      <ng-container *ngIf="isAdmin || isTenantAdmin">
        <a routerLink="/users">Users</a>
        <a routerLink="/groups">Groups</a>
      </ng-container>
      
      <!-- Only for system admins -->
      <ng-container *ngIf="isAdmin">
        <a routerLink="/tenants">All Tenants</a>
        <a routerLink="/system-config">System Configuration</a>
      </ng-container>
      
      <!-- For tenant admins -->
      <ng-container *ngIf="isTenantAdmin && !isAdmin">
        <a routerLink="/my-tenants">My Tenants</a>
      </ng-container>
    </nav>
  `
})
export class NavigationComponent implements OnInit {
  isAdmin = false;
  isTenantAdmin = false;
  
  constructor(private authService: AuthService) {}
  
  ngOnInit() {
    this.isAdmin = this.authService.hasRole('ADMIN');
    this.isTenantAdmin = this.authService.hasRole('TENANT_ADMIN');
  }
}
```

### User Management

#### User List

- **System Admins**: Show all users with full management capabilities
- **Tenant Admins**: Show only users in their tenants with limited management capabilities
- **Regular Users**: Do not show user management

Example implementation:

```typescript
// In users service
getUsers(page: number, size: number, search?: string, tenantId?: string): Observable<UserListResponse> {
  let params = new HttpParams()
    .set('page', page.toString())
    .set('size', size.toString());
    
  if (search) {
    params = params.set('search', search);
  }
  
  // For tenant admins, automatically filter by their tenant
  if (this.authService.isTenantAdmin() && !this.authService.isSystemAdmin()) {
    // Get the first tenant they manage if no specific tenant is requested
    if (!tenantId) {
      return this.tenantAdminService.getMyTenants().pipe(
        switchMap(tenants => {
          if (tenants.length > 0) {
            params = params.set('tenantId', tenants[0].id);
          }
          return this.apiUsersService.getUsers(params);
        })
      );
    } else {
      params = params.set('tenantId', tenantId);
    }
  }
  
  return this.apiUsersService.getUsers(params);
}
```

#### User Creation Form

- **System Admins**: Show all fields including system role assignment and tenant selection
- **Tenant Admins**: Show limited fields, pre-select their tenant, hide system roles

Example implementation:

```typescript
// In user creation component
import { AuthService } from '../core/auth/auth.service';

@Component({
  selector: 'app-user-create',
  template: `
    <form [formGroup]="userForm" (ngSubmit)="onSubmit()">
      <!-- Basic fields for all admin users -->
      <input formControlName="username" placeholder="Username">
      <input formControlName="firstName" placeholder="First Name">
      <input formControlName="lastName" placeholder="Last Name">
      <input formControlName="email" placeholder="Email">
      <input type="password" formControlName="password" placeholder="Password">
      
      <!-- Tenant selection - only for system admins -->
      <ng-container *ngIf="isAdmin">
        <select formControlName="tenantId">
          <option *ngFor="let tenant of tenants" [value]="tenant.id">
            {{tenant.displayName}}
          </option>
        </select>
      </ng-container>
      
      <!-- Role selection - filtered based on access -->
      <div *ngIf="isAdmin || isTenantAdmin">
        <h4>Roles</h4>
        <div *ngFor="let role of availableRoles">
          <label>
            <input type="checkbox" 
                  [value]="role.id" 
                  (change)="onRoleChange($event, role.id)"
                  [disabled]="isRestrictedRole(role) && !isAdmin">
            {{role.name}}
          </label>
        </div>
      </div>
      
      <button type="submit" [disabled]="userForm.invalid">Create User</button>
    </form>
  `
})
export class UserCreateComponent implements OnInit {
  userForm: FormGroup;
  tenants: TenantDto[] = [];
  availableRoles: RoleDto[] = [];
  isAdmin = false;
  isTenantAdmin = false;
  
  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private usersService: UsersService,
    private tenantsService: TenantsService,
    private rolesService: RolesService
  ) {
    this.userForm = this.fb.group({
      username: ['', Validators.required],
      firstName: [''],
      lastName: [''],
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required],
      tenantId: [''],
      roles: [[]]
    });
  }
  
  ngOnInit() {
    this.isAdmin = this.authService.hasRole('ADMIN');
    this.isTenantAdmin = this.authService.hasRole('TENANT_ADMIN');
    
    // Load available roles based on access level
    this.loadRoles();
    
    // For system admins, load all tenants
    if (this.isAdmin) {
      this.tenantsService.getTenants().subscribe(tenants => {
        this.tenants = tenants;
      });
    } 
    // For tenant admins, load their tenants and pre-select the first one
    else if (this.isTenantAdmin) {
      this.tenantsService.getMyTenants().subscribe(tenants => {
        this.tenants = tenants;
        if (tenants.length > 0) {
          this.userForm.get('tenantId').setValue(tenants[0].id);
        }
      });
    }
  }
  
  loadRoles() {
    this.rolesService.getRoles().subscribe(roles => {
      // Filter out system roles for tenant admins
      if (!this.isAdmin) {
        this.availableRoles = roles.filter(role => 
          !['ADMIN', 'TENANT_ADMIN'].includes(role.name)
        );
      } else {
        this.availableRoles = roles;
      }
    });
  }
  
  isRestrictedRole(role: RoleDto): boolean {
    return ['ADMIN', 'TENANT_ADMIN'].includes(role.name);
  }
  
  onRoleChange(event: any, roleId: string) {
    const roles = this.userForm.get('roles').value;
    if (event.target.checked) {
      roles.push(roleId);
    } else {
      const index = roles.indexOf(roleId);
      if (index > -1) {
        roles.splice(index, 1);
      }
    }
    this.userForm.get('roles').setValue(roles);
  }
  
  onSubmit() {
    if (this.userForm.valid) {
      this.usersService.createUser(this.userForm.value).subscribe(
        response => {
          // Handle success
        },
        error => {
          // Handle error
        }
      );
    }
  }
}
```

### Tenant Management

#### Tenant List

- **System Admins**: Show all tenants with full management capabilities
- **Tenant Admins**: Show only their assigned tenants with limited management capabilities
- **Regular Users**: Do not show tenant management

Example implementation:

```typescript
// In tenants component
@Component({
  selector: 'app-tenants',
  template: `
    <div class="tenants-container">
      <h2>Tenants</h2>
      
      <!-- Create tenant button - only for system admins -->
      <button *ngIf="isAdmin" (click)="openCreateTenantDialog()">Create Tenant</button>
      
      <!-- Tenant list -->
      <table>
        <thead>
          <tr>
            <th>Name</th>
            <th>Display Name</th>
            <th>Users</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let tenant of tenants">
            <td>{{tenant.name}}</td>
            <td>{{tenant.displayName}}</td>
            <td>
              <button (click)="viewUsers(tenant.id)">View Users</button>
            </td>
            <td>
              <button (click)="editTenant(tenant)">Edit</button>
              <!-- Delete button - only for system admins -->
              <button *ngIf="isAdmin" (click)="deleteTenant(tenant.id)">Delete</button>
              <!-- Statistics button - for all admins -->
              <button (click)="viewStatistics(tenant.id)">Statistics</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  `
})
export class TenantsComponent implements OnInit {
  tenants: TenantDto[] = [];
  isAdmin = false;
  
  constructor(
    private tenantsService: TenantsService,
    private authService: AuthService,
    private dialog: MatDialog,
    private router: Router
  ) {}
  
  ngOnInit() {
    this.isAdmin = this.authService.hasRole('ADMIN');
    this.loadTenants();
  }
  
  loadTenants() {
    // For system admins, load all tenants
    // For tenant admins, load only their tenants
    if (this.isAdmin) {
      this.tenantsService.getAllTenants().subscribe(tenants => {
        this.tenants = tenants;
      });
    } else {
      this.tenantsService.getMyTenants().subscribe(tenants => {
        this.tenants = tenants;
      });
    }
  }
  
  openCreateTenantDialog() {
    const dialogRef = this.dialog.open(CreateTenantDialogComponent);
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadTenants();
      }
    });
  }
  
  editTenant(tenant: TenantDto) {
    const dialogRef = this.dialog.open(EditTenantDialogComponent, {
      data: { tenant }
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadTenants();
      }
    });
  }
  
  deleteTenant(tenantId: string) {
    if (confirm('Are you sure you want to delete this tenant?')) {
      this.tenantsService.deleteTenant(tenantId).subscribe(() => {
        this.loadTenants();
      });
    }
  }
  
  viewUsers(tenantId: string) {
    this.router.navigate(['/tenants', tenantId, 'users']);
  }
  
  viewStatistics(tenantId: string) {
    this.router.navigate(['/tenants', tenantId, 'statistics']);
  }
}
```

### Role and Group Management

- **System Admins**: Show all roles and groups with full management capabilities
- **Tenant Admins**: Show only roles and groups within their tenants with limited management capabilities
- **Regular Users**: Do not show role and group management

Example implementation:

```typescript
// In roles service
getRolesForCurrentUser(): Observable<RoleDto[]> {
  if (this.authService.isSystemAdmin()) {
    // System admins can see all roles
    return this.apiRolesService.getRoles();
  } else if (this.authService.isTenantAdmin()) {
    // Tenant admins can only see roles within their tenant scope
    return this.tenantAdminService.getMyTenants().pipe(
      switchMap(tenants => {
        if (tenants.length === 0) {
          return of([]);
        }
        
        // Get roles from the first tenant they manage
        return this.apiRolesService.getTenantRoles(tenants[0].id);
      })
    );
  } else {
    // Regular users can't manage roles
    return of([]);
  }
}
```

## Error Handling and Access Denied

When a user attempts to access a resource they don't have permission for, display an appropriate error message:

```typescript
// In an interceptor or error handling service
handleError(error: HttpErrorResponse): Observable<never> {
  if (error.status === 403) {
    // Access denied
    this.notificationService.showError(
      'You do not have permission to perform this action.'
    );
    
    // Optionally redirect to an access denied page
    this.router.navigate(['/access-denied']);
  } else if (error.status === 404) {
    // Resource not found
    this.notificationService.showError(
      'The requested resource was not found.'
    );
  } else {
    // Other errors
    this.notificationService.showError(
      'An error occurred. Please try again later.'
    );
  }
  
  return throwError(error);
}
```

## Best Practices

1. **Always check permissions client-side before showing UI elements**
   - Hide buttons, forms, and navigation items based on user roles
   - This improves user experience by not showing options that will result in access denied

2. **Implement server-side validation for all requests**
   - Never rely solely on UI hiding for security
   - Always validate permissions on the server side

3. **Use role-based guards for routes**
   - Prevent unauthorized access to routes based on user roles
   - Redirect to appropriate pages when access is denied

4. **Implement tenant-aware components**
   - Design components to be aware of the current tenant context
   - Filter data based on tenant access

5. **Provide clear feedback on permission errors**
   - Show helpful error messages when access is denied
   - Guide users to appropriate actions based on their permissions

## Implementation Checklist

- [ ] Update AuthService to properly check for roles and tenant access
- [ ] Implement role-based route guards
- [ ] Update navigation menu to show only permitted items
- [ ] Modify user management components to respect tenant boundaries
- [ ] Update tenant management components for proper access control
- [ ] Implement proper error handling for permission issues
- [ ] Add tenant context awareness to all relevant components
- [ ] Test all UI flows with different user roles
