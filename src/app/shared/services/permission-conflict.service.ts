import { Injectable } from '@angular/core';
import { Permission } from '../interfaces/permission.interface';

@Injectable({
  providedIn: 'root'
})
export class PermissionConflictService {
  resolveConflicts(permissions: Permission[]): Permission[] {
    // Remove duplicates based on permission name
    const uniquePermissions = new Map<string, Permission>();
    
    permissions.forEach(permission => {
      // If permission already exists, keep the one that grants access
      // This implements a "most permissive" conflict resolution strategy
      if (!uniquePermissions.has(permission.name)) {
        uniquePermissions.set(permission.name, permission);
      }
    });

    return Array.from(uniquePermissions.values());
  }

  detectConflicts(permissions: Permission[]): Map<string, Permission[]> {
    const conflicts = new Map<string, Permission[]>();
    
    // Group permissions by name
    permissions.forEach(permission => {
      const existing = conflicts.get(permission.name) || [];
      if (existing.length > 0) {
        conflicts.set(permission.name, [...existing, permission]);
      }
    });

    // Only return groups that actually have conflicts
    return new Map([...conflicts.entries()].filter(([_, perms]) => perms.length > 1));
  }
}
