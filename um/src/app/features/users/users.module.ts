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

import { UsersRoutingModule } from './users-routing.module';
import {UserDetailComponent} from './user-detail/user-detail.component';
import {UsersListComponent} from './users-list/users-list.component';
import {ListComponent} from '../../shared/components/list/list.component';

@NgModule({
  declarations: [UserDetailComponent,UsersListComponent],
  imports: [
    ListComponent,
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
    UsersRoutingModule
  ]
})
export class UsersModule { }
