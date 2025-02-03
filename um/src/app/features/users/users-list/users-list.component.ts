import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { User } from '../../../shared/interfaces/user.interface';
import { UsersService } from '../users.service';
import { ConfirmationDialogComponent } from '../../../shared/components/confirmation-dialog/confirmation-dialog.component';
import { LoadingService } from '../../../shared/services/loading.service';
import { ErrorHandlingService } from '../../../shared/services/error-handling.service';
import { ListColumn } from '../../../shared/components/list/list.component';

@Component({
  selector: 'app-users-list',
  templateUrl: './users-list.component.html',
  styleUrls: ['./users-list.component.css']
})
export class UsersListComponent implements OnInit {
  users: User[] = [];
  loading = false;
  
  columns: ListColumn[] = [
    { key: 'username', label: 'Username' },
    { key: 'fullName', label: 'Full Name' },
    { key: 'email', label: 'Email' }
  ];

  constructor(
    private router: Router,
    private usersService: UsersService,
    private dialog: MatDialog,
    private loadingService: LoadingService,
    private errorHandling: ErrorHandlingService
  ) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  private loadUsers(): void {
    this.loading = true;
    this.usersService.getUsers().subscribe({
      next: (users) => {
        this.users = users;
        this.loading = false;
      },
      error: (error) => {
        this.errorHandling.handleError(error);
        this.loading = false;
      }
    });
  }

  onAddUser(): void {
    this.router.navigate(['/users/new']);
  }

  onEditUser(id: string): void {
    this.router.navigate(['/users', id]);
  }

  onDeleteUser(id: string): void {
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      data: {
        title: 'Delete User',
        message: 'Are you sure you want to delete this user?',
        confirmText: 'Delete',
        cancelText: 'Cancel'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loading = true;
        this.usersService.deleteUser(id).subscribe({
          next: () => {
            this.loadUsers();
          },
          error: (error) => {
            this.errorHandling.handleError(error);
            this.loading = false;
          }
        });
      }
    });
  }
}
