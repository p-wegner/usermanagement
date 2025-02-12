import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { KeycloakService } from 'keycloak-angular';
import { KeycloakProfile } from 'keycloak-js';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private isAuthenticatedSubject = new BehaviorSubject<boolean>(false);
  private userProfileSubject = new BehaviorSubject<KeycloakProfile | null>(null);

  constructor(private keycloak: KeycloakService) {
    this.init();
  }

  private async init() {
    try {
      const authenticated = await this.keycloak.isLoggedIn();
      this.isAuthenticatedSubject.next(authenticated);
      
      if (authenticated) {
        const profile = await this.keycloak.loadUserProfile();
        this.userProfileSubject.next(profile);
      }
    } catch (error) {
      console.error('Failed to initialize auth service:', error);
    }
  }

  get isAuthenticated$(): Observable<boolean> {
    return this.isAuthenticatedSubject.asObservable();
  }

  get userProfile$(): Observable<KeycloakProfile | null> {
    return this.userProfileSubject.asObservable();
  }

  async login(): Promise<void> {
    await this.keycloak.login();
  }

  async logout(): Promise<void> {
    await this.keycloak.logout();
  }

  async getToken(): Promise<string> {
    return await this.keycloak.getToken();
  }

  hasRole(role: string): boolean {
    return this.keycloak.isUserInRole(role);
  }
}
