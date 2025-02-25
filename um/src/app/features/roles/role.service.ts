import { Injectable } from '@angular/core';
import { Observable, from, switchMap, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { RoleControllerService } from '../../api/com/example/api/role-controller.service';
import { RoleDto } from '../../api/com/example/model/role-dto.model';
import { ErrorHandlingService } from '../../shared/services/error-handling.service';

interface RolesResponse {
  roles: RoleDto[];
  total: number;
}

@Injectable({
  providedIn: 'root'
})
export class RoleService {
  constructor(
    private roleControllerService: RoleControllerService,
    private errorHandling: ErrorHandlingService
  ) {}

  getRoles(page: number = 0, size: number = 100, search?: string): Observable<RolesResponse> {
    return from(this.roleControllerService.getRoles({
      page,
      size,
      search,
      includeRealmRoles: true,
      includeClientRoles: true
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
      }),
      catchError(error => {
        this.errorHandling.handleError(error);
        return of({ roles: [], total: 0 });
      })
    ));
  }

  getRole(id: string): Observable<RoleDto> {
    return from(this.roleControllerService.getRole({ id }).pipe(
      switchMap(async (response: any) => {
        const jsonResponse = await this.blobToJson(response);
        if (!jsonResponse.success || !jsonResponse.data) {
          throw new Error(jsonResponse.error || 'Failed to fetch role');
        }
        return jsonResponse.data as RoleDto;
      }),
      catchError(error => {
        this.errorHandling.handleError(error);
        throw error;
      })
    ));
  }

  private async blobToJson(blob: Blob): Promise<any> {
    const text = await blob.text();
    return JSON.parse(text);
  }
}
