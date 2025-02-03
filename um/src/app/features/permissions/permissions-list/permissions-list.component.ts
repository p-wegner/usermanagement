import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { Permission } from '../../../shared/interfaces/permission.interface';
import { PermissionsService } from '../permissions.service';
import { LoadingService } from '../../../shared/services/loading.service';
import { ErrorHandlingService } from '../../../shared/services/error-handling.service';
import { ListColumn } from '../../../shared/components/list/list.component';
import { ConfirmationDialogComponent } from '../../../shared/components/confirmation-dialog/confirmation-dialog.component';

@Component({
  selector: 'app-permissions-list',
  templateUrl: './permissions-list.component.html',
  styleUrls: ['./permissions-list.component.css']
})
export class PermissionsListComponent implements OnInit {
  permissions: Permission[] = [];
  loading = false;
  
  columns: ListColumn[] = [
    { key: 'name', label: 'Name' },
    { key: 'description', label: 'Description' }
  ];

  constructor(
    private permissionsService: PermissionsService,
    private router: Router,
    private dialog: MatDialog,
    private loadingService: LoadingService,
    private errorHandling: ErrorHandlingService
  ) {}

  ngOnInit(): void {
    this.loadPermissions();
  }

  private loadPermissions(): void {
    this.loadingService.show();
    this.permissionsService.getPermissions().subscribe({
      next: (permissions) => {
        this.permissions = permissions;
        this.loadingService.hide();
      },
      error: (error) => {
        this.errorHandling.handleError(error);
        this.loadingService.hide();
      }
    });
  }

  onAdd(): void {
    this.router.navigate(['/permissions/new']);
  }

  onEdit(permission: Permission): void {
    this.router.navigate(['/permissions', permission.id]);
  }

  onDelete(permission: Permission): void {
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
        this.loadingService.show();
        this.permissionsService.deletePermission(permission.id).subscribe({
          next: () => {
            this.loadPermissions();
            this.loadingService.hide();
          },
          error: (error) => {
            this.errorHandling.handleError(error);
            this.loadingService.hide();
          }
        });
      }
    });
  }
}
