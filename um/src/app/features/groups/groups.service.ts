import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable, map, throwError} from 'rxjs';
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
  private readonly groupsSubject = new BehaviorSubject<PermissionGroup[]>([]);
  private readonly loadingSubject = new BehaviorSubject<boolean>(false);

  readonly groups$ = this.groupsSubject.asObservable();
  readonly loading$ = this.loadingSubject.asObservable();

  constructor(
    private groupControllerService: GroupControllerService,
    private roleControllerService: RoleControllerService
  ) {
    this.loadInitialGroups();
  }

  private loadInitialGroups(): void {
    this.loadGroups();
  }

  loadGroups(page: number = 0, size: number = 20, search?: string): void {
    this.loadingSubject.next(true);
    this.groupControllerService.getGroups(page, size, search).pipe(
      map(response => {
        // TODO: here response is a Blob (bug in generator?) with type application/json, further deserialization needed
        let response1 = response as Blob;
        response1.text()
        // this has the proper data now
          .then(data => {console.log(data)})
        let s = JSON.stringify(response1);
        if (!response.success || !response.data) {
          throw new Error(response.error || 'Failed to fetch groups');
        }
        return response.data.map(this.mapToPermissionGroup);
      })
    ).subscribe({
      next: (groups) => {
        this.groupsSubject.next(groups);
        this.loadingSubject.next(false);
      },
      error: (error) => {
        console.error('Error fetching groups:', error);
        this.groupsSubject.next([]);
        this.loadingSubject.next(false);
      }
    });
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
        const newGroup = this.mapToPermissionGroup(response.data);
        const currentGroups = this.groupsSubject.value;
        this.groupsSubject.next([...currentGroups, newGroup]);
        return newGroup;
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
        const currentGroups = this.groupsSubject.value;
        this.groupsSubject.next(currentGroups.filter(group => group.id !== id));
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
