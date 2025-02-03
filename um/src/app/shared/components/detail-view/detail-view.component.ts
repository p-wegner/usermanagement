import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-detail-view',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatDividerModule,
    MatProgressBarModule,
    MatIconModule,
    MatTooltipModule
  ],
  templateUrl: './detail-view.component.html',
  styleUrl: './detail-view.component.css'
})
export class DetailViewComponent {
  private readonly errorDuration = 5000; // 5 seconds
  @Input() title = '';
  @Input() form!: FormGroup;
  @Input() submitLabel = 'Save';
  @Input() showDelete = false;
  @Input() loading = false;
  @Input() error: string | null = null;
  @Input() submitDisabled = false;
  @Input() deleteDisabled = false;
  @Input() cancelDisabled = false;
  @Input() submitTooltip = '';
  @Input() deleteTooltip = '';
  @Input() cancelTooltip = '';
  
  @Output() save = new EventEmitter<void>();
  @Output() cancel = new EventEmitter<void>();
  @Output() delete = new EventEmitter<void>();

  onSubmit() {
    if (!this.form.valid) {
      this.snackBar.open('Please fix the form errors before submitting', 'Close', {
        duration: this.errorDuration,
        panelClass: ['error-snackbar']
      });
      return;
    }
    
    if (!this.loading && !this.submitDisabled) {
      this.save.emit();
    }
  }

  onCancel() {
    if (!this.loading && !this.cancelDisabled) {
      this.cancel.emit();
    }
  }

  onDelete() {
    if (!this.loading && !this.deleteDisabled) {
      this.delete.emit();
    }
  }
}
