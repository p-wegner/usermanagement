import {KeycloakConfig, KeycloakInitOptions} from 'keycloak-js';
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

export interface AuthConfig {
  authServerUrl: string;
  realm: string;
  clientId: string;
  resourceServerUrl: string;
}

export interface ApiResponse<T> {
  success: boolean;
  data?: T;
  error?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthConfigService {
  private config: AuthConfig | null = null;

  constructor(private http: HttpClient) {}

  async getConfig(): Promise<KeycloakConfig> {
    if (!this.config) {
      const response = await firstValueFrom(
        this.http.get<ApiResponse<AuthConfig>>('/api/auth/config')
      );

      if (!response.success || !response.data) {
        throw new Error('Failed to load auth config');
      }

      this.config = response.data;
    }

    return {
      url: this.config.authServerUrl,
      realm: this.config.realm,
      clientId: this.config.clientId
    };
  }
}

export const keycloakInitOptions : KeycloakInitOptions = {
  onLoad: 'check-sso',
  silentCheckSsoRedirectUri: window.location.origin + '/silent-check-sso.html',
  checkLoginIframe: false,
  pkceMethod: 'S256'
};
