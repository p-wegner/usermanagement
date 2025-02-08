import {Component, Input, OnInit} from '@angular/core';
import {Permission} from '../../../shared/interfaces/permission.interface';
import {PermissionGroup} from '../../../shared/interfaces/permission.interface';
import {GroupsService} from '../../groups/groups.service';
import {PermissionConflictService} from '../../../shared/services/permission-conflict.service';
import {MatCard, MatCardHeader} from '@angular/material/card';

interface InheritanceNode {
  name: string;
  type: 'permission' | 'group';
  children: InheritanceNode[];
  hasConflicts?: boolean;
}

@Component({
  selector: 'app-permission-inheritance',
  templateUrl: './permission-inheritance.component.html',
  styleUrls: ['./permission-inheritance.component.css'],
  standalone: false
})
export class PermissionInheritanceComponent implements OnInit {
  @Input() permission!: Permission;
  inheritanceTree!: InheritanceNode;

  constructor(
    private groupsService: GroupsService,
    private conflictService: PermissionConflictService
  ) {
  }

  ngOnInit(): void {
    this.buildInheritanceTree();
  }

  private buildInheritanceTree(): void {
    this.groupsService.getGroups().subscribe(groups => {
      const relatedGroups = groups.filter(group =>
        group.permissions.some(p => p.id === this.permission.id)
      );

      // Check for conflicts in inherited permissions
      const allPermissions = relatedGroups.flatMap(g => g.permissions);
      const conflicts = this.conflictService.detectConflicts(allPermissions);

      this.inheritanceTree = {
        name: this.permission.name,
        type: 'permission',
        children: relatedGroups.map(group => ({
          name: group.name,
          type: 'group',
          children: [],
          hasConflicts: conflicts.has(this.permission.name)
        }))
      };
    });
  }
}
