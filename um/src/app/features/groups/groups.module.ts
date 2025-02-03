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

import { GroupsRoutingModule } from './groups-routing.module';
import { GroupsListComponent } from './groups-list/groups-list.component';
import { GroupDetailComponent } from './group-detail/group-detail.component';
import { SharedModule } from '../../shared/shared.module';

@NgModule({
  declarations: [
    GroupsListComponent,
    GroupDetailComponent
  ],
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
    SharedModule,
    GroupsRoutingModule
  ]
})
export class GroupsModule { }
