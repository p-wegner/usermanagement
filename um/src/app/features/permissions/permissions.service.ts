import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { Permission } from '../../shared/interfaces/permission.interface';

@Injectable({
  providedIn: 'root'
})
export class PermissionsService {
  private permissions: Permission[] = [
    // Temporary mock data
    { id: '1', name: 'user.create', description: 'Create new users' },
    { id: '2', name: 'user.edit', description: 'Edit existing users' },
    { id: '3', name: 'user.delete', description: 'Delete users' },
    { id: '4', name: 'group.create', description: 'Create new groups' },
    { id: '5', name: 'group.edit', description: 'Edit existing groups' },
    { id: '6', name: 'group.delete', description: 'Delete groups' }
  ];

  constructor(private http: HttpClient) {}

  getPermissions(): Observable<Permission[]> {
    // TODO: Replace with actual API call
    return of(this.permissions);
  }

  getPermission(id: string): Observable<Permission | undefined> {
    // TODO: Replace with actual API call
    return of(this.permissions.find(permission => permission.id === id));
  }

  createPermission(permission: Omit<Permission, 'id'>): Observable<Permission> {
    // TODO: Replace with actual API call
    const newPermission = { ...permission, id: Date.now().toString() };
    this.permissions.push(newPermission);
    return of(newPermission);
  }

  updatePermission(id: string, permission: Partial<Permission>): Observable<Permission> {
    // TODO: Replace with actual API call
    const index = this.permissions.findIndex(p => p.id === id);
    if (index >= 0) {
      this.permissions[index] = { ...this.permissions[index], ...permission };
      return of(this.permissions[index]);
    }
    throw new Error('Permission not found');
  }

  deletePermission(id: string): Observable<void> {
    // TODO: Replace with actual API call
    const index = this.permissions.findIndex(p => p.id === id);
    if (index >= 0) {
      this.permissions.splice(index, 1);
      return of(void 0);
    }
    throw new Error('Permission not found');
  }
}
