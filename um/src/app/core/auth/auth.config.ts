import { KeycloakConfig, KeycloakInitOptions } from 'keycloak-js';
import { Injectable } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { AuthControllerService } from '../../api/com/example/api/authController.service';
import { AuthConfigDto } from '../../api/com/example/model/authConfigDto';

@Injectable({
  providedIn: 'root'
})
export class AuthConfigService {
  private config: AuthConfigDto | null = null;

  constructor(private authController: AuthControllerService) {}

  getInitialConfig(): KeycloakConfig {
    return {
      url: '',
      realm: '',
      clientId: ''
    };
  }

  async getConfig(): Promise<KeycloakConfig> {
    if (!this.config) {
      const response = await firstValueFrom(
        this.authController.getAuthConfig()
      );

      if (!response.success || !response.data) {
        throw new Error('Failed to load auth config');
      }

      this.config = response.data;
    }

    return {
      url: this.config!.authServerUrl,
      realm: this.config!.realm,
      clientId: this.config!.clientId
    };
  }
}

export const keycloakInitOptions : KeycloakInitOptions = {
  onLoad: 'login-required',
  silentCheckSsoRedirectUri: window.location.origin + '/silent-check-sso.html',
  checkLoginIframe: false,
  pkceMethod: 'S256'
};
