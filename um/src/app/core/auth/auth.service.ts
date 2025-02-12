import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';

export interface AuthToken {
  access_token: string;
  refresh_token: string;
  expires_in: number;
  refresh_expires_in: number;
  token_type: string;
}

export interface AuthUser {
  id: string;
  username: string;
  email: string;
  firstName?: string;
  lastName?: string;
  roles: string[];
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private currentUserSubject = new BehaviorSubject<AuthUser | null>(null);
  private isAuthenticatedSubject = new BehaviorSubject<boolean>(false);
  
  constructor(
    private http: HttpClient,
    private router: Router
  ) {
    this.checkInitialAuth();
  }

  get currentUser$(): Observable<AuthUser | null> {
    return this.currentUserSubject.asObservable();
  }

  get isAuthenticated$(): Observable<boolean> {
    return this.isAuthenticatedSubject.asObservable();
  }

  private checkInitialAuth(): void {
    const token = localStorage.getItem('auth_token');
    if (token) {
      // TODO: Validate token and get user info
      this.isAuthenticatedSubject.next(true);
    }
  }

  async login(username: string, password: string): Promise<void> {
    try {
      const response = await this.http.post<AuthToken>('/api/auth/login', {
        username,
        password
      }).toPromise();

      if (response) {
        localStorage.setItem('auth_token', response.access_token);
        localStorage.setItem('refresh_token', response.refresh_token);
        this.isAuthenticatedSubject.next(true);
        await this.loadUserProfile();
      }
    } catch (error) {
      console.error('Login failed:', error);
      throw error;
    }
  }

  async logout(): Promise<void> {
    try {
      await this.http.post('/api/auth/logout', {}).toPromise();
    } catch (error) {
      console.error('Logout request failed:', error);
    } finally {
      localStorage.removeItem('auth_token');
      localStorage.removeItem('refresh_token');
      this.currentUserSubject.next(null);
      this.isAuthenticatedSubject.next(false);
      this.router.navigate(['/login']);
    }
  }

  private async loadUserProfile(): Promise<void> {
    try {
      const user = await this.http.get<AuthUser>('/api/auth/profile').toPromise();
      if (user) {
        this.currentUserSubject.next(user);
      }
    } catch (error) {
      console.error('Failed to load user profile:', error);
      throw error;
    }
  }

  async refreshToken(): Promise<void> {
    const refreshToken = localStorage.getItem('refresh_token');
    if (!refreshToken) {
      throw new Error('No refresh token available');
    }

    try {
      const response = await this.http.post<AuthToken>('/api/auth/refresh', {
        refresh_token: refreshToken
      }).toPromise();

      if (response) {
        localStorage.setItem('auth_token', response.access_token);
        localStorage.setItem('refresh_token', response.refresh_token);
      }
    } catch (error) {
      console.error('Token refresh failed:', error);
      this.logout();
      throw error;
    }
  }
}
