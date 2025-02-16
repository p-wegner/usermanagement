export interface Permission {
  id: string;
  name: string;
  description?: string;
  composite: boolean;
  clientRole: boolean;
}

export interface PermissionGroup {
  id: string;
  name: string;
  path?: string;
  permissions: Permission[];
  subGroups: PermissionGroup[];
  parentGroupId?: string;
}
