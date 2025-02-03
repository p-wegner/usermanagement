import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTabsModule } from '@angular/material/tabs';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-detail-view',
  standalone: true,
  imports: [
    CommonModule,
    MatTabsModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    ReactiveFormsModule
  ],
  templateUrl: './detail-view.component.html',
  styleUrl: './detail-view.component.css'
})
export class DetailViewComponent {
  @Input() title = '';
  @Input() form!: FormGroup;
  @Input() showDelete = false;
  @Input() loading = false;
  @Input() submitLabel = 'Confirm';
  @Input() cancelLabel = 'Cancel';
  
  @Output() save = new EventEmitter<void>();
  @Output() delete = new EventEmitter<void>();
  @Output() cancel = new EventEmitter<void>();

  onSubmit() {
    if (this.form.valid && !this.loading) {
      this.save.emit();
    }
  }

  onCancel() {
    if (!this.loading) {
      this.cancel.emit();
    }
  }

  onDelete() {
    if (!this.loading) {
      this.delete.emit();
    }
  }
}
