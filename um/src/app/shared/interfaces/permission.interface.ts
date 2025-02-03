export interface Permission {
  id: string;
  name: string;
  description?: string;
}

export interface PermissionGroup {
  id: string;
  name: string;
  description?: string;
  permissions: Permission[];
}
