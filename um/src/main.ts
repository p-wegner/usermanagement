import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { AppComponent } from './app/app.component';
import {createKeycloakProvider, fetchAuthConfig} from './app/keycloak-setup';



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
