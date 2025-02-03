import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

@Component({
  selector: 'app-detail-view',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule
  ],
  templateUrl: './detail-view.component.html',
  styleUrls: ['./detail-view.component.css']
})
export class DetailViewComponent {
  @Input() title = '';
  @Input() form!: FormGroup;
  @Input() loading = false;
  @Input() submitLabel = 'Save';
  @Input() cancelLabel = 'Cancel';
  @Input() showDelete = false;
  @Input() deleteLabel = 'Delete';

  @Output() formSubmit = new EventEmitter<void>();
  @Output() formCancel = new EventEmitter<void>();
  @Output() formDelete = new EventEmitter<void>();

  onSubmit(): void {
    if (this.form.valid) {
      this.formSubmit.emit();
    }
  }

  onCancel(): void {
    this.formCancel.emit();
  }

  onDelete(): void {
    this.formDelete.emit();
  }
}
