import {ApplicationConfig, provideZoneChangeDetection, inject} from '@angular/core';
import {provideRouter, Router, withHashLocation} from '@angular/router';
import {provideAnimations} from '@angular/platform-browser/animations';
import {MAT_FORM_FIELD_DEFAULT_OPTIONS} from '@angular/material/form-field';
import {MAT_SNACK_BAR_DEFAULT_OPTIONS} from '@angular/material/snack-bar';
import {provideHttpClient, withInterceptors} from '@angular/common/http';
import {
  includeBearerTokenInterceptor,
  INCLUDE_BEARER_TOKEN_INTERCEPTOR_CONFIG,
  provideKeycloak, withAutoRefreshToken, AutoRefreshTokenService, UserActivityService
} from 'keycloak-angular';

import {routes} from './app.routes';
import {urlCondition} from './keycloak-setup';
import {Configuration} from './api/com/example';
import {AuthService} from './core/auth/auth.service';


export function appConfig(authConfig: {
  authServerUrl: string;
  realm: string;
  clientId: string;
}): ApplicationConfig {
  return {
    providers: [
      provideZoneChangeDetection({eventCoalescing: true}),
      provideRouter(routes, withHashLocation()),
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
      },
      {
        provide: INCLUDE_BEARER_TOKEN_INTERCEPTOR_CONFIG,
        useValue: [urlCondition]
      },
      provideKeycloak({
        config: {
          url: authConfig.authServerUrl,
          realm: authConfig.realm,
          clientId: authConfig.clientId,
        },
        initOptions: {
          onLoad: 'check-sso',
          silentCheckSsoRedirectUri: window.location.origin + '/assets/silent-check-sso.html',
          redirectUri: window.location.origin + window.location.pathname,
          checkLoginIframe: false,
          enableLogging: true
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
      }),
      {
        provide: Configuration,
        useFactory: (authService: AuthService) => new Configuration(
          {
            basePath: 'http://localhost:8080',
            // basePath: environment.apiUrl,
            accessToken: () => authService.getAccessToken()
          }
        ),
        deps: [AuthService],
        multi: false
      },
      provideHttpClient(withInterceptors([includeBearerTokenInterceptor]))
    ]

  };
}
