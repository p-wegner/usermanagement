import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {MatDialog} from '@angular/material/dialog';
import {GroupsService} from '../groups.service';
import {
  ConfirmationDialogComponent
} from '../../../shared/components/confirmation-dialog/confirmation-dialog.component';
import {ErrorHandlingService} from '../../../shared/services/error-handling.service';
import {Group} from '../../../shared/interfaces/group.interface';
import {ListColumn, ListComponent} from '../../../shared/components/list/list.component';
import {MatIcon} from '@angular/material/icon';
import {MatFabButton} from '@angular/material/button';
import {Observable} from 'rxjs';
import {AsyncPipe} from '@angular/common';

@Component({
  selector: 'app-groups-list',
  templateUrl: './groups-list.component.html',
  styleUrls: ['./groups-list.component.css'],
  imports: [ListComponent, MatIcon, MatFabButton, AsyncPipe],
  standalone: true
})
export class GroupsListComponent implements OnInit {
  groups$: Observable<Group[]>;
  loading$: Observable<boolean>;

  columns: ListColumn[] = [
    {key: 'name', label: 'Name'},
    {key: 'description', label: 'Description'}
  ];

  constructor(
    private router: Router,
    private groupsService: GroupsService,
    private dialog: MatDialog,
    private errorHandling: ErrorHandlingService
  ) {
    this.groups$ = this.groupsService.groups$;
    this.loading$ = this.groupsService.loading$;
  }

  ngOnInit(): void {
    this.groupsService.loadGroups();
  }

  onAddGroup(): void {
    this.router.navigate(['/groups/new']);
  }

  onEditGroup(id: string): void {
    this.router.navigate(['/groups', id]);
  }

  onDeleteGroup(id: string): void {
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      data: {
        title: 'Delete Group',
        message: 'Are you sure you want to delete this group?',
        confirmText: 'Delete',
        cancelText: 'Cancel'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.groupsService.deleteGroup(id).subscribe({
          error: (error) => this.errorHandling.handleError(error)
        });
      }
    });
  }
}
