import { Component, EventEmitter, Input, Output, ContentChild, TemplateRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatTabsModule } from '@angular/material/tabs';
import { MatIconModule } from '@angular/material/icon';

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
    MatTabsModule,
    MatIconModule
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
  @Input() showTabs = true;

  @Output() save = new EventEmitter<void>();
  @Output() delete = new EventEmitter<void>();
  @Output() cancel = new EventEmitter<void>();

  @ContentChild('basicData') basicDataTemplate?: TemplateRef<any>;
  @ContentChild('permissions') permissionsTemplate?: TemplateRef<any>;
  @ContentChild('includedIn') includedInTemplate?: TemplateRef<any>;

  onSubmit(): void {
    if (this.form.valid && !this.loading) {
      this.save.emit();
    }
  }

  onCancel(): void {
    if (!this.loading) {
      this.cancel.emit();
    }
  }

  onDelete(): void {
    if (!this.loading) {
      this.delete.emit();
    }
  }
}
