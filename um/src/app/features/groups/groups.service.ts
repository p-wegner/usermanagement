import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import {Permission, PermissionGroup} from '../../shared/interfaces/permission.interface';

@Injectable({
  providedIn: 'root'
})
export class GroupsService {
  private groups: PermissionGroup[] = [
    // Temporary mock data
    {
      id: '1',
      name: 'Administrators',
      description: 'Full system access',
      permissions: []
    },
    {
      id: '2',
      name: 'Users',
      description: 'Basic user permissions',
      permissions: []
    }
  ];

  constructor(private http: HttpClient) {}

  getGroups(): Observable<PermissionGroup[]> {
    // TODO: Replace with actual API call
    return of(this.groups);
  }

  getAvailablePermissions(): Observable<Permission[]> {
    // TODO: Replace with actual API call
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
    // TODO: Replace with actual API call
    const groupIndex = this.groups.findIndex(g => g.id === groupId);
    if (groupIndex >= 0) {
      this.groups[groupIndex].permissions = permissions;
      return of(void 0);
    }
    throw new Error('Group not found');
  }

  getInheritedPermissions(groupId: string): Observable<Permission[]> {
    // TODO: Replace with actual API call
    const group = this.groups.find(g => g.id === groupId);
    if (!group || !group.parentGroupId) {
      return of([]);
    }

    const parentGroup = this.groups.find(g => g.id === group.parentGroupId);
    if (!parentGroup) {
      return of([]);
    }

    return of(parentGroup.permissions.map(p => ({ ...p, inherited: true })));
  }

  getGroup(id: string): Observable<PermissionGroup | undefined> {
    // TODO: Replace with actual API call
    return of(this.groups.find(group => group.id === id));
  }

  createGroup(group: Omit<PermissionGroup, 'id'>): Observable<PermissionGroup> {
    // TODO: Replace with actual API call
    const newGroup = { ...group, id: Date.now().toString() };
    this.groups.push(newGroup);
    return of(newGroup);
  }

  updateGroup(id: string, group: Partial<PermissionGroup>): Observable<PermissionGroup> {
    // TODO: Replace with actual API call
    const index = this.groups.findIndex(g => g.id === id);
    if (index >= 0) {
      this.groups[index] = { ...this.groups[index], ...group };
      return of(this.groups[index]);
    }
    throw new Error('Group not found');
  }

  deleteGroup(id: string): Observable<void> {
    // TODO: Replace with actual API call
    const index = this.groups.findIndex(g => g.id === id);
    if (index >= 0) {
      this.groups.splice(index, 1);
      return of(void 0);
    }
    throw new Error('Group not found');
  }
}
