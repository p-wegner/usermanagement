import { Injectable } from '@angular/core';
import {Observable, map, of} from 'rxjs';
import { Permission, PermissionGroup } from '../../shared/interfaces/permission.interface';
import { GroupControllerService } from '../../api/com/example/api/groupController.service';
import { GroupCreateDto } from '../../api/com/example/model/groupCreateDto';
import { GroupUpdateDto } from '../../api/com/example/model/groupUpdateDto';
import { GroupDto } from '../../api/com/example/model/groupDto';

@Injectable({
  providedIn: 'root'
})
export class GroupsService {
  constructor(private groupControllerService: GroupControllerService) {}

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

  // These methods still need to be implemented with actual API calls once available
  getAvailablePermissions(): Observable<Permission[]> {
    return of([
      { id: '1', name: 'user.create', description: 'Create new users', groups: [], inherited: false },
      { id: '2', name: 'user.edit', description: 'Edit existing users', groups: [], inherited: false },
      { id: '3', name: 'user.delete', description: 'Delete users', groups: [], inherited: false },
      { id: '4', name: 'group.create', description: 'Create new groups', groups: [], inherited: false },
      { id: '5', name: 'group.edit', description: 'Edit existing groups', groups: [], inherited: false },
      { id: '6', name: 'group.delete', description: 'Delete groups', groups: [], inherited: false }
    ]);
  }

  assignPermissionsToGroup(groupId: string, permissions: Permission[]): Observable<void> {
    // TODO: Implement when API is available
    return of(void 0);
  }

  getInheritedPermissions(groupId: string): Observable<Permission[]> {
    // TODO: Implement when API is available
    return of([]);
  }

  private mapToPermissionGroup(dto: GroupDto): PermissionGroup {
    return {
      id: dto.id!,
      name: dto.name,
      path: dto.path || '',
      permissions: [], // TODO: Add when API supports permissions
      subGroups: dto.subGroups.map(this.mapToPermissionGroup)
    };
  }
}
