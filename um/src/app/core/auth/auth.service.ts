import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, from } from 'rxjs';
import { KeycloakService } from 'keycloak-angular';
import { KeycloakProfile, KeycloakTokenParsed } from 'keycloak-js';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private isAuthenticatedSubject = new BehaviorSubject<boolean>(false);
  private userProfileSubject = new BehaviorSubject<KeycloakProfile | null>(null);
  private tokenSubject = new BehaviorSubject<KeycloakTokenParsed | null>(null);

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
        
        const token = this.keycloak.getKeycloakInstance().tokenParsed;
        this.tokenSubject.next(token);
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

  get token$(): Observable<KeycloakTokenParsed | null> {
    return this.tokenSubject.asObservable();
  }

  async login(redirectUri?: string): Promise<void> {
    await this.keycloak.login({
      redirectUri: redirectUri || window.location.origin
    });
  }

  async logout(redirectUri?: string): Promise<void> {
    await this.keycloak.logout(redirectUri || window.location.origin);
  }

  async getToken(): Promise<string> {
    try {
      await this.keycloak.updateToken(20);
      return await this.keycloak.getToken();
    } catch (error) {
      console.error('Error refreshing token:', error);
      await this.login();
      throw error;
    }
  }

  hasRole(role: string): boolean {
    return this.keycloak.isUserInRole(role);
  }

  hasAnyRole(roles: string[]): boolean {
    return roles.some(role => this.hasRole(role));
  }

  getRoles(): string[] {
    return this.keycloak.getKeycloakInstance().realmAccess?.roles || [];
  }

  getUsername(): string | undefined {
    return this.keycloak.getKeycloakInstance().tokenParsed?.preferred_username;
  }
}
