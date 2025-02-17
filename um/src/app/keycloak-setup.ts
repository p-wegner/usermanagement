import {
  AutoRefreshTokenService,
  createInterceptorCondition, IncludeBearerTokenCondition,
  provideKeycloak,
  UserActivityService,
  withAutoRefreshToken
} from 'keycloak-angular';

export function createKeycloakProvider(authConfig: any) {
  return provideKeycloak({
    config: {
      url: authConfig.authServerUrl,
      realm: authConfig.realm,
      clientId: authConfig.clientId,
    },
    initOptions: {
      onLoad: 'check-sso',
      silentCheckSsoRedirectUri: window.location.origin + '/silent-check-sso.html',
      redirectUri: window.location.origin,
      checkLoginIframe: false
    },
    features: [
      withAutoRefreshToken({
        onInactivityTimeout: 'logout',
        sessionTimeout: 60000
      })
    ],
    providers: [
      AutoRefreshTokenService,
      UserActivityService
    ]
  });
}
export const urlCondition = createInterceptorCondition<IncludeBearerTokenCondition>({
  urlPattern: /^(http:\/\/localhost:8080)(\/.*)?$/i,
  bearerPrefix: 'Bearer'
});

export async function fetchAuthConfig() {
  try {
    const response = await fetch('http://localhost:8080/api/auth/config');
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    const result = await response.json();
    if (!result || !result.data) {
      throw new Error('Failed to load auth config');
    }
    return result.data as {
      authServerUrl: string;
      realm: string;
      clientId: string;
    };
  } catch (error) {
    console.error('Failed to fetch auth config:', error);
    throw error;
  }
}
