import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '../auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatProgressSpinnerModule
  ],
  template: `
    <div class="login-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title>Login</mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <div *ngIf="loading" class="spinner-container">
            <mat-spinner diameter="50"></mat-spinner>
          </div>
        </mat-card-content>
        <mat-card-actions>
          <button mat-raised-button color="primary" (click)="login()" [disabled]="loading">
            Login with Keycloak
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
    }
    .spinner-container {
      display: flex;
      justify-content: center;
      margin: 20px 0;
    }
  `]
})
export class LoginComponent implements OnInit {
  loading = false;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.authService.isAuthenticated().subscribe(authenticated => {
      if (authenticated) {
        this.router.navigate(['/']);
      }
    });
  }

  async login(): Promise<void> {
    this.loading = true;
    try {
      await this.authService.login(window.location.origin);
    } catch (error) {
      console.error('Login failed:', error);
    } finally {
      this.loading = false;
    }
  }
}
