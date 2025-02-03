import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';

export interface SearchFilter {
  field: string;
  label: string;
}

@Component({
  selector: 'app-search',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
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
               [(ngModel)]="searchTerm"
               (ngModelChange)="onSearchChange()"
               [placeholder]="placeholder">
        <button *ngIf="searchTerm"
                matSuffix
                mat-icon-button
                aria-label="Clear"
                (click)="clearSearch()">
          <mat-icon>close</mat-icon>
        </button>
      </mat-form-field>

      <mat-form-field *ngIf="filters?.length" appearance="outline">
        <mat-label>Filter by</mat-label>
        <mat-select [(ngModel)]="selectedFilter" (selectionChange)="onFilterChange()">
          <mat-option [value]="''">All</mat-option>
          <mat-option *ngFor="let filter of filters" [value]="filter.field">
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

  searchTerm = '';
  selectedFilter = '';

  onSearchChange(): void {
    this.emitSearch();
  }

  onFilterChange(): void {
    this.emitSearch();
  }

  clearSearch(): void {
    this.searchTerm = '';
    this.emitSearch();
  }

  private emitSearch(): void {
    this.search.emit({
      term: this.searchTerm,
      filter: this.selectedFilter || undefined
    });
  }
}
