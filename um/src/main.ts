import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { AppComponent } from './app/app.component';
import { provideKeycloak, withAutoRefreshToken, AutoRefreshTokenService, UserActivityService } from 'keycloak-angular';

async function fetchAuthConfig() {
  const response = await fetch('http://localhost:8080/api/auth/config');
  const result = await response.json();
  if (!result.success || !result.data) {
    throw new Error('Failed to load auth config');
  }
  return result.data;
}

function createKeycloakProvider(authConfig: any) {
  return provideKeycloak({
    config: {
      url: authConfig.authServerUrl,
      realm: authConfig.realm,
      clientId: authConfig.clientId
    },
    initOptions: {
      onLoad: 'login-required',
      silentCheckSsoRedirectUri: window.location.origin + '/assets/silent-check-sso.html',
      checkLoginIframe: false,
      pkceMethod: 'S256'
    },
    features: [
      withAutoRefreshToken({
        onInactivityTimeout: 'logout',
        sessionTimeout: 300000 // 5 minutes
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
    // You might want to show a user-friendly error message here
  }
}

initializeApp();
