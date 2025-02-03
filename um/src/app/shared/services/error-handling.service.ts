import { Injectable } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { HttpErrorResponse } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class ErrorHandlingService {
  constructor(private snackBar: MatSnackBar) {}

  handleError(error: Error | HttpErrorResponse): void {
    let message = 'An error occurred';
    
    if (error instanceof HttpErrorResponse) {
      // Handle HTTP errors
      message = error.error?.message || error.message || message;
    } else {
      // Handle other errors
      message = error.message || message;
    }

    this.snackBar.open(message, 'Close', {
      duration: 5000,
      horizontalPosition: 'end',
      verticalPosition: 'bottom'
    });
  }
}
