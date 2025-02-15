import { APP_INITIALIZER, ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';
import { MAT_FORM_FIELD_DEFAULT_OPTIONS } from '@angular/material/form-field';
import { MAT_SNACK_BAR_DEFAULT_OPTIONS } from '@angular/material/snack-bar';
import {
  KeycloakService,
  provideKeycloak,
  withAutoRefreshToken,
  AutoRefreshTokenService,
  UserActivityService,
  createInterceptorCondition,
  IncludeBearerTokenCondition,
  INCLUDE_BEARER_TOKEN_INTERCEPTOR_CONFIG,
  includeBearerTokenInterceptor, KeycloakOptions
} from 'keycloak-angular';

import { routes } from './app.routes';
import { AuthConfigService, keycloakInitOptions } from './core/auth/auth.config';
import {KeycloakConfig} from 'keycloak-js';

const urlCondition = createInterceptorCondition<IncludeBearerTokenCondition>({
  urlPattern: /^(http:\/\/localhost:8080)(\/.*)?$/i,
  bearerPrefix: 'Bearer'
});

function initializeKeycloak(keycloak: KeycloakService, authConfig: AuthConfigService) {
  return async () => {
    const config = await authConfig.getConfig();
    await keycloak.init({
      config,
      initOptions: {
        ...keycloakInitOptions,
      },
    });
  };
}

let options: KeycloakOptions = {
  initOptions: keycloakInitOptions,
  configResolver: (service: AuthConfigService) => service.getConfig(),
  features: [
    withAutoRefreshToken({
      onInactivityTimeout: 'logout',
      sessionTimeout: 600000 // 10 minutes
    })
  ],
  providers: [AutoRefreshTokenService, UserActivityService]
};
export const appConfig: ApplicationConfig = {
  providers: [
    AuthConfigService,
    {
      provide: APP_INITIALIZER,
      useFactory: initializeKeycloak,
      multi: true,
      deps: [KeycloakService, AuthConfigService],
    },
    provideKeycloak(options),
    {
      provide: INCLUDE_BEARER_TOKEN_INTERCEPTOR_CONFIG,
      useValue: [urlCondition]
    },
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(withInterceptors([includeBearerTokenInterceptor])),
    provideAnimations(),
    {
      provide: MAT_FORM_FIELD_DEFAULT_OPTIONS,
      useValue: {
        appearance: 'outline',
        hideRequiredMarker: false
      }
    },
    {
      provide: MAT_SNACK_BAR_DEFAULT_OPTIONS,
      useValue: {
        duration: 5000,
        horizontalPosition: 'end',
        verticalPosition: 'bottom'
      }
    }
  ]
};
