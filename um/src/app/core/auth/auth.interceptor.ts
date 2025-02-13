import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { KeycloakService } from 'keycloak-angular';
import { from, lastValueFrom } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';
import { Router } from '@angular/router';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const keycloak = inject(KeycloakService);
  const router = inject(Router);
  
  const publicPaths = [
    '/assets/',
    '/public/',
    '/api/public/',
    '/v3/api-docs',
    '/swagger-ui'
  ];

  if (publicPaths.some(path => req.url.includes(path))) {
    return next(req);
  }

  return from(lastValueFrom(from(keycloak.getToken()).pipe(
    switchMap(token => {
      if (!token) {
        throw new Error('No token available');
      }

      const authReq = req.clone({
        headers: req.headers
          .set('Authorization', `Bearer ${token}`)
          .set('Content-Type', 'application/json')
          .set('X-Requested-With', 'XMLHttpRequest')
      });
      
      return next(authReq);
    }),
    catchError(async error => {
      if (error?.status === 401) {
        try {
          await keycloak.updateToken();
          const newToken = await keycloak.getToken();
          const retryReq = req.clone({
            headers: req.headers
              .set('Authorization', `Bearer ${newToken}`)
              .set('Content-Type', 'application/json')
              .set('X-Requested-With', 'XMLHttpRequest')
          });
          return next(retryReq);
        } catch (refreshError) {
          console.error('Token refresh failed:', refreshError);
          await keycloak.logout();
          throw refreshError;
        }
      }
      
      if (error?.status === 403) {
        await router.navigate(['/forbidden']);
      }
      
      throw error;
    })
  )));
};
