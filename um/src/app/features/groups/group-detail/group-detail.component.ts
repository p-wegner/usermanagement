import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MatSnackBar} from '@angular/material/snack-bar';
import {Permission} from '../../../shared/interfaces/permission.interface';
import {Group} from '../../../shared/interfaces/group.interface';
import {GroupsService} from '../groups.service';
import {LoadingService} from '../../../shared/services/loading.service';
import {ErrorHandlingService} from '../../../shared/services/error-handling.service';
import {
  MatCard,
  MatCardActions,
  MatCardContent,
  MatCardHeader,
  MatCardModule,
  MatCardTitle
} from '@angular/material/card';
import {MatError, MatFormField, MatLabel} from '@angular/material/form-field';
import {MatListOption, MatSelectionList} from '@angular/material/list';
import {MatIcon} from '@angular/material/icon';
import {NgIf} from '@angular/common';
import {MatInput} from '@angular/material/input';
import {MatTooltip} from '@angular/material/tooltip';
import {MatButton} from '@angular/material/button';

@Component({
  selector: 'app-group-detail',
  templateUrl: './group-detail.component.html',
  imports: [
    MatCardHeader,
    MatCardContent,
    MatFormField,
    ReactiveFormsModule,
    MatSelectionList,
    MatListOption,
    MatCardActions,
    MatIcon,
    MatLabel,
    MatError,
    MatCardModule,
    NgIf,
    MatInput,
    MatTooltip,
    MatButton
  ],
  styleUrls: ['./group-detail.component.css']
})
export class GroupDetailComponent implements OnInit {
  groupForm: FormGroup;
  isNewGroup = true;
  availablePermissions: Permission[] = [];
  private groupId: string | null = null;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private groupsService: GroupsService,
    private loadingService: LoadingService,
    private errorHandling: ErrorHandlingService,
    private snackBar: MatSnackBar
  ) {
    this.groupForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3)]],
      description: [''],
      permissions: [[]]
    });
  }

  ngOnInit(): void {
    this.loadAvailablePermissions();
    this.groupId = this.route.snapshot.paramMap.get('id');
    if (this.groupId) {
      this.isNewGroup = false;
      this.loadGroup(this.groupId);
      this.loadInheritedPermissions(this.groupId);
    }
  }

  private loadInheritedPermissions(groupId: string): void {
    this.loadingService.startLoading();
    this.groupsService.getInheritedPermissions(groupId).subscribe({
      next: (permissions) => {
        this.availablePermissions = this.availablePermissions.map(p => {
          const inherited = permissions.find(ip => ip.id === p.id);
          return inherited ? {...p, inherited: true} : p;
        });
        this.loadingService.stopLoading();
      },
      error: (error) => {
        this.errorHandling.handleError(error);
        this.loadingService.stopLoading();
      }
    });
  }

  private loadAvailablePermissions(): void {
    this.loadingService.startLoading();
    this.groupsService.getAvailablePermissions().subscribe({
      next: (permissions) => {
        this.availablePermissions = permissions;
        this.loadingService.stopLoading();
      },
      error: (error) => {
        this.errorHandling.handleError(error);
        this.loadingService.stopLoading();
      }
    });
  }

  private loadGroup(id: string): void {
    this.loadingService.startLoading();
    this.groupsService.getGroup(id).subscribe({
      next: (group) => {
        if (group) {
          this.groupForm.patchValue(group);
        }
        this.loadingService.stopLoading();
      },
      error: (error) => {
        this.errorHandling.handleError(error);
        this.loadingService.stopLoading();
      }
    });
  }

  onSubmit(): void {
    if (this.groupForm.valid) {
      this.loadingService.startLoading();
      const groupData: Group = this.groupForm.value;

      const request = this.isNewGroup ?
        this.groupsService.createGroup(groupData) :
        this.groupsService.updateGroup(this.groupId!, groupData);

      request.subscribe({
        next: () => {
          this.snackBar.open(
            `Group ${this.isNewGroup ? 'created' : 'updated'} successfully`,
            'Close',
            {duration: 3000}
          );
          this.router.navigate(['/groups']);
          this.loadingService.stopLoading();
        },
        error: (error) => {
          this.errorHandling.handleError(error);
          this.loadingService.stopLoading();
        }
      });
    }
  }

  onCancel(): void {
    this.router.navigate(['/groups']);
  }
}
