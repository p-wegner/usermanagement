import { Component, Input, OnInit } from '@angular/core';
import { Permission } from '../../../shared/interfaces/permission.interface';
import { PermissionGroup } from '../../../shared/interfaces/permission.interface';
import { GroupsService } from '../../groups/groups.service';

interface InheritanceNode {
  name: string;
  type: 'permission' | 'group';
  children: InheritanceNode[];
}

@Component({
  selector: 'app-permission-inheritance',
  templateUrl: './permission-inheritance.component.html',
  styleUrls: ['./permission-inheritance.component.css']
})
export class PermissionInheritanceComponent implements OnInit {
  @Input() permission!: Permission;
  inheritanceTree: InheritanceNode | null = null;

  constructor(private groupsService: GroupsService) {}

  ngOnInit(): void {
    this.buildInheritanceTree();
  }

  private buildInheritanceTree(): void {
    this.groupsService.getGroups().subscribe(groups => {
      const relatedGroups = groups.filter(group => 
        group.permissions.some(p => p.id === this.permission.id)
      );

      this.inheritanceTree = {
        name: this.permission.name,
        type: 'permission',
        children: relatedGroups.map(group => ({
          name: group.name,
          type: 'group',
          children: []
        }))
      };
    });
  }
}
