import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable, from, timer} from 'rxjs';
import {KeycloakService} from 'keycloak-angular';
import {KeycloakProfile, KeycloakTokenParsed} from 'keycloak-js';
import {Router} from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private userProfileSubject = new BehaviorSubject<KeycloakProfile | null>(null);
  private savedUrl: string | null = null;
  private readonly TOKEN_MIN_VALIDITY_SECONDS = 70;
  private readonly TOKEN_CHECK_INTERVAL = 60000; // 1 minute

  constructor(
    private keycloak: KeycloakService,
    private router: Router
  ) {
    this.init();
    this.setupTokenRefresh();
  }

  private async init() {
    try {
      if (await this.keycloak.isLoggedIn()) {
        const profile = await this.keycloak.loadUserProfile();
        this.userProfileSubject.next(profile);
        localStorage.setItem('user_profile', JSON.stringify(profile));
      }
    } catch (error) {
      console.error('Failed to initialize auth service:', error);
    }
  }

  private setupTokenRefresh(): void {
    timer(0, this.TOKEN_CHECK_INTERVAL).subscribe(async () => {
      try {
        const isExpired = await this.keycloak.isTokenExpired(this.TOKEN_MIN_VALIDITY_SECONDS);
        if (isExpired) {
          const success = await this.refreshToken();
          if (success) {
            const instance = this.keycloak.getKeycloakInstance();
            sessionStorage.setItem('kc_token', instance.token || '');
            sessionStorage.setItem('kc_refreshToken', instance.refreshToken || '');
          } else {
            console.warn('Token refresh was not successful');
            await this.login();
          }
        }
      } catch (error) {
        console.error('Token refresh check failed:', error);
        await this.login();
      }
    });
  }

  isAuthenticated(): Observable<boolean> {
    return from(this.keycloak.isLoggedIn());
  }

  getUserProfile(): Observable<KeycloakProfile | null> {
    return this.userProfileSubject.asObservable();
  }

  getTokenParsed(): KeycloakTokenParsed | undefined {
    return this.keycloak.getKeycloakInstance().tokenParsed;
  }

  async login(redirectUri?: string, options: { prompt?: string } = {}): Promise<void> {
    this.saveUrl(window.location.pathname);
    await this.keycloak.login({
      redirectUri: redirectUri || window.location.origin,
      // prompt: options.prompt
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

  async refreshToken(minValidity: number = this.TOKEN_MIN_VALIDITY_SECONDS): Promise<boolean> {
    try {
      const success = await this.keycloak.updateToken(minValidity);
      if (!success) {
        console.warn('Token refresh was not successful');
      }
      return success;
    } catch (error) {
      console.error('Token refresh failed:', error);
      throw error;
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

  hasAnyRole(roles: string[]): boolean {
    return roles.some(role => this.hasRole(role));
  }

  getRoles(): string[] {
    return this.keycloak.getKeycloakInstance().realmAccess?.roles || [];
  }

  getUsername(): string | undefined {
    return this.keycloak.getKeycloakInstance().tokenParsed?.['preferred_username'] ?? '';
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
