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

  detectConflicts(permissions: Permission[]): Set<string> {
    const conflicts = new Set<string>();
    const permissionMap = new Map<string, Permission[]>();

    // Group permissions by name
    permissions.forEach(permission => {
      const existing = permissionMap.get(permission.name) || [];
      existing.push(permission);
      permissionMap.set(permission.name, existing);
    });

    // Check for conflicts (multiple different permissions with same name)
    permissionMap.forEach((perms, name) => {
      if (perms.length > 1) {
        conflicts.add(name);
      }
    });

    return conflicts;
  }
}
