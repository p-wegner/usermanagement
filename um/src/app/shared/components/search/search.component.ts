import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-search',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatButtonModule
  ],
  template: `
    <mat-form-field appearance="outline" class="search-field">
      <mat-label>{{label}}</mat-label>
      <input matInput
             type="text"
             [(ngModel)]="searchTerm"
             (ngModelChange)="onSearchChange($event)"
             [placeholder]="placeholder">
      <button *ngIf="searchTerm"
              matSuffix
              mat-icon-button
              aria-label="Clear"
              (click)="clear()">
        <mat-icon>close</mat-icon>
      </button>
    </mat-form-field>
  `,
  styles: [`
    .search-field {
      width: 100%;
    }
  `]
})
export class SearchComponent {
  @Input() label = 'Search';
  @Input() placeholder = 'Type to search...';
  @Output() search = new EventEmitter<string>();

  searchTerm = '';

  onSearchChange(term: string) {
    this.search.emit(term);
  }

  clear() {
    this.searchTerm = '';
    this.search.emit('');
  }
}
