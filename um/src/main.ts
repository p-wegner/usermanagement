import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { AppComponent } from './app/app.component';
import { provideKeycloak, withAutoRefreshToken, AutoRefreshTokenService, UserActivityService } from 'keycloak-angular';
async function fetchAuthConfig() {
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

function createKeycloakProvider(authConfig: any) {
  return provideKeycloak({
    config: {
      url: authConfig.authServerUrl,
      realm: authConfig.realm,
      clientId: authConfig.clientId,
    },
    initOptions: {
      onLoad: 'check-sso',
      silentCheckSsoRedirectUri: window.location.origin + '/assets/silent-check-sso.html',
      checkLoginIframe: true,
      pkceMethod: 'S256',
      flow: 'standard'
    },
    features: [
      withAutoRefreshToken({
        onInactivityTimeout: 'logout',
        sessionTimeout: 300000
      })
    ],
    providers: [AutoRefreshTokenService, UserActivityService]
  });
}

async function initializeApp() {
  try {
    const authConfig = await fetchAuthConfig();
    const keycloakProvider = createKeycloakProvider(authConfig);

    const finalConfig = {
      ...appConfig,
      providers: [
        keycloakProvider,
        ...appConfig.providers
      ]
    };

    await bootstrapApplication(AppComponent, finalConfig);
  } catch (err) {
    console.error('Failed to initialize application:', err);
    document.body.innerHTML = '<div style="color: red; padding: 20px;">Failed to initialize application. Please try again later.</div>';
  }
}

initializeApp();
