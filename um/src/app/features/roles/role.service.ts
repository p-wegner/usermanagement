import { Injectable } from '@angular/core';
import { Observable, from, switchMap } from 'rxjs';
import { RoleControllerService } from '../../api/com/example/api/role-controller.service';
import { RoleDto } from '../../api/com/example/model/role-dto.model';

interface RolesResponse {
  roles: RoleDto[];
  total: number;
}

@Injectable({
  providedIn: 'root'
})
export class RoleService {
  constructor(
    private roleControllerService: RoleControllerService
  ) {}

  getRoles(page: number = 0, size: number = 100, search?: string): Observable<RolesResponse> {
    return from(this.roleControllerService.getRoles({
      page,
      size,
      search,
      includeRealmRoles: true
    }).pipe(
      switchMap(async (response: any) => {
        const jsonResponse = await this.blobToJson(response);
        if (!jsonResponse.success || !jsonResponse.data) {
          throw new Error(jsonResponse.error || 'Failed to fetch roles');
        }
        return {
          roles: jsonResponse.data as RoleDto[],
          total: jsonResponse.data.length
        };
      })
    ));
  }

  private async blobToJson(blob: Blob): Promise<any> {
    const text = await blob.text();
    return JSON.parse(text);
  }
}
