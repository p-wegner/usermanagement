import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Permission } from '../../../shared/interfaces/permission.interface';
import { PermissionsService } from '../permissions.service';
import { LoadingService } from '../../../shared/services/loading.service';
import { ErrorHandlingService } from '../../../shared/services/error-handling.service';

@Component({
  selector: 'app-permission-detail',
  templateUrl: './permission-detail.component.html',
  styleUrls: ['./permission-detail.component.css']
})
export class PermissionDetailComponent implements OnInit {
  permissionForm: FormGroup;
  isNewPermission = true;
  private permissionId: string | null = null;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private permissionsService: PermissionsService,
    private loadingService: LoadingService,
    private errorHandling: ErrorHandlingService,
    private snackBar: MatSnackBar
  ) {
    this.permissionForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3)]],
      description: ['']
    });
  }

  ngOnInit(): void {
    this.permissionId = this.route.snapshot.paramMap.get('id');
    if (this.permissionId) {
      this.isNewPermission = false;
      this.loadPermission(this.permissionId);
    }
  }

  private loadPermission(id: string): void {
    this.loadingService.startLoading();
    this.permissionsService.getPermission(id).subscribe({
      next: (permission) => {
        if (permission) {
          this.permissionForm.patchValue(permission);
        }
        this.loadingService.stopLoading();
      },
      error: (error) => {
        this.errorHandling.handleError(error);
        this.loadingService.hide();
      }
    });
  }

  onSubmit(): void {
    if (this.permissionForm.valid) {
      this.loadingService.show();
      const permissionData: Permission = this.permissionForm.value;
      
      const request = this.isNewPermission ? 
        this.permissionsService.createPermission(permissionData) :
        this.permissionsService.updatePermission(this.permissionId!, permissionData);

      request.subscribe({
        next: () => {
          this.snackBar.open(
            `Permission ${this.isNewPermission ? 'created' : 'updated'} successfully`,
            'Close',
            { duration: 3000 }
          );
          this.router.navigate(['/permissions']);
          this.loadingService.hide();
        },
        error: (error) => {
          this.errorHandling.handleError(error);
          this.loadingService.hide();
        }
      });
    }
  }

  onCancel(): void {
    this.router.navigate(['/permissions']);
  }
}
