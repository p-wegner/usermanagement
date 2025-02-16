import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { AppComponent } from './app/app.component';
import { provideKeycloak, withAutoRefreshToken } from 'keycloak-angular';
import { firstValueFrom } from 'rxjs';
import { HttpClient } from '@angular/common/http';

async function fetchAuthConfig(http: HttpClient) {
  try {
    const result = await firstValueFrom(http.get('http://localhost:8080/api/auth/config'));
    if (!result || !result.data) {
      throw new Error('Failed to load auth config');
    }
    return result.data;
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
      credentials: {
        secret: authConfig.clientSecret
      }
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
    ]
  });
}

async function initializeApp() {
  try {
    const http = new HttpClient(null!);
    const authConfig = await fetchAuthConfig(http);
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
