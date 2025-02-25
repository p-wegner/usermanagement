import { Pipe, PipeTransform } from '@angular/core';
import { RoleDto } from '../../../../api/com/example/model/role-dto.model';

@Pipe({
  name: 'filterRoles',
  standalone: true
})
export class FilterRolesPipe implements PipeTransform {
  transform(roles: RoleDto[], isClientRole: boolean): RoleDto[] {
    if (!roles || !Array.isArray(roles)) {
      return [];
    }
    
    return roles.filter(role => role.clientRole === isClientRole);
  }
}
