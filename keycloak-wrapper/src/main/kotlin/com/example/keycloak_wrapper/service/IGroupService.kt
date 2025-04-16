package com.example.keycloak_wrapper.service

import com.example.keycloak_wrapper.dto.*

interface IGroupService {
    fun getGroups(searchDto: GroupSearchDto): List<GroupDto>
    fun getGroup(id: String): GroupDto
    fun createGroup(dto: GroupCreateDto): GroupDto
    fun updateGroup(id: String, dto: GroupUpdateDto): GroupDto
    fun deleteGroup(id: String)
    fun updateGroupRoles(id: String, roleAssignment: RoleAssignmentDto): GroupDto
    fun getGroupRoles(id: String): RoleAssignmentDto
}
