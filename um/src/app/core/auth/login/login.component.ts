import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AuthService } from '../auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  template: `
    <div class="login-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title>Welcome</mat-card-title>
          <mat-card-subtitle>Please sign in to continue</mat-card-subtitle>
        </mat-card-header>
        <mat-card-content>
          <div *ngIf="loading" class="spinner-container">
            <mat-spinner diameter="50"></mat-spinner>
          </div>
          <p *ngIf="!loading" class="login-message">
            Sign in with your Keycloak account to access the application.
          </p>
        </mat-card-content>
        <mat-card-actions>
          <button 
            mat-raised-button 
            color="primary" 
            (click)="login()" 
            [disabled]="loading"
            class="login-button">
            <span *ngIf="!loading">Sign in with Keycloak</span>
            <span *ngIf="loading">Signing in...</span>
          </button>
        </mat-card-actions>
      </mat-card>
    </div>
  `,
  styles: [`
    .login-container {
      height: 100vh;
      display: flex;
      justify-content: center;
      align-items: center;
      background-color: #f5f5f5;
    }
    mat-card {
      max-width: 400px;
      width: 90%;
      padding: 20px;
      box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
    }
    .spinner-container {
      display: flex;
      justify-content: center;
      margin: 20px 0;
    }
    .login-message {
      text-align: center;
      color: rgba(0, 0, 0, 0.6);
      margin: 20px 0;
    }
    .login-button {
      width: 100%;
      padding: 8px;
      font-size: 16px;
    }
    mat-card-subtitle {
      text-align: center;
      margin-top: 8px;
    }
  `]
})
export class LoginComponent implements OnInit {
  loading = false;

  constructor(
    private authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.checkAuthentication();
  }

  private async checkAuthentication(): Promise<void> {
    try {
      const authenticated = await this.authService.isAuthenticated().toPromise();
      if (authenticated) {
        await this.router.navigate(['/']);
      }
    } catch (error) {
      console.error('Authentication check failed:', error);
      this.showError('Failed to check authentication status');
    }
  }

  async login(): Promise<void> {
    this.loading = true;
    try {
      await this.authService.login(window.location.origin);
    } catch (error) {
      console.error('Login failed:', error);
      this.showError('Login failed. Please try again.');
    } finally {
      this.loading = false;
    }
  }

  private showError(message: string): void {
    this.snackBar.open(message, 'Close', {
      duration: 5000,
      horizontalPosition: 'center',
      verticalPosition: 'bottom',
      panelClass: ['error-snackbar']
    });
  }
}
