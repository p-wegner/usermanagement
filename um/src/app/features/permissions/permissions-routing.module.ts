import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { PermissionGuard } from '../../shared/guards/permission.guard';
import { PermissionsListComponent } from './permissions-list/permissions-list.component';
import { PermissionDetailComponent } from './permission-detail/permission-detail.component';

const routes: Routes = [
  {
    path: '',
    component: PermissionsListComponent,
    canActivate: [PermissionGuard],
    data: { requiredPermission: 'permissions.view' }
  },
  {
    path: 'new',
    component: PermissionDetailComponent,
    canActivate: [PermissionGuard],
    data: { requiredPermission: 'permissions.create' }
  },
  {
    path: ':id',
    component: PermissionDetailComponent,
    canActivate: [PermissionGuard],
    data: { requiredPermission: 'permissions.edit' }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class PermissionsRoutingModule { }
