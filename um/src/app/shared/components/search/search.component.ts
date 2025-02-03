import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';

export interface SearchFilter {
  field: string;
  label: string;
}

@Component({
  selector: 'app-search',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatButtonModule,
    MatSelectModule
  ],
  template: `
    <div class="search-container">
      <mat-form-field appearance="outline">
        <mat-label>Search</mat-label>
        <input matInput
               [formControl]="searchControl"
               [placeholder]="placeholder">
        <button @if="searchControl.value"
                matSuffix
                mat-icon-button
                aria-label="Clear"
                (click)="clearSearch()">
          <mat-icon>close</mat-icon>
        </button>
      </mat-form-field>

      <mat-form-field @if="filters?.length" appearance="outline">
        <mat-label>Filter by</mat-label>
        <mat-select [formControl]="filterControl">
          <mat-option [value]="''">All</mat-option>
          <mat-option @for="let filter of filters" [value]="filter.field">
            {{filter.label}}
          </mat-option>
        </mat-select>
      </mat-form-field>
    </div>
  `,
  styles: [`
    .search-container {
      display: flex;
      gap: 16px;
      align-items: start;
    }
    
    mat-form-field {
      flex: 1;
    }
  `]
})
export class SearchComponent {
  @Input() placeholder = 'Search...';
  @Input() filters?: SearchFilter[];
  
  @Output() search = new EventEmitter<{term: string, filter?: string}>();

  searchControl = new FormControl('');
  filterControl = new FormControl('');

  constructor() {
    // Debounce search input
    this.searchControl.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged()
    ).subscribe(() => this.emitSearch());

    // Immediate filter changes
    this.filterControl.valueChanges.subscribe(() => this.emitSearch());
  }

  clearSearch(): void {
    this.searchControl.setValue('');
  }

  private emitSearch(): void {
    this.search.emit({
      term: this.searchControl.value || '',
      filter: this.filterControl.value || undefined
    });
  }
}
