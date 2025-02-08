import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { User } from '../../shared/interfaces/user.interface';

@Injectable({
  providedIn: 'root'
})
export class UsersService {
  private users: User[] = [
    // Temporary mock data
    { id: '1', username: 'john.doe', fullName: 'John Doe', email: 'john@example.com' },
    { id: '2', username: 'jane.smith', fullName: 'Jane Smith', email: 'jane@example.com' }
  ];

  constructor(private http: HttpClient) {}

  getUsers(): Observable<User[]> {
    // TODO: Replace with actual API call
    return of(this.users);
  }

  getUser(id: string): Observable<User> {
    // TODO: Replace with actual API call
    return of(this.users.find(user => user.id === id)!);
  }

  createUser(user: Omit<User, 'id'>): Observable<User> {
    // TODO: Replace with actual API call
    const newUser = { ...user, id: Date.now().toString() };
    this.users.push(newUser);
    return of(newUser);
  }

  updateUser(id: string, user: Partial<User>): Observable<User> {
    // TODO: Replace with actual API call
    const index = this.users.findIndex(u => u.id === id);
    if (index >= 0) {
      this.users[index] = { ...this.users[index], ...user };
      return of(this.users[index]);
    }
    throw new Error('User not found');
  }

  deleteUser(id: string): Observable<void> {
    // TODO: Replace with actual API call
    const index = this.users.findIndex(u => u.id === id);
    if (index >= 0) {
      this.users.splice(index, 1);
      return of(void 0);
    }
    throw new Error('User not found');
  }
}
