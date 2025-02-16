import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'users',
    loadChildren: () => import('./features/users/users.module').then(m => m.UsersModule)
  },
  {
    path: 'groups',
    loadChildren: () => import('./features/groups/groups.module').then(m => m.GroupsModule)
  },
  {
    path: 'permissions',
    loadChildren: () => import('./features/permissions/permissions.module').then(m => m.PermissionsModule)
  },
  {
    path: 'silent-check-sso.html',
    loadComponent: () => import('./core/auth/silent-check-sso.component').then(m => m.SilentCheckSsoComponent)
  },
  {
    path: '',
    redirectTo: 'users',
    pathMatch: 'full'
  }
];
