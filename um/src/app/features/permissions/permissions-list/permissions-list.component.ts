import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {MatDialog} from '@angular/material/dialog';
import {Permission} from '../../../shared/interfaces/permission.interface';
import {PermissionsService} from '../permissions.service';
import {LoadingService} from '../../../shared/services/loading.service';
import {ErrorHandlingService} from '../../../shared/services/error-handling.service';
import {
  ConfirmationDialogComponent
} from '../../../shared/components/confirmation-dialog/confirmation-dialog.component';
import {ListColumn, ListComponent} from '../../../shared/components/list/list.component';
import {MatIcon} from '@angular/material/icon';

@Component({
  selector: 'app-permissions-list',
  templateUrl: './permissions-list.component.html',
  styleUrls: ['./permissions-list.component.css'],
  imports: [ListComponent, MatIcon]
})
export class PermissionsListComponent implements OnInit {
  permissions: Permission[] = [];
  loading = false;

  columns: ListColumn[] = [
    {key: 'name', label: 'Name'},
    {key: 'description', label: 'Description'}
  ];

  constructor(
    private permissionsService: PermissionsService,
    private router: Router,
    private dialog: MatDialog,
    private loadingService: LoadingService,
    private errorHandling: ErrorHandlingService
  ) {
  }

  ngOnInit(): void {
    this.loadPermissions();
  }

  private loadPermissions(): void {
    this.loadingService.startLoading();
    this.permissionsService.getPermissions().subscribe({
      next: (permissions) => {
        this.permissions = permissions;
        this.loadingService.stopLoading();
      },
      error: (error) => {
        this.errorHandling.handleError(error);
        this.loadingService.stopLoading();
      }
    });
  }

  onAdd(): void {
    this.permissionsService.hasPermission('permissions.create').subscribe(hasPermission => {
      if (hasPermission) {
        this.router.navigate(['/permissions/new']);
      } else {
        this.errorHandling.handleError(new Error('You do not have permission to create permissions'));
      }
    });
  }

  onEdit(permission: Permission): void {
    this.permissionsService.hasPermission('permissions.edit').subscribe(hasPermission => {
      if (hasPermission) {
        this.router.navigate(['/permissions', permission.id]);
      } else {
        this.errorHandling.handleError(new Error('You do not have permission to edit permissions'));
      }
    });
  }

  onDelete(permission: Permission): void {
    this.permissionsService.hasPermission('permissions.delete').subscribe(hasPermission => {
      if (!hasPermission) {
        this.errorHandling.handleError(new Error('You do not have permission to delete permissions'));
        return;
      }
      const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
        data: {
          title: 'Delete Permission',
          message: `Are you sure you want to delete the permission "${permission.name}"?`,
          confirmText: 'Delete',
          cancelText: 'Cancel'
        }
      });

      dialogRef.afterClosed().subscribe(result => {
        if (result) {
          this.loadingService.startLoading();
          this.permissionsService.deletePermission(permission.id).subscribe({
            next: () => {
              this.loadPermissions();
              this.loadingService.stopLoading();
            },
            error: (error) => {
              this.errorHandling.handleError(error);
              this.loadingService.stopLoading();
            }
          });
        }
      });
    });

  }
}
