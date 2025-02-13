import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse,
  HttpHeaders
} from '@angular/common/http';
import { Observable, from, throwError } from 'rxjs';
import { catchError, switchMap, finalize, retryWhen, delay, take } from 'rxjs/operators';
import { KeycloakService } from 'keycloak-angular';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  private refreshTokenInProgress = false;
  private readonly publicPaths = [
    '/assets/',
    '/public/',
    '/api/public/',
    '/v3/api-docs',
    '/swagger-ui'
  ];
  private readonly maxRetries = 3;
  private readonly retryDelay = 1000;

  constructor(
    private keycloak: KeycloakService,
    private authService: AuthService,
    private router: Router
  ) {}

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if (this.isPublicPath(request.url)) {
      return next.handle(request);
    }

    return from(this.keycloak.getToken()).pipe(
      switchMap(token => {
        if (!token) {
          return throwError(() => new Error('No token available'));
        }

        const headers = new HttpHeaders()
          .set('Authorization', `Bearer ${token}`)
          .set('Content-Type', 'application/json')
          .set('X-Requested-With', 'XMLHttpRequest');

        const authRequest = request.clone({ headers });
        return next.handle(authRequest);
      }),
      retryWhen(errors => 
        errors.pipe(
          delay(this.retryDelay),
          take(this.maxRetries)
        )
      ),
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401 && !this.refreshTokenInProgress) {
          this.refreshTokenInProgress = true;
          
          return from(this.authService.refreshToken()).pipe(
            switchMap(() => {
              return from(this.keycloak.getToken()).pipe(
                switchMap(newToken => {
                  const headers = new HttpHeaders()
                    .set('Authorization', `Bearer ${newToken}`)
                    .set('Content-Type', 'application/json')
                    .set('X-Requested-With', 'XMLHttpRequest');

                  const retryRequest = request.clone({ headers });
                  return next.handle(retryRequest);
                })
              );
            }),
            catchError(refreshError => {
              console.error('Token refresh failed:', refreshError);
              this.authService.logout();
              return throwError(() => refreshError);
            }),
            finalize(() => {
              this.refreshTokenInProgress = false;
            })
          );
        }

        if (error.status === 403) {
          this.router.navigate(['/forbidden']);
        }

        return throwError(() => error);
      })
    );
  }

  private isPublicPath(url: string): boolean {
    return this.publicPaths.some(path => url.includes(path));
  }
}
