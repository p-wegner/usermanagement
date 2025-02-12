import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, from } from 'rxjs';
import { KeycloakService } from 'keycloak-angular';
import { KeycloakProfile } from 'keycloak-js';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private userProfileSubject = new BehaviorSubject<KeycloakProfile | null>(null);

  constructor(private keycloak: KeycloakService) {
    this.init();
  }

  private async init() {
    try {
      if (await this.keycloak.isLoggedIn()) {
        const profile = await this.keycloak.loadUserProfile();
        this.userProfileSubject.next(profile);
      }
    } catch (error) {
      console.error('Failed to initialize auth service:', error);
    }
  }

  isAuthenticated(): Observable<boolean> {
    return from(this.keycloak.isLoggedIn());
  }

  getUserProfile(): Observable<KeycloakProfile | null> {
    return this.userProfileSubject.asObservable();
  }

  async login(redirectUri?: string): Promise<void> {
    await this.keycloak.login({
      redirectUri: redirectUri || window.location.origin
    });
  }

  async logout(redirectUri?: string): Promise<void> {
    await this.keycloak.logout(redirectUri || window.location.origin);
    this.userProfileSubject.next(null);
  }

  hasRole(role: string): boolean {
    return this.keycloak.isUserInRole(role);
  }

  getRoles(): string[] {
    return this.keycloak.getKeycloakInstance().realmAccess?.roles || [];
  }

  getUsername(): string | undefined {
    return this.keycloak.getKeycloakInstance().tokenParsed?.preferred_username;
  }
}
