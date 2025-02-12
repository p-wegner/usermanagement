import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, from, throwError } from 'rxjs';
import { catchError, switchMap, retryWhen, delay, take } from 'rxjs/operators';
import { AuthService } from './auth.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private authService: AuthService) {}

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // Skip authentication for public endpoints
    if (request.url.includes('/public/') || request.url.includes('/auth/')) {
      return next.handle(request);
    }

    return from(this.authService.getToken()).pipe(
      switchMap(token => {
        if (token) {
          request = request.clone({
            setHeaders: {
              Authorization: `Bearer ${token}`,
              'Content-Type': 'application/json'
            }
          });
        }
        return next.handle(request);
      }),
      retryWhen(errors => 
        errors.pipe(
          delay(1000),
          take(3)
        )
      ),
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401) {
          return from(this.handleUnauthorized(error));
        }
        return throwError(() => error);
      })
    );
  }

  private async handleUnauthorized(error: HttpErrorResponse): Promise<never> {
    try {
      // Try to refresh the token
      await this.authService.getToken();
    } catch (refreshError) {
      // If refresh fails, logout
      await this.authService.logout();
    }
    throw error;
  }
}
