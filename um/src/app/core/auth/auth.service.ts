import { Injectable, inject } from '@angular/core';
import { BehaviorSubject, Observable, from } from 'rxjs';
import { KeycloakService } from 'keycloak-angular';
import Keycloak, { KeycloakProfile, KeycloakTokenParsed } from 'keycloak-js';
import { Router } from '@angular/router';
import { KEYCLOAK_EVENT_SIGNAL } from 'keycloak-angular';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private userProfileSubject = new BehaviorSubject<KeycloakProfile | null>(null);
  private savedUrl: string | null = null;
  constructor(
    private keycloak: Keycloak,
    private router: Router,
  ) {
    this.init();
  }

  private async init() {
    try {
      if (this.keycloak.authenticated) {
        const profile = await this.keycloak.loadUserProfile();
        this.userProfileSubject.next(profile);
      }
    } catch (error) {
      console.error('Failed to initialize auth service:', error);
    }
  }

  isAuthenticated(): Observable<boolean> {
    return from(Promise.resolve(this.keycloak.authenticated || false));
  }

  getAccessToken(){
    return this.keycloak.token || ''
  };

  getUserProfile(): Observable<KeycloakProfile | null> {
    return this.userProfileSubject.asObservable();
  }

  getTokenParsed(): KeycloakTokenParsed | undefined {
    return this.keycloak.tokenParsed;
  }

  async login(redirectUri?: string): Promise<void> {
    try {
      // Save current URL if no redirect URI is provided
      if (!redirectUri) {
        const currentUrl = window.location.pathname + window.location.search;
        this.saveUrl(currentUrl);
      }

      await this.keycloak.login({
        redirectUri: redirectUri || window.location.origin,
        scope: 'openid profile email'
      });
    } catch (error) {
      console.error('Login failed:', error);
      throw error;
    }
  }

  async logout(redirectUri?: string): Promise<void> {
    try {
      await this.keycloak.logout({redirectUri:redirectUri || window.location.origin});
      this.userProfileSubject.next(null);
    } catch (error) {
      console.error('Logout failed:', error);
      throw error;
    }
  }

  hasRole(role: string): boolean {
    return true;
    // return this.keycloak.isUserInRole(role);
  }


  getRoles(): string[] {
    return []
    // return this.keycloak.getKeycloakInstance().realmAccess?.roles || [];
  }

  getUsername(): string {
    return ''
    // return this.keycloak.getKeycloakInstance().tokenParsed?.['preferred_username'] || '';
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

}
