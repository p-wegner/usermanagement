[Git Code Review Diff]

This file contains the git diff content for code review analysis.

```diff
diff --git a/um/src/app/features/groups/group-detail/group-detail.component.ts b/um/src/app/features/groups/group-detail/group-detail.component.ts
index a1b0a76..f824773 100644
--- a/um/src/app/features/groups/group-detail/group-detail.component.ts
+++ b/um/src/app/features/groups/group-detail/group-detail.component.ts
@@ -19,7 +19,6 @@ import {NgIf} from '@angular/common';
 import {MatInput} from '@angular/material/input';
 import {MatTooltip} from '@angular/material/tooltip';
 import {MatButton} from '@angular/material/button';
-import {map} from 'rxjs';
 
 @Component({
   selector: 'app-group-detail',
diff --git a/um/src/app/features/groups/groups-list/groups-list.component.html b/um/src/app/features/groups/groups-list/groups-list.component.html
index 380ded6..c937d83 100644
--- a/um/src/app/features/groups/groups-list/groups-list.component.html
+++ b/um/src/app/features/groups/groups-list/groups-list.component.html
@@ -1,8 +1,8 @@
 <app-list
   [title]="'Groups'"
-  [items]="(groups$ | async)!!"
+  [items]="groups$ | async"
   [columns]="columns"
-  [loading]="(loading$ | async)!!"
+  [loading]="loading$ | async"
   (edit)="onEditGroup($event.id)"
   (delete)="onDeleteGroup($event.id)">
   <button mat-fab color="primary" class="add-button" (click)="onAddGroup()">
diff --git a/um/src/app/features/groups/groups.service.ts b/um/src/app/features/groups/groups.service.ts
index 5fcb63f..4c8b065 100644
--- a/um/src/app/features/groups/groups.service.ts
+++ b/um/src/app/features/groups/groups.service.ts
@@ -1,5 +1,5 @@
 import {Injectable} from '@angular/core';
-import {BehaviorSubject, Observable, map, throwError, flatMap, switchMap} from 'rxjs';
+import {BehaviorSubject, Observable, map, throwError} from 'rxjs';
 import {Permission, PermissionGroup} from '../../shared/interfaces/permission.interface';
 import {GroupControllerService} from '../../api/com/example/api/groupController.service';
 import {RoleControllerService} from '../../api/com/example/api/roleController.service';
@@ -14,7 +14,7 @@ import {RoleDto} from '../../api/com/example';
 export class GroupsService {
   private readonly groupsSubject = new BehaviorSubject<PermissionGroup[]>([]);
   private readonly loadingSubject = new BehaviorSubject<boolean>(false);
-
+  
   readonly groups$ = this.groupsSubject.asObservable();
   readonly loading$ = this.loadingSubject.asObservable();
 
@@ -29,20 +29,14 @@ export class GroupsService {
     this.loadGroups();
   }
 
-  private async blobToJson(blob: Blob): Promise<any> {
-    const text = await blob.text();
-    return JSON.parse(text);
-  }
-
   loadGroups(page: number = 0, size: number = 20, search?: string): void {
     this.loadingSubject.next(true);
     this.groupControllerService.getGroups(page, size, search).pipe(
-      switchMap(async (response: any) => {
-        const jsonResponse = await this.blobToJson(response);
-        if (!jsonResponse.success || !jsonResponse.data) {
-          throw new Error(jsonResponse.error || 'Failed to fetch groups');
+      map(response => {
+        if (!response.success || !response.data) {
+          throw new Error(response.error || 'Failed to fetch groups');
         }
-        return jsonResponse.data.map(this.mapToPermissionGroup);
+        return response.data.map(this.mapToPermissionGroup);
       })
     ).subscribe({
       next: (groups) => {
@@ -59,12 +53,11 @@ export class GroupsService {
 
   getGroup(id: string): Observable<PermissionGroup> {
     return this.groupControllerService.getGroup(id).pipe(
-      switchMap(async (response: any) => {
-        const jsonResponse = await this.blobToJson(response);
-        if (!jsonResponse.success || !jsonResponse.data) {
-          throw new Error(jsonResponse.error || 'Failed to fetch group');
+      map(response => {
+        if (!response.success || !response.data) {
+          throw new Error(response.error || 'Failed to fetch group');
         }
-        return this.mapToPermissionGroup(jsonResponse.data);
+        return this.mapToPermissionGroup(response.data);
       })
     );
   }
@@ -77,12 +70,11 @@ export class GroupsService {
     };
 
     return this.groupControllerService.createGroup(dto).pipe(
-      switchMap(async (response: any) => {
-        const jsonResponse = await this.blobToJson(response);
-        if (!jsonResponse.success || !jsonResponse.data) {
-          throw new Error(jsonResponse.error || 'Failed to create group');
+      map(response => {
+        if (!response.success || !response.data) {
+          throw new Error(response.error || 'Failed to create group');
         }
-        const newGroup = this.mapToPermissionGroup(jsonResponse.data);
+        const newGroup = this.mapToPermissionGroup(response.data);
         const currentGroups = this.groupsSubject.value;
         this.groupsSubject.next([...currentGroups, newGroup]);
         return newGroup;
@@ -96,22 +88,20 @@ export class GroupsService {
     };
 
     return this.groupControllerService.updateGroup(id, dto).pipe(
-      switchMap(async (response: any) => {
-        const jsonResponse = await this.blobToJson(response);
-        if (!jsonResponse.success || !jsonResponse.data) {
-          throw new Error(jsonResponse.error || 'Failed to update group');
+      map(response => {
+        if (!response.success || !response.data) {
+          throw new Error(response.error || 'Failed to update group');
         }
-        return this.mapToPermissionGroup(jsonResponse.data);
+        return this.mapToPermissionGroup(response.data);
       })
     );
   }
 
   deleteGroup(id: string): Observable<void> {
     return this.groupControllerService.deleteGroup(id).pipe(
-      switchMap(async (response: any) => {
-        const jsonResponse = await this.blobToJson(response);
-        if (!jsonResponse.success) {
-          throw new Error(jsonResponse.error || 'Failed to delete group');
+      map(response => {
+        if (!response.success) {
+          throw new Error(response.error || 'Failed to delete group');
         }
         const currentGroups = this.groupsSubject.value;
         this.groupsSubject.next(currentGroups.filter(group => group.id !== id));
@@ -121,12 +111,11 @@ export class GroupsService {
 
   getAvailablePermissions(): Observable<Permission[]> {
     return this.roleControllerService.getRoles().pipe(
-      switchMap(async (response: any) => {
-        const jsonResponse = await this.blobToJson(response);
-        if (!jsonResponse.success || !jsonResponse.data) {
-          throw new Error(jsonResponse.error || 'Failed to fetch roles');
+      map(response => {
+        if (!response.success || !response.data) {
+          throw new Error(response.error || 'Failed to fetch roles');
         }
-        return jsonResponse.data.map((role: RoleDto) => ({
+        return response.data.map((role: RoleDto) => ({
           id: role.id || '',
           name: role.name,
           description: role.description || '',
@@ -161,7 +150,7 @@ export class GroupsService {
         composite: role.composite,
         clientRole: role.clientRole
       })) || [],
-      subGroups: dto.subGroups.map(it=>this.mapToPermissionGroup(it))
+      subGroups: dto.subGroups.map(this.mapToPermissionGroup)
     };
   }
 }
```