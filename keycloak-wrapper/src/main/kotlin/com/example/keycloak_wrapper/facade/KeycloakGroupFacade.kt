package com.example.keycloak_wrapper.facade

import com.example.keycloak_wrapper.dto.ClientRoleDto
import com.example.keycloak_wrapper.dto.RoleAssignmentDto
import com.example.keycloak_wrapper.dto.RoleDto
import com.example.keycloak_wrapper.dto.TenantCreateDto
import com.example.keycloak_wrapper.dto.TenantUpdateDto
import com.example.keycloak_wrapper.exception.KeycloakException
import jakarta.ws.rs.core.Response
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.resource.GroupResource
import org.keycloak.representations.idm.GroupRepresentation
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class KeycloakGroupFacade(
    private val keycloak: Keycloak,
    @Value("\${keycloak.realm}")
    private val realm: String
) {
    fun getGroups(
        search: String?, 
        first: Int, 
        max: Int, 
        tenantsOnly: Boolean = false
    ): List<GroupRepresentation> {
        return try {
            val groups = keycloak.realm(realm).groups().groups(search, first, max)
            if (tenantsOnly) {
                groups.filter { it.attributes?.get("isTenant")?.firstOrNull()?.equals("true", ignoreCase = true) == true }
            } else {
                groups
            }
        } catch (e: Exception) {
            throw KeycloakException("Failed to fetch groups", e)
        }
    }

    fun createTenant(tenantCreateDto: TenantCreateDto): GroupRepresentation {
        try {
            val tenantGroup = GroupRepresentation().apply {
                name = "tenant_${tenantCreateDto.name}"
                attributes = mutableMapOf(
                    "isTenant" to listOf("true"),
                    "displayName" to listOf(tenantCreateDto.displayName)
                )
            }
            val response = keycloak.realm(realm).groups().add(tenantGroup)
            
            if (response.status != Response.Status.CREATED.statusCode) {
                val errorBody = response.readEntity(String::class.java)
                throw KeycloakException("Failed to create tenant: ${response.status} - $errorBody")
            }

            val locationHeader = response.location.toString()
            val tenantId = locationHeader.substring(locationHeader.lastIndexOf("/") + 1)
            return getGroup(tenantId)
        } catch (e: Exception) {
            throw KeycloakException("Failed to create tenant", e)
        }
    }

    fun updateTenant(id: String, tenantUpdateDto: TenantUpdateDto): GroupRepresentation {
        try {
            val groupResource = keycloak.realm(realm).groups().group(id)
            val existingGroup = groupResource.toRepresentation()
            
            existingGroup.attributes = existingGroup.attributes ?: mutableMapOf()
            existingGroup.attributes["displayName"] = listOf(tenantUpdateDto.displayName)
            
            groupResource.update(existingGroup)
            return getGroup(id)
        } catch (e: Exception) {
            throw KeycloakException("Failed to update tenant", e)
        }
    }

    fun deleteTenant(id: String) {
        try {
            val groupResource = keycloak.realm(realm).groups().group(id)
            val group = groupResource.toRepresentation()
            
            // Ensure this is actually a tenant group
            if (group.attributes?.get("isTenant")?.firstOrNull() != "true") {
                throw KeycloakException("Group is not a tenant")
            }
            
            groupResource.remove()
        } catch (e: Exception) {
            throw KeycloakException("Failed to delete tenant", e)
        }
    }

    fun getGroup(id: String): GroupRepresentation {
        return try {
            keycloak.realm(realm).groups().group(id).toRepresentation()
        } catch (e: Exception) {
            throw KeycloakException("Failed to fetch group with id: $id", e)
        }
    }

    fun createGroup(group: GroupRepresentation): GroupRepresentation {
        try {
            val response = keycloak.realm(realm).groups().add(group)
            if (response.status != Response.Status.CREATED.statusCode) {
                val errorBody = response.readEntity(String::class.java)
                throw KeycloakException("Failed to create group: ${response.status} - $errorBody")
            }

            val locationHeader = response.location.toString()
            val groupId = locationHeader.substring(locationHeader.lastIndexOf("/") + 1)
            return getGroup(groupId)
        } catch (e: Exception) {
            throw KeycloakException("Failed to create group", e)
        }
    }

    fun createSubGroup(parentId: String, group: GroupRepresentation): GroupRepresentation {
        try {
            val response = keycloak.realm(realm).groups().group(parentId).subGroup(group)
            if (response.status != Response.Status.CREATED.statusCode) {
                val errorBody = response.readEntity(String::class.java)
                throw KeycloakException("Failed to create subgroup: ${response.status} - $errorBody")
            }

            val locationHeader = response.location.toString()
            val groupId = locationHeader.substring(locationHeader.lastIndexOf("/") + 1)
            return getGroup(groupId)
        } catch (e: Exception) {
            throw KeycloakException("Failed to create subgroup", e)
        }
    }

    fun updateGroup(id: String, group: GroupRepresentation): GroupRepresentation {
        try {
            val groupResource = keycloak.realm(realm).groups().group(id)
            groupResource.update(group)
            return getGroup(id)
        } catch (e: Exception) {
            throw KeycloakException("Failed to update group with id: $id", e)
        }
    }

    fun updateGroupRoles(id: String, roleAssignment: RoleAssignmentDto): GroupRepresentation {
        try {
            val groupResource = keycloak.realm(realm).groups().group(id)
            updateRealmRoles(roleAssignment, groupResource)
            updateClientRoles(roleAssignment, groupResource)
            return getGroup(id)
        } catch (e: Exception) {
            throw KeycloakException("Failed to update roles for group with id: $id", e)
        }
    }

    private fun updateRealmRoles(
        roleAssignment: RoleAssignmentDto,
        groupResource: GroupResource
    ) {
        val realmRoleReps = roleAssignment.realmRoles.map {
            keycloak.realm(realm).roles().get(it.name).toRepresentation()
        }
        groupResource.roles().realmLevel().remove(groupResource.roles().realmLevel().listAll())
        if (realmRoleReps.isNotEmpty()) {
            groupResource.roles().realmLevel().add(realmRoleReps)
        }
    }

    fun getGroupRoles(id: String): RoleAssignmentDto {
        try {
            val groupResource = keycloak.realm(realm).groups().group(id)
            val realmRoles = getRealmRoles(groupResource)
            val clientRoles = getClientRoles(groupResource)
            return RoleAssignmentDto(realmRoles, clientRoles)
        } catch (e: Exception) {
            throw KeycloakException("Failed to get roles for group with id: $id", e)
        }
    }

    private fun getClientRoles(groupResource: GroupResource): List<ClientRoleDto> {
        val clientRoles = keycloak.realm(realm).clients().findAll().mapNotNull { client ->
            val roles = groupResource.roles().clientLevel(client.id).listAll()
            if (roles.isNotEmpty()) {
                ClientRoleDto(
                    clientId = client.id,
                    clientName = client.clientId,
                    roles = roles.map { role ->
                        RoleDto(
                            id = role.id,
                            name = role.name,
                            description = role.description,
                            composite = role.isComposite,
                            clientRole = role.clientRole
                        )
                    }
                )
            } else {
                null
            }
        }
        return clientRoles
    }

    private fun getRealmRoles(groupResource: GroupResource) =
        groupResource.roles().realmLevel().listAll()
            .map { role ->
                RoleDto(
                    id = role.id,
                    name = role.name,
                    description = role.description,
                    composite = role.isComposite,
                    clientRole = role.clientRole
                )
            }

    fun deleteGroup(id: String) {
        try {
            keycloak.realm(realm).groups().group(id).remove()
        } catch (e: Exception) {
            throw KeycloakException("Failed to delete group with id: $id", e)
        }
    }

    private fun updateClientRoles(
        roleAssignment: RoleAssignmentDto,
        groupResource: GroupResource
    ) {
        roleAssignment.clientRoles
            .forEach {
                val clientId = it.clientId
                val client = keycloak.realm(realm).clients().get(clientId)
                val clientRoleReps = it.roles.map { role ->
                    client.roles().get(role.name).toRepresentation()
                }
                groupResource.roles().clientLevel(clientId)
                    .remove(groupResource.roles().clientLevel(clientId).listAll())
                if (clientRoleReps.isNotEmpty()) {
                    groupResource.roles().clientLevel(clientId).add(clientRoleReps)
                }
            }
    }
    
    fun getGroupMembers(groupId: String): List<org.keycloak.representations.idm.UserRepresentation> {
        try {
            return keycloak.realm(realm).groups().group(groupId).members()
        } catch (e: Exception) {
            throw KeycloakException("Failed to get members for group with id: $groupId", e)
        }
    }
}
