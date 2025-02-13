import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, RouterStateSnapshot, UrlTree, Router } from '@angular/router';
import { KeycloakAuthGuard, KeycloakService } from 'keycloak-angular';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard extends KeycloakAuthGuard {
  constructor(
    protected override readonly keycloak: KeycloakService,
    private router: Router
  ) {
    super(keycloak);
  }

  async isAccessAllowed(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Promise<boolean | UrlTree> {
    if (!this.authenticated) {
      await this.keycloak.login({
        redirectUri: window.location.origin + state.url,
        prompt: 'login'
      });
      return false;
    }

    // Check for resource-specific roles if specified
    const requiredRoles = route.data['roles'];
    if (requiredRoles && requiredRoles.length > 0) {
      const hasRole = requiredRoles.some(role => {
        const roleMatch = this.roles.includes(role);
        if (!roleMatch) {
          console.debug(`User missing required role: ${role}`);
        }
        return roleMatch;
      });

      if (!hasRole) {
        console.warn('Access denied - missing required roles:', requiredRoles);
        return this.router.createUrlTree(['/unauthorized']);
      }
    }

    return true;
  }
}
