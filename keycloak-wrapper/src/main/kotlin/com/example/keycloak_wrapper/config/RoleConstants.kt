package com.example.keycloak_wrapper.config

/**
 * Constants for role names used throughout the application.
 */
object RoleConstants {
    const val ROLE_ADMIN = "ADMIN"
    const val ROLE_USER_MANAGER = "USER_MANAGER"
    const val ROLE_USER_VIEWER = "USER_VIEWER"
    const val ROLE_GROUP_MANAGER = "GROUP_MANAGER"
    const val ROLE_GROUP_VIEWER = "GROUP_VIEWER"
    const val ROLE_ROLE_VIEWER = "ROLE_VIEWER"
    
    // Role groups for annotations
    val USER_MANAGEMENT_ROLES = arrayOf(ROLE_ADMIN, ROLE_USER_MANAGER, ROLE_USER_VIEWER)
    val USER_EDIT_ROLES = arrayOf(ROLE_ADMIN, ROLE_USER_MANAGER)
    val GROUP_MANAGEMENT_ROLES = arrayOf(ROLE_ADMIN, ROLE_GROUP_MANAGER, ROLE_GROUP_VIEWER)
    val GROUP_EDIT_ROLES = arrayOf(ROLE_ADMIN, ROLE_GROUP_MANAGER)
    val ROLE_MANAGEMENT_ROLES = arrayOf(ROLE_ADMIN, ROLE_ROLE_VIEWER)
    val AUTHENTICATED = arrayOf("AUTHENTICATED")
}
