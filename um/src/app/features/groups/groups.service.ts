import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { PermissionGroup } from '../../shared/interfaces/permission.interface';

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
      { id: '1', name: 'user.create', description: 'Create new users' },
      { id: '2', name: 'user.edit', description: 'Edit existing users' },
      { id: '3', name: 'user.delete', description: 'Delete users' },
      { id: '4', name: 'group.create', description: 'Create new groups' },
      { id: '5', name: 'group.edit', description: 'Edit existing groups' },
      { id: '6', name: 'group.delete', description: 'Delete groups' }
    ]);
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
