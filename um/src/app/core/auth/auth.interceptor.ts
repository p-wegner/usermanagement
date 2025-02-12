import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';
import { AuthService } from './auth.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  private isRefreshing = false;

  constructor(private authService: AuthService) {}

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = localStorage.getItem('auth_token');
    
    if (token) {
      request = this.addToken(request, token);
    }

    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401 && !this.isRefreshing) {
          return this.handle401Error(request, next);
        }
        return throwError(() => error);
      })
    );
  }

  private addToken(request: HttpRequest<any>, token: string): HttpRequest<any> {
    return request.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  private handle401Error(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if (!this.isRefreshing) {
      this.isRefreshing = true;

      return new Observable(observer => {
        this.authService.refreshToken()
          .then(() => {
            this.isRefreshing = false;
            const token = localStorage.getItem('auth_token');
            if (token) {
              observer.next();
            } else {
              observer.error('No token after refresh');
            }
          })
          .catch(err => {
            this.isRefreshing = false;
            observer.error(err);
          });
      }).pipe(
        switchMap(() => {
          const token = localStorage.getItem('auth_token');
          return next.handle(this.addToken(request, token!));
        })
      );
    }
    return next.handle(request);
  }
}
