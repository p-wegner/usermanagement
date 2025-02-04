import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { PermissionsService } from '../../features/permissions/permissions.service';

@Injectable({
  providedIn: 'root'
})
export class PermissionGuard implements CanActivate {
  constructor(
    private permissionsService: PermissionsService,
    private router: Router
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean> {
    const requiredPermission = route.data['requiredPermission'];
    
    if (!requiredPermission) {
      return new Observable<boolean>(observer => observer.next(true));
    }

    return this.permissionsService.hasPermission(requiredPermission).pipe(
      map(hasPermission => {
        if (!hasPermission) {
          this.router.navigate(['/']);
        }
        return hasPermission;
      })
    );
  }
}
