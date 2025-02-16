import {Injectable} from '@angular/core';
import {Observable, map, of, throwError} from 'rxjs';
import {Permission, PermissionGroup} from '../../shared/interfaces/permission.interface';
import {GroupControllerService} from '../../api/com/example/api/groupController.service';
import {RoleControllerService} from '../../api/com/example/api/roleController.service';
import {GroupCreateDto} from '../../api/com/example/model/groupCreateDto';
import {GroupUpdateDto} from '../../api/com/example/model/groupUpdateDto';
import {GroupDto} from '../../api/com/example/model/groupDto';
import {RoleDto} from '../../api/com/example';

@Injectable({
  providedIn: 'root'
})
export class GroupsService {
  constructor(
    private groupControllerService: GroupControllerService,
    private roleControllerService: RoleControllerService
  ) {
  }

  getGroups(page: number = 0, size: number = 20, search?: string): Observable<PermissionGroup[]> {
    return this.groupControllerService.getGroups(page, size, search).pipe(
      map(response => {
        if (!response.success || !response.data) {
          throw new Error(response.error || 'Failed to fetch groups');
        }
        return response.data.map(this.mapToPermissionGroup);
      })
    );
  }

  getGroup(id: string): Observable<PermissionGroup> {
    return this.groupControllerService.getGroup(id).pipe(
      map(response => {
        if (!response.success || !response.data) {
          throw new Error(response.error || 'Failed to fetch group');
        }
        return this.mapToPermissionGroup(response.data);
      })
    );
  }

  createGroup(group: Omit<PermissionGroup, 'id'>): Observable<PermissionGroup> {
    const dto: GroupCreateDto = {
      realmRoles: [],
      name: group.name,
      parentGroupId: group.parentGroupId
    };

    return this.groupControllerService.createGroup(dto).pipe(
      map(response => {
        if (!response.success || !response.data) {
          throw new Error(response.error || 'Failed to create group');
        }
        return this.mapToPermissionGroup(response.data);
      })
    );
  }

  updateGroup(id: string, group: Partial<PermissionGroup>): Observable<PermissionGroup> {
    const dto: GroupUpdateDto = {
      name: group.name
    };

    return this.groupControllerService.updateGroup(id, dto).pipe(
      map(response => {
        if (!response.success || !response.data) {
          throw new Error(response.error || 'Failed to update group');
        }
        return this.mapToPermissionGroup(response.data);
      })
    );
  }

  deleteGroup(id: string): Observable<void> {
    return this.groupControllerService.deleteGroup(id).pipe(
      map(response => {
        if (!response.success) {
          throw new Error(response.error || 'Failed to delete group');
        }
      })
    );
  }

  getAvailablePermissions(): Observable<Permission[]> {
    return this.roleControllerService.getRoles().pipe(
      map(response => {
        if (!response.success || !response.data) {
          throw new Error(response.error || 'Failed to fetch roles');
        }
        return response.data.map((role: RoleDto) => ({
          id: role.id || '',
          name: role.name,
          description: role.description || '',
          composite: role.composite,
          clientRole: role.clientRole
        }));
      })
    );
  }

  assignPermissionsToGroup(groupId: string, permissions: Permission[]): Observable<void> {
    // Note: This would need a backend API endpoint to assign roles to groups
    // For now, we'll throw an error
    return throwError(() => new Error('API endpoint for assigning permissions to groups is not yet available'));
  }

  getInheritedPermissions(groupId: string): Observable<Permission[]> {
    return this.getGroup(groupId).pipe(
      map(group => group.permissions.filter(p => p.composite))
    );
  }

  private mapToPermissionGroup(dto: GroupDto): PermissionGroup {
    return {
      id: dto.id!,
      name: dto.name,
      path: dto.path || '',
      permissions: dto.realmRoles?.map(role => ({
        id: role.id,
        name: role.name,
        description: role.description || '',
        composite: role.composite,
        clientRole: role.clientRole
      })) || [],
      subGroups: dto.subGroups.map(this.mapToPermissionGroup)
    };
  }
}
