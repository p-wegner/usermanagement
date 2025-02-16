import {Injectable} from '@angular/core';
import {Observable, map, of} from 'rxjs';
import {Permission} from '../../shared/interfaces/permission.interface';
import {RoleControllerService} from '../../api/com/example/api/roleController.service';
import {RoleCreateDto} from '../../api/com/example/model/roleCreateDto';
import {RoleUpdateDto} from '../../api/com/example/model/roleUpdateDto';
import {AuthService} from '../../core/auth/auth.service';
import {RoleDto} from '../../api/com/example';

@Injectable({
  providedIn: 'root'
})
export class PermissionsService {
  constructor(
    private roleController: RoleControllerService,
    private authService: AuthService
  ) {
  }

  getPermissions(): Observable<Permission[]> {
    return this.roleController.getRoles().pipe(
      map(response => {
        if (!response.success || !response.data) {
          throw new Error('Failed to fetch permissions');
        }
        return response.data.map((role: RoleDto) => ({
          id: role.id || '',
          name: role.name,
          description: role.description,
          composite: role.composite,
          clientRole: role.clientRole
        }));
      })
    );
  }

  getPermission(name: string): Observable<Permission | undefined> {
    return this.roleController.getRole(name).pipe(
      map(response => {
        if (!response.success || !response.data) {
          return undefined;
        }
        const role = response.data;
        return {
          id: role.id || '',
          name: role.name,
          description: role.description,
          composite: role.composite,
          clientRole: role.clientRole
        };
      })
    );
  }

  createPermission(permission: Omit<Permission, 'id'>): Observable<Permission> {
    const roleCreate: RoleCreateDto = {
      compositeRoleIds: [],
      name: permission.name,
      description: permission.description,
      composite: permission.composite
    };

    return this.roleController.createRole(roleCreate).pipe(
      map(response => {
        if (!response.success || !response.data) {
          throw new Error('Failed to create permission');
        }
        const role = response.data;
        return {
          id: role.id || '',
          name: role.name,
          description: role.description,
          composite: role.composite,
          clientRole: role.clientRole
        };
      })
    );
  }

  updatePermission(name: string, permission: Partial<Permission>): Observable<Permission> {
    const roleUpdate: RoleUpdateDto = {
      name: permission.name,
      description: permission.description,
      composite: permission.composite
    };

    return this.roleController.updateRole(name, roleUpdate).pipe(
      map(response => {
        if (!response.success || !response.data) {
          throw new Error('Failed to update permission');
        }
        const role = response.data;
        return {
          id: role.id || '',
          name: role.name,
          description: role.description,
          composite: role.composite,
          clientRole: role.clientRole
        };
      })
    );
  }

  deletePermission(name: string): Observable<void> {
    return this.roleController.deleteRole(name).pipe(
      map(response => {
        if (!response.success) {
          throw new Error('Failed to delete permission');
        }
      })
    );
  }

  hasPermission(permissionName: string): Observable<boolean> {
    return of(this.authService.hasRole(permissionName));
  }
}
