import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, from } from 'rxjs';
import { KeycloakService } from 'keycloak-angular';
import { KeycloakProfile } from 'keycloak-js';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private userProfileSubject = new BehaviorSubject<KeycloakProfile | null>(null);
  private savedUrl: string | null = null;

  constructor(
    private keycloak: KeycloakService,
    private router: Router
  ) {
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

  async login(redirectUri?: string, options: { prompt?: string } = {}): Promise<void> {
    this.saveUrl(window.location.pathname);
    await this.keycloak.login({
      redirectUri: redirectUri || window.location.origin,
      prompt: options.prompt
    });
  }

  async logout(redirectUri?: string): Promise<void> {
    try {
      await this.keycloak.logout(redirectUri || window.location.origin);
      this.userProfileSubject.next(null);
      localStorage.removeItem('user_profile');
      sessionStorage.clear();
    } catch (error) {
      console.error('Logout failed:', error);
      throw error;
    }
  }

  async refreshToken(minValidity: number = 20): Promise<boolean> {
    try {
      return await this.keycloak.updateToken(minValidity);
    } catch (error) {
      console.error('Token refresh failed:', error);
      await this.login();
      return false;
    }
  }

  async checkAuthentication(): Promise<boolean> {
    try {
      const isLoggedIn = await this.keycloak.isLoggedIn();
      if (isLoggedIn) {
        const profile = await this.keycloak.loadUserProfile();
        this.userProfileSubject.next(profile);
        localStorage.setItem('user_profile', JSON.stringify(profile));
      }
      return isLoggedIn;
    } catch (error) {
      console.error('Authentication check failed:', error);
      return false;
    }
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

  saveUrl(url: string): void {
    this.savedUrl = url;
  }

  redirectToSavedUrl(): void {
    if (this.savedUrl) {
      this.router.navigateByUrl(this.savedUrl);
      this.savedUrl = null;
    } else {
      this.router.navigate(['/']);
    }
  }
}
