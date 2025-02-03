import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { Group } from '../../../shared/interfaces/group.interface';
import { GroupsService } from '../groups.service';
import { ConfirmationDialogComponent } from '../../../shared/components/confirmation-dialog/confirmation-dialog.component';
import { LoadingService } from '../../../shared/services/loading.service';
import { ErrorHandlingService } from '../../../shared/services/error-handling.service';
import { ListColumn } from '../../../shared/components/list/list.component';

@Component({
  selector: 'app-groups-list',
  templateUrl: './groups-list.component.html',
  styleUrls: ['./groups-list.component.css']
})
export class GroupsListComponent implements OnInit {
  groups: Group[] = [];
  loading = false;
  
  columns: ListColumn[] = [
    { key: 'name', label: 'Name' },
    { key: 'description', label: 'Description' }
  ];

  constructor(
    private router: Router,
    private groupsService: GroupsService,
    private dialog: MatDialog,
    private loadingService: LoadingService,
    private errorHandling: ErrorHandlingService
  ) {}

  ngOnInit(): void {
    this.loadGroups();
  }

  private loadGroups(): void {
    this.loading = true;
    this.groupsService.getGroups().subscribe({
      next: (groups) => {
        this.groups = groups;
        this.loading = false;
      },
      error: (error) => {
        this.errorHandling.handleError(error);
        this.loading = false;
      }
    });
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
        this.loading = true;
        this.groupsService.deleteGroup(id).subscribe({
          next: () => {
            this.loadGroups();
          },
          error: (error) => {
            this.errorHandling.handleError(error);
            this.loading = false;
          }
        });
      }
    });
  }
}
