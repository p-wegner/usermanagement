import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatSortModule } from '@angular/material/sort';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

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
    MatSortModule,
    MatIconModule,
    MatButtonModule
  ],
  template: `
    <table mat-table [dataSource]="data">
      <!-- Dynamic columns -->
      <ng-container *ngFor="let column of columns" [matColumnDef]="column.key">
        <th mat-header-cell *matHeaderCellDef>{{ column.label }}</th>
        <td mat-cell *matCellDef="let element">{{ element[column.key] }}</td>
      </ng-container>

      <!-- Actions column -->
      <ng-container matColumnDef="actions">
        <th mat-header-cell *matHeaderCellDef>Actions</th>
        <td mat-cell *matCellDef="let element">
          <button mat-icon-button (click)="edit.emit(element)">
            <mat-icon>edit</mat-icon>
          </button>
          <button mat-icon-button (click)="delete.emit(element)">
            <mat-icon>delete</mat-icon>
          </button>
        </td>
      </ng-container>

      <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
      <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
    </table>

    <mat-paginator
      [length]="totalItems"
      [pageSize]="pageSize"
      [pageSizeOptions]="[5, 10, 25, 100]"
      (page)="pageChange.emit($event)">
    </mat-paginator>
  `,
  styles: [`
    table {
      width: 100%;
    }
  `]
})
export class ListComponent {
  @Input() columns: Column[] = [];
  @Input() data: any[] = [];
  @Input() totalItems = 0;
  @Input() pageSize = 10;

  @Output() edit = new EventEmitter<any>();
  @Output() delete = new EventEmitter<any>();
  @Output() pageChange = new EventEmitter<any>();

  get displayedColumns(): string[] {
    return [...this.columns.map(col => col.key), 'actions'];
  }
}
