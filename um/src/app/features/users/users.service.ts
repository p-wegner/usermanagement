import { Injectable } from '@angular/core';
import { Observable, map } from 'rxjs';
import { User } from '../../shared/interfaces/user.interface';
import { UsersService as ApiUsersService } from '../../api/com/example/api/users.service';
import { UserCreateDto } from '../../api/com/example/model/userCreateDto';
import { UserUpdateDto } from '../../api/com/example/model/userUpdateDto';
import { UserDto } from '../../api/com/example/model/userDto';

@Injectable({
  providedIn: 'root'
})
export class UsersService {
  constructor(private apiUsersService: ApiUsersService) {}

  getUsers(page: number = 0, size: number = 20, search?: string): Observable<{ users: User[], total: number }> {
    return this.apiUsersService.getUsers(page, size, search).pipe(
      map(response => {
        if (!response.success || !response.data) {
          throw new Error(response.error || 'Failed to fetch users');
        }
        const data = response.data as { users: UserDto[], total: number };
        return {
          users: data.users.map(this.mapToUser),
          total: data.total
        };
      })
    );
  }

  getUser(id: string): Observable<User> {
    return this.apiUsersService.getUser(id).pipe(
      map(response => {
        if (!response.success || !response.data) {
          throw new Error(response.error || 'Failed to fetch user');
        }
        return this.mapToUser(response.data);
      })
    );
  }

  createUser(user: Omit<User, 'id'>): Observable<User> {
    const dto: UserCreateDto = {
      realmRoles: [],
      username: user.username,
      firstName: user.firstName,
      lastName: user.lastName,
      email: user.email,
      password: user.password!,
      enabled: true
    };

    return this.apiUsersService.createUser(dto).pipe(
      map(response => {
        if (!response.success || !response.data) {
          throw new Error(response.error || 'Failed to create user');
        }
        return this.mapToUser(response.data);
      })
    );
  }

  updateUser(id: string, user: Partial<User>): Observable<User> {
    const dto: UserUpdateDto = {
      firstName: user.firstName,
      lastName: user.lastName,
      email: user.email,
      enabled: user.enabled
    };

    return this.apiUsersService.updateUser(id, dto).pipe(
      map(response => {
        if (!response.success || !response.data) {
          throw new Error(response.error || 'Failed to update user');
        }
        return this.mapToUser(response.data);
      })
    );
  }

  deleteUser(id: string): Observable<void> {
    return this.apiUsersService.deleteUser(id).pipe(
      map(response => {
        if (!response.success) {
          throw new Error(response.error || 'Failed to delete user');
        }
      })
    );
  }

  private mapToUser(dto: UserDto): User {
    return {
      id: dto.id!,
      username: dto.username,
      firstName: dto.firstName || '',
      lastName: dto.lastName || '',
      email: dto.email,
      enabled: dto.enabled,
      fullName: `${dto.firstName || ''} ${dto.lastName || ''}`.trim()
    };
  }
}
