import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';

export interface Column {
  key: string;
  label: string;
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
    MatCheckboxModule
  ],
  templateUrl: './list.component.html',
  styleUrl: './list.component.css'
})
export class ListComponent<T> implements OnInit {
  @Input() items: T[] = [];
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

  displayedColumns: string[] = [];
  selectedItems: T[] = [];
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

  toggleSelection(item: T) {
    const index = this.selectedItems.indexOf(item);
    if (index === -1) {
      this.selectedItems.push(item);
    } else {
      this.selectedItems.splice(index, 1);
    }
    this.selectionChange.emit(this.selectedItems);
  }

  isSelected(item: T): boolean {
    return this.selectedItems.includes(item);
  }

  clearFilter() {
    this.filterValue = '';
    this.filterChange.emit('');
  }
}
