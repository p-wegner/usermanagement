import { Injectable } from '@angular/core';
import {Observable,  from, switchMap, BehaviorSubject} from 'rxjs';
import { User } from '../../shared/interfaces/user.interface';
import { UsersService as ApiUsersService } from '../../api/com/example/api/users.service';
import {ApiResponseService} from "../../shared/services/api-response.service";
import {UserCreateDto, UserDto, UserUpdateDto} from '../../api/com/example';

@Injectable({
  providedIn: 'root'
})
export class UsersService {
  private readonly usersSubject = new BehaviorSubject<User[]>([]);
  private readonly loadingSubject = new BehaviorSubject<boolean>(false);
  private readonly totalUsersSubject = new BehaviorSubject<number>(0);

  readonly users$ = this.usersSubject.asObservable();
  readonly loading$ = this.loadingSubject.asObservable();
  readonly total$ = this.totalUsersSubject.asObservable();

  constructor(
    private apiUsersService: ApiUsersService,
    private apiResponseService: ApiResponseService
  ) {
    this.loadInitialUsers();
  }

  private loadInitialUsers(): void {
    this.loadUsers();
  }

  private async blobToJson(blob: Blob): Promise<any> {
    const text = await blob.text();
    return JSON.parse(text);
  }

  loadUsers(page: number = 0, size: number = 20, search?: string): void {
    this.loadingSubject.next(true);
    from(this.apiUsersService.getUsers({page, size, search}).pipe(
      switchMap(async (response: any) => {
        const jsonResponse = await this.blobToJson(response);
        if (!jsonResponse.success || !jsonResponse.data) {
          throw new Error(jsonResponse.error || 'Failed to fetch users');
        }
        const data = jsonResponse.data as { users: UserDto[], total: number };
        return {
          users: data.users.map(this.mapToUser),
          total: data.total
        };
      })
    )).subscribe({
      next: (data) => {
        this.usersSubject.next(data.users);
        this.totalUsersSubject.next(data.total);
        this.loadingSubject.next(false);
      },
      error: (error) => {
        console.error('Error fetching users:', error);
        this.usersSubject.next([]);
        this.totalUsersSubject.next(0);
        this.loadingSubject.next(false);
      }
    });
  }

  getUser(id: string): Observable<User> {
    return from(this.apiUsersService.getUser({id}).pipe(
      switchMap(async (response: any) => {
        const jsonResponse = await this.blobToJson(response);
        if (!jsonResponse.success || !jsonResponse.data) {
          throw new Error(jsonResponse.error || 'Failed to fetch user');
        }
        return this.mapToUser(jsonResponse.data);
      })
    ));
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

    return from(this.apiUsersService.createUser({userCreateDto:dto}).pipe(
      switchMap(async (response: any) => {
        const jsonResponse = await this.blobToJson(response);
        if (!jsonResponse.success || !jsonResponse.data) {
          throw new Error(jsonResponse.error || 'Failed to create user');
        }
        const newUser = this.mapToUser(jsonResponse.data);
        const currentUsers = this.usersSubject.value;
        this.usersSubject.next([...currentUsers, newUser]);
        return newUser;
      })
    ));
  }

  updateUser(id: string, user: Partial<User>): Observable<User> {
    const dto: UserUpdateDto = {
      firstName: user.firstName,
      lastName: user.lastName,
      email: user.email,
      enabled: user.enabled
    };

    return from(this.apiUsersService.updateUser({id, userUpdateDto:dto}).pipe(
      switchMap(async (response: any) => {
        const jsonResponse = await this.blobToJson(response);
        if (!jsonResponse.success || !jsonResponse.data) {
          throw new Error(jsonResponse.error || 'Failed to update user');
        }
        const updatedUser = this.mapToUser(jsonResponse.data);
        const currentUsers = this.usersSubject.value;
        const updatedUsers = currentUsers.map(u => u.id === id ? updatedUser : u);
        this.usersSubject.next(updatedUsers);
        return updatedUser;
      })
    ));
  }

  deleteUser(id: string): Observable<void> {
    return from(this.apiUsersService.deleteUser({id}).pipe(
      switchMap(async (response: any) => {
        const jsonResponse = await this.blobToJson(response);
        if (!jsonResponse.success) {
          throw new Error(jsonResponse.error || 'Failed to delete user');
        }
        const currentUsers = this.usersSubject.value;
        this.usersSubject.next(currentUsers.filter(user => user.id !== id));
      })
    ));
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
