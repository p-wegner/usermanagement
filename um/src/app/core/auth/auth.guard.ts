import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { KeycloakService } from 'keycloak-angular';

export const authGuard: CanActivateFn = async (route, state) => {
  const keycloak = inject(KeycloakService);
  const router = inject(Router);

  if (!await keycloak.isLoggedIn()) {
    await keycloak.login({
      redirectUri: window.location.origin + state.url,
    });
    return false;
  }

  const requiredRoles = route.data['roles'] as Array<string>;
  if (!requiredRoles || requiredRoles.length === 0) {
    return true;
  }

  const hasRequiredRole = requiredRoles.some(role => {
    const roleMatch = keycloak.isUserInRole(role);
    if (!roleMatch) {
      console.debug(`User missing required role: ${role}`);
    }
    return roleMatch;
  });

  if (!hasRequiredRole) {
    console.warn('Access denied - missing required roles:', requiredRoles);
    await router.navigate(['/forbidden']);
    return false;
  }

  return true;
};
