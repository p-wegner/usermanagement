import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { AppComponent } from './app/app.component';

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
      providers: appConfig.providers.map(provider => {
        if (typeof provider === 'object' && 'provide' in provider && provider.provide.toString().includes('KeycloakOptions')) {
          return {
            ...provider,
            useValue: { config: keycloakConfig }
          };
        }
        return provider;
      })
    };

    await bootstrapApplication(AppComponent, finalConfig);
  } catch (err) {
    console.error('Failed to initialize application:', err);
  }
}

initializeApp();
