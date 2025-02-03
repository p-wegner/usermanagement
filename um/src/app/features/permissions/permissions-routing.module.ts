import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { PermissionsListComponent } from './permissions-list/permissions-list.component';
import { PermissionDetailComponent } from './permission-detail/permission-detail.component';

const routes: Routes = [
  {
    path: '',
    component: PermissionsListComponent
  },
  {
    path: 'new',
    component: PermissionDetailComponent
  },
  {
    path: ':id',
    component: PermissionDetailComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class PermissionsRoutingModule { }
