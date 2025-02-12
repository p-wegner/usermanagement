import { KeycloakConfig } from 'keycloak-js';

export const keycloakConfig: KeycloakConfig = {
  url: 'http://localhost:8081',
  realm: 'master',
  clientId: 'admin-cli'
};

export const keycloakInitOptions = {
  onLoad: 'check-sso',
  silentCheckSsoRedirectUri: window.location.origin + '/silent-check-sso.html',
  checkLoginIframe: false
};
