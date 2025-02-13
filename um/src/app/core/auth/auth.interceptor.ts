import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, from, throwError } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';
import { KeycloakService } from 'keycloak-angular';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private keycloak: KeycloakService) {}

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // Skip authentication for public endpoints
    if (request.url.includes('/assets/') || 
        request.url.includes('/public/') ||
        request.url.includes('/api/public/')) {
      return next.handle(request);
    }

    return from(this.keycloak.getToken()).pipe(
      switchMap(token => {
        if (!token) {
          return throwError(() => new Error('No token available'));
        }

        const authRequest = request.clone({
          setHeaders: {
            Authorization: `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        });
        
        return next.handle(authRequest);
      }),
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401) {
          return from(this.keycloak.updateToken(20)).pipe(
            switchMap(() => {
              return from(this.keycloak.getToken()).pipe(
                switchMap(newToken => {
                  const retryRequest = request.clone({
                    setHeaders: {
                      Authorization: `Bearer ${newToken}`,
                      'Content-Type': 'application/json'
                    }
                  });
                  return next.handle(retryRequest);
                })
              );
            }),
            catchError(() => {
              this.keycloak.login();
              return throwError(() => error);
            })
          );
        }
        return throwError(() => error);
      })
    );
  }
}
