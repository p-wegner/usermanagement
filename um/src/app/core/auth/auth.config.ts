import { KeycloakConfig } from 'keycloak-js';

export const defaultKeycloakConfig: KeycloakConfig = {
  url: 'http://localhost:8081/',
  realm: 'master',
  clientId: 'keycloak-wrapper-client'
};

export const keycloakInitOptions = {
  enableBearerInterceptor: true,
  loadUserProfileAtStartUp: true,
  initOptions: {
    onLoad: 'check-sso',
    silentCheckSsoRedirectUri: window.location.origin + '/assets/silent-check-sso.html',
    checkLoginIframe: false,
    pkceMethod: 'S256',
    enableLogging: true
  }
};
