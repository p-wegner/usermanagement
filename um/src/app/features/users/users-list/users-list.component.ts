import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { User } from '../../../shared/interfaces/user.interface';

@Component({
  selector: 'app-users-list',
  templateUrl: './users-list.component.html',
  styleUrls: ['./users-list.component.css']
})
export class UsersListComponent {
  users: User[] = [];

  constructor(private router: Router) {}

  onAddUser(): void {
    this.router.navigate(['/users/new']);
  }

  onEditUser(id: string): void {
    this.router.navigate(['/users', id]);
  }

  onDeleteUser(id: string): void {
    // TODO: Implement delete functionality
  }
}
