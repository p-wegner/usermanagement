import { Injectable, inject } from '@angular/core';
import { BehaviorSubject, Observable, from } from 'rxjs';
import { KeycloakService } from 'keycloak-angular';
import { KeycloakProfile, KeycloakTokenParsed } from 'keycloak-js';
import { Router } from '@angular/router';
import { KEYCLOAK_EVENT_SIGNAL } from 'keycloak-angular';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private userProfileSubject = new BehaviorSubject<KeycloakProfile | null>(null);
  private savedUrl: string | null = null;
  private keycloakEventSignal = inject(KEYCLOAK_EVENT_SIGNAL);

  constructor(
    private keycloak: KeycloakService,
    private router: Router,
    private http: HttpClient
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
    return from(Promise.resolve(this.keycloak.isLoggedIn()));
  }

  getUserProfile(): Observable<KeycloakProfile | null> {
    return this.userProfileSubject.asObservable();
  }

  getTokenParsed(): KeycloakTokenParsed | undefined {
    return this.keycloak.getKeycloakInstance().tokenParsed;
  }

  async login(redirectUri?: string): Promise<void> {
    try {
      const currentUrl = window.location.pathname + window.location.search;
      this.saveUrl(currentUrl);
      
      const finalRedirectUri = redirectUri || 
        (this.savedUrl ? `${window.location.origin}${this.savedUrl}` : window.location.origin);

      await this.keycloak.login({
        redirectUri: finalRedirectUri,
        scope: 'openid profile email'
      });
    } catch (error) {
      console.error('Login failed:', error);
      throw error;
    }
  }

  async logout(redirectUri?: string): Promise<void> {
    try {
      await this.keycloak.logout(redirectUri || window.location.origin);
      this.userProfileSubject.next(null);
    } catch (error) {
      console.error('Logout failed:', error);
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

  getUsername(): string {
    return this.keycloak.getKeycloakInstance().tokenParsed?.['preferred_username'] || '';
  }

  async loadUserProfile(): Promise<KeycloakProfile> {
    try {
      const profile = await this.keycloak.loadUserProfile();
      this.userProfileSubject.next(profile);
      return profile;
    } catch (error) {
      console.error('Failed to load user profile:', error);
      throw error;
    }
  }

  saveUrl(url: string): void {
    // Sanitize and validate the URL
    if (url && url.startsWith('/') && !url.includes('//')) {
      this.savedUrl = url;
      sessionStorage.setItem('redirectUrl', url);
    }
  }

  redirectToSavedUrl(): void {
    const savedUrl = this.savedUrl || sessionStorage.getItem('redirectUrl');
    if (savedUrl) {
      this.router.navigateByUrl(savedUrl);
      this.savedUrl = null;
      sessionStorage.removeItem('redirectUrl');
    } else {
      this.router.navigate(['/']);
    }
  }
}
