import { Component, OnInit } from '@angular/core';
import { MatTabChangeEvent } from '@angular/material/tabs';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { User } from '../../../shared/interfaces/user.interface';
import { UsersService } from '../users.service';
import { LoadingService } from '../../../shared/services/loading.service';
import { ErrorHandlingService } from '../../../shared/services/error-handling.service';
import {map} from "rxjs";

@Component({
  selector: 'app-user-detail',
  templateUrl: './user-detail.component.html',
  styleUrls: ['./user-detail.component.css'],
  standalone: false
})
export class UserDetailComponent implements OnInit {
  userForm: FormGroup;
  isNewUser = true;
  private userId: string | null = null;
  selectedTabIndex = 0;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private usersService: UsersService,
    private loadingService: LoadingService,
    private errorHandling: ErrorHandlingService,
    private snackBar: MatSnackBar
  ) {
    this.userForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      fullName: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]]
    });
  }

  ngOnInit(): void {
    this.userId = this.route.snapshot.paramMap.get('id');
    if (this.userId) {
      this.isNewUser = false;
      this.loadUser(this.userId);
    }
  }

  private loadUser(id: string): void {
    this.loadingService.startLoading();
    // First check if the user is in the current list
    this.usersService.users$.pipe(
      map(users => users.find(u => u.id === id))
    ).subscribe({
      next: (user) => {
        if (user) {
          this.userForm.patchValue(user);
          this.loadingService.stopLoading();
        } else {
          // If not found in current list, fetch individually
          this.usersService.getUser(id).subscribe({
            next: (user) => {
              this.userForm.patchValue(user);
              this.loadingService.stopLoading();
            },
            error: (error) => {
              this.errorHandling.handleError(error);
              this.loadingService.stopLoading();
            }
          });
        }
      },
      error: (error) => {
        this.errorHandling.handleError(error);
        this.loadingService.stopLoading();
      }
    });
  }

  onSubmit(): void {
    if (this.userForm.valid) {
      this.loadingService.startLoading();
      const userData: User = this.userForm.value;

      const request = this.isNewUser ?
        this.usersService.createUser(userData) :
        this.usersService.updateUser(this.userId!, userData);

      request.subscribe({
        next: () => {
          this.snackBar.open(
            `User ${this.isNewUser ? 'created' : 'updated'} successfully`,
            'Close',
            { duration: 3000 }
          );
          this.router.navigate(['/users']);
          this.loadingService.stopLoading();
        },
        error: (error) => {
          this.errorHandling.handleError(error);
          this.loadingService.stopLoading();
        }
      });
    }
  }

  onCancel(): void {
    this.router.navigate(['/users']);
  }
}
