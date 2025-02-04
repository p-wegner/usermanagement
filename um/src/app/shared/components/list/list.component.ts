import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatSortModule, Sort } from '@angular/material/sort';
import { SelectionModel } from '@angular/cdk/collections';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { SearchComponent } from '../search/search.component';
import {of} from 'rxjs';

export interface Column {
  key: string;
  label: string;
  sortable?: boolean;
}

@Component({
  selector: 'app-list',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatButtonModule,
    MatCheckboxModule,
    MatSortModule,
    MatProgressSpinnerModule,
    SearchComponent
  ],
  templateUrl: './list.component.html',
  styleUrl: './list.component.css'
})
export class ListComponent<T> implements OnInit {
  @Input() items: T[] = [];
  @Input() loading = false;
  @Input() columns: Column[] = [];
  @Input() totalItems = 0;
  @Input() pageSize = 10;
  @Input() pageSizeOptions = [5, 10, 25, 50];
  @Input() selectable = false;

  @Output() pageChange = new EventEmitter<PageEvent>();
  @Output() filterChange = new EventEmitter<string>();
  @Output() selectionChange = new EventEmitter<T[]>();
  @Output() rowClick = new EventEmitter<T>();
  @Output() edit = new EventEmitter<T>();
  @Output() delete = new EventEmitter<T>();
  @Output() sortChange = new EventEmitter<Sort>();

  displayedColumns: string[] = [];
  selection = new SelectionModel<T>(true, []);
  filterValue = '';

  ngOnInit() {
    const columnKeys = this.columns.map(col => col.key);
    if (this.selectable) {
      columnKeys.unshift('select');
    }
    columnKeys.push('actions');
    this.displayedColumns = columnKeys;
  }

  onFilterChange(event: Event) {
    const input = event.target as HTMLInputElement;
    this.filterValue = input.value;
    this.filterChange.emit(this.filterValue);
  }

  onPageChange(event: PageEvent) {
    this.pageChange.emit(event);
  }

  onRowClick(item: T) {
    this.rowClick.emit(item);
  }

  onSortChange(sort: Sort) {
    this.sortChange.emit(sort);
  }

  isAllSelected() {
    const numSelected = this.selection.selected.length;
    const numRows = this.items.length;
    return numSelected === numRows && numRows > 0;
  }

  toggleAllRows() {
    if (this.isAllSelected()) {
      this.selection.clear();
    } else {
      this.selection.select(...this.items);
    }
    this.selectionChange.emit(this.selection.selected);
  }

  toggleSelection(item: T) {
    this.selection.toggle(item);
    this.selectionChange.emit(this.selection.selected);
  }

  isSelected(item: T): boolean {
    return this.selection.isSelected(item);
  }

  clearFilter() {
    this.filterValue = '';
    this.filterChange.emit('');
  }

  protected readonly of = of;
}
