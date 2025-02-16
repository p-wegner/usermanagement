import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { AppComponent } from './app/app.component';
import { provideKeycloak, withAutoRefreshToken } from 'keycloak-angular';

async function initializeApp() {
  try {
    const response = await fetch('http://localhost:8080/api/auth/config');
    const result = await response.json();
    if (!result.success || !result.data) {
      throw new Error('Failed to load auth config');
    }

    const keycloakConfig = {
      url: result.data.authServerUrl,
      realm: result.data.realm,
      clientId: result.data.clientId
    };

    const finalConfig = {
      ...appConfig,
      providers: [
        provideKeycloak({
          config: keycloakConfig,
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
          ]
        }),
        ...appConfig.providers
      ]
    };

    await bootstrapApplication(AppComponent, finalConfig);
  } catch (err) {
    console.error('Failed to initialize application:', err);
  }
}

initializeApp();
