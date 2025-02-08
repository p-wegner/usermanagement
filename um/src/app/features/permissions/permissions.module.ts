import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatTableModule } from '@angular/material/table';
import { MatListModule } from '@angular/material/list';

import { PermissionsRoutingModule } from './permissions-routing.module';
import {PermissionInheritanceComponent} from './permission-inheritance/permission-inheritance.component';
import {PermissionDetailComponent} from './permission-detail/permission-detail.component';

@NgModule({
  declarations: [PermissionInheritanceComponent,PermissionDetailComponent],
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatTableModule,
    MatListModule,
    PermissionsRoutingModule,
  ]
})
export class PermissionsModule { }
