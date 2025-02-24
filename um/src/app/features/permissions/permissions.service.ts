import {Injectable} from '@angular/core';
import {Observable, map, of, switchMap} from 'rxjs';
import {Permission} from '../../shared/interfaces/permission.interface';
import {AuthService} from '../../core/auth/auth.service';
import {RoleControllerService, RoleCreateDto, RoleDto, RoleUpdateDto} from '../../api/com/example';

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
    return this.roleController.getRoles()
      .pipe(switchMap(async (response: any) => await this.blobToJson(response)))
      .pipe(
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

  getPermission(id: string): Observable<Permission | undefined> {
    return this.roleController.getRole({id})
      .pipe(switchMap(async (response: any) => await this.blobToJson(response)))
      .pipe(
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

    return this.roleController.createRole({roleCreateDto: roleCreate}).pipe(
      switchMap(async (response: any) => {
        const jsonResponse = await this.blobToJson(response);
        if (!jsonResponse.success || !jsonResponse.data) {
          throw new Error('Failed to create permission');
        }
        const role = jsonResponse.data;
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

  private async blobToJson(blob: Blob): Promise<any> {
    const text = await blob.text();
    return JSON.parse(text);
  }

  updatePermission(id: string, permission: Partial<Permission>): Observable<Permission> {
    const roleUpdate: RoleUpdateDto = {
      name: permission.name,
      description: permission.description,
      composite: permission.composite
    };

    return this.roleController.updateRole({id, roleUpdateDto: roleUpdate}).pipe(
      switchMap(async (response: any) => {
        const jsonResponse = await this.blobToJson(response);
        if (!jsonResponse.success || !jsonResponse.data) {
          throw new Error('Failed to update permission');
        }
        const role = jsonResponse.data;
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
    return this.roleController.deleteRole({id: name}).pipe(
      switchMap(async (response: any) => {
        const jsonResponse = await this.blobToJson(response);
        if (!jsonResponse.success) {
          throw new Error('Failed to delete permission');
        }
      })
    );
  }

  hasPermission(permissionName: string): Observable<boolean> {
    return of(this.authService.hasRole(permissionName));
  }
}
