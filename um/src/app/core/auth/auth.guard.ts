import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, RouterStateSnapshot, UrlTree, Router } from '@angular/router';
import { KeycloakAuthGuard, KeycloakService } from 'keycloak-angular';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard extends KeycloakAuthGuard {
  constructor(
    protected override readonly keycloak: KeycloakService,
    private authService: AuthService,
    private router: Router
  ) {
    super(keycloak);
  }

  async isAccessAllowed(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Promise<boolean | UrlTree> {
    if (!this.authenticated) {
      await this.authService.login(window.location.origin + state.url);
      return false;
    }

    const requiredRoles = route.data['roles'];
    if (!requiredRoles || requiredRoles.length === 0) {
      return true;
    }

    if (!this.authService.hasAnyRole(requiredRoles)) {
      console.warn('User does not have required roles:', requiredRoles);
      return this.router.createUrlTree(['/unauthorized']);
    }

    return true;
  }
}
