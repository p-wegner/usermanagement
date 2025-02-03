import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Group } from '../../../shared/interfaces/group.interface';
import { GroupsService } from '../groups.service';
import { LoadingService } from '../../../shared/services/loading.service';
import { ErrorHandlingService } from '../../../shared/services/error-handling.service';

@Component({
  selector: 'app-group-detail',
  templateUrl: './group-detail.component.html',
  styleUrls: ['./group-detail.component.css']
})
export class GroupDetailComponent implements OnInit {
  groupForm: FormGroup;
  isNewGroup = true;
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
      description: ['']
    });
  }

  ngOnInit(): void {
    this.groupId = this.route.snapshot.paramMap.get('id');
    if (this.groupId) {
      this.isNewGroup = false;
      this.loadGroup(this.groupId);
    }
  }

  private loadGroup(id: string): void {
    this.loadingService.show();
    this.groupsService.getGroup(id).subscribe({
      next: (group) => {
        if (group) {
          this.groupForm.patchValue(group);
        }
        this.loadingService.hide();
      },
      error: (error) => {
        this.errorHandling.handleError(error);
        this.loadingService.hide();
      }
    });
  }

  onSubmit(): void {
    if (this.groupForm.valid) {
      this.loadingService.show();
      const groupData: Group = this.groupForm.value;
      
      const request = this.isNewGroup ? 
        this.groupsService.createGroup(groupData) :
        this.groupsService.updateGroup(this.groupId!, groupData);

      request.subscribe({
        next: () => {
          this.snackBar.open(
            `Group ${this.isNewGroup ? 'created' : 'updated'} successfully`,
            'Close',
            { duration: 3000 }
          );
          this.router.navigate(['/groups']);
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
    this.router.navigate(['/groups']);
  }
}
