import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable, map, throwError, switchMap} from 'rxjs';
import {ApiResponseService} from '../../shared/services/api-response.service';
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
    private roleControllerService: RoleControllerService,
    private apiResponseService: ApiResponseService
  ) {
    this.loadInitialGroups();
  }

  private loadInitialGroups(): void {
    this.loadGroups();
  }

  private async blobToJson(blob: Blob): Promise<any> {
    const text = await blob.text();
    return JSON.parse(text);
  }
  loadGroups(page: number = 0, size: number = 20, search?: string): void {
    this.loadingSubject.next(true);
    this.apiResponseService.handleListResponse(
      this.groupControllerService.getGroups(page, size, search),
      this.mapToPermissionGroup,
      'Failed to fetch groups'
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
    return this.apiResponseService.handleResponse(
      this.groupControllerService.getGroup(id),
      this.mapToPermissionGroup,
      'Failed to fetch group'
    );
  }

  createGroup(group: Omit<PermissionGroup, 'id'>): Observable<PermissionGroup> {
    const dto: GroupCreateDto = {
      realmRoles: [],
      name: group.name,
      parentGroupId: group.parentGroupId
    };

    return this.apiResponseService.handleResponse(
      this.groupControllerService.createGroup(dto),
      this.mapToPermissionGroup,
      'Failed to create group'
    ).pipe(
      map(newGroup => {
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

    return this.apiResponseService.handleResponse(
      this.groupControllerService.updateGroup(id, dto),
      this.mapToPermissionGroup,
      'Failed to update group'
    );
  }

  deleteGroup(id: string): Observable<void> {
    return this.apiResponseService.handleResponse(
      this.groupControllerService.deleteGroup(id),
      () => undefined,
      'Failed to delete group'
    ).pipe(
      map(() => {
        const currentGroups = this.groupsSubject.value;
        this.groupsSubject.next(currentGroups.filter(group => group.id !== id));
      })
    );
  }

  getAvailablePermissions(): Observable<Permission[]> {
    return this.roleControllerService.getRoles().pipe(
      switchMap(async (response: any) => {
        // TODO pieed 2025-02-20: fix this
        const jsonResponse = await this.blobToJson(response);
        if (!jsonResponse.success || !jsonResponse.data) {
          throw new Error(jsonResponse.error || 'Failed to fetch roles');
        }
        return jsonResponse.data.map((role: RoleDto) => ({
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
      subGroups: dto.subGroups.map(it=>this.mapToPermissionGroup(it))
    };
  }
}
