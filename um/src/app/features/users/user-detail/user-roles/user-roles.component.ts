import { Component, Input, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { forkJoin, of } from 'rxjs';
import { catchError, finalize, switchMap } from 'rxjs/operators';
import { RoleDto } from '../../../../api/com/example/model/role-dto.model';
import { RoleAssignmentDto } from '../../../../api/com/example/model/role-assignment-dto.model';
import { LoadingService } from '../../../../shared/services/loading.service';
import { ErrorHandlingService } from '../../../../shared/services/error-handling.service';
import { UsersService } from '../../users.service';
import { RoleService } from '../../../roles/role.service';
import { AuthService } from '../../../../core/auth/auth.service';

@Component({
  selector: 'app-user-roles',
  templateUrl: './user-roles.component.html',
  styleUrls: ['./user-roles.component.css'],
  standalone: false
})
export class UserRolesComponent implements OnInit {
  @Input() userId!: string;

  rolesForm: FormGroup;
  availableRoles: RoleDto[] = [];
  assignedRoleIds: string[] = [];
  originalRoleIds: string[] = [];
  isLoading = false;
  isAdmin = false;
  hasChanges = false;

  constructor(
    private fb: FormBuilder,
    private usersService: UsersService,
    private roleService: RoleService,
    public loadingService: LoadingService,
    private errorHandling: ErrorHandlingService,
    private snackBar: MatSnackBar,
    private authService: AuthService
  ) {
    this.rolesForm = this.fb.group({});
    // Check if user has admin role
    this.isAdmin = this.authService.getRoles().includes('ADMIN');
  }

  ngOnInit(): void {
    this.loadRoles();
  }

  loadRoles(): void {
    this.loadingService.startLoading();
    this.isLoading = true;

    // Use forkJoin to make parallel requests for all roles and user's assigned roles
    forkJoin({
      allRoles: this.roleService.getRoles().pipe(
        catchError(error => {
          this.errorHandling.handleError(error);
          return of({ roles: [] });
        })
      ),
      userRoles: this.usersService.getUserRoles(this.userId).pipe(
        catchError(error => {
          this.errorHandling.handleError(error);
          return of({ roleAssignment: { realmRoles: [], clientRoles: [], allRoleIds: [] } });
        })
      )
    }).pipe(
      finalize(() => {
        this.loadingService.stopLoading();
        this.isLoading = false;
      })
    ).subscribe(result => {
      this.availableRoles = result.allRoles.roles || [];
      this.assignedRoleIds = result.userRoles.roleAssignment?.allRoleIds || [];
      // Store original role IDs to detect changes
      this.originalRoleIds = [...this.assignedRoleIds];

      // Create form controls for each role
      this.createRoleFormControls();
    });
  }

  createRoleFormControls(): void {
    // Reset the form
    this.rolesForm = this.fb.group({});

    // Create a form control for each role
    this.availableRoles.forEach(role => {
      const isAssigned = this.assignedRoleIds.includes(role.id);
      this.rolesForm.addControl(role.id, this.fb.control(isAssigned));
    });

    // Subscribe to form value changes to detect when roles are modified
    this.rolesForm.valueChanges.subscribe(() => {
      this.checkForChanges();
    });
  }

  checkForChanges(): void {
    // Get current selected role IDs
    const currentSelectedRoleIds = Object.keys(this.rolesForm.value)
      .filter(roleId => this.rolesForm.value[roleId]);
    
    // Compare with original role IDs
    const hasAdded = currentSelectedRoleIds.some(id => !this.originalRoleIds.includes(id));
    const hasRemoved = this.originalRoleIds.some(id => !currentSelectedRoleIds.includes(id));
    
    this.hasChanges = hasAdded || hasRemoved;
  }

  saveRoles(): void {
    if (!this.isAdmin) {
      this.snackBar.open('You do not have permission to modify roles', 'Close', { duration: 3000 });
      return;
    }

    if (!this.hasChanges) {
      this.snackBar.open('No changes to save', 'Close', { duration: 3000 });
      return;
    }

    this.loadingService.startLoading();
    this.isLoading = true;

    // Get selected role IDs from form
    const selectedRoleIds = Object.keys(this.rolesForm.value)
      .filter(roleId => this.rolesForm.value[roleId]);

    // Create role assignment DTO
    const roleAssignment: RoleAssignmentDto = {
      realmRoles: this.availableRoles.filter(role => selectedRoleIds.includes(role.id)),
      clientRoles: [],
      allRoleIds: selectedRoleIds
    };

    this.usersService.updateUserRoles(this.userId, roleAssignment)
      .pipe(
        finalize(() => {
          this.loadingService.stopLoading();
          this.isLoading = false;
        })
      )
      .subscribe({
        next: () => {
          this.snackBar.open('User roles updated successfully', 'Close', { duration: 3000 });
          this.hasChanges = false;
          // Refresh the roles
          this.loadRoles();
        },
        error: (error) => {
          this.errorHandling.handleError(error);
        }
      });
  }

  cancelChanges(): void {
    // Reset to original state by reloading roles
    this.loadRoles();
  }

  getRealmRoles(): RoleDto[] {
    return this.availableRoles.filter(role => !role.clientRole);
  }

  getClientRoles(): RoleDto[] {
    return this.availableRoles.filter(role => role.clientRole);
  }

  isRoleAssigned(roleId: string): boolean {
    return this.assignedRoleIds.includes(roleId);
  }
}
