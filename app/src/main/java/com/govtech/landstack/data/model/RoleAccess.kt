package com.govtech.landstack.data.model

/**
 * Centralized role-based access control matrix.
 * Maps features to access levels per role, matching the project's
 * access tables for Citizen, Revenue Officer, Registration Officer,
 * Surveyor, Land Officer (legacy), and Admin.
 */
enum class AccessLevel { NONE, VIEW, EDIT, FULL }

object RoleAccess {

    // Feature constants
    const val GIS_MAP = "gis_map"
    const val CADASTRAL_MAPS = "cadastral_maps"
    const val PARCEL_BOUNDARIES = "parcel_boundaries"
    const val PARCEL_ID = "parcel_id"
    const val PARCEL_SEARCH = "parcel_search"
    const val RECORD_OF_RIGHTS = "record_of_rights"
    const val OWNERSHIP_RECORDS = "ownership_records"
    const val OWNERSHIP_HISTORY = "ownership_history"
    const val LAND_REGISTRATION = "land_registration"
    const val TRANSACTION_TRACKING = "transaction_tracking"
    const val ENCUMBRANCE_RECORDS = "encumbrance_records"
    const val DOCUMENTS = "documents"
    const val DISPUTES = "disputes"
    const val CREATE_PARCEL = "create_parcel"
    const val EDIT_PARCEL = "edit_parcel"

    private val matrix: Map<String, Map<String, AccessLevel>> = mapOf(
        GIS_MAP to mapOf(
            "Citizen" to AccessLevel.FULL,
            "Revenue Officer" to AccessLevel.FULL,
            "Registration Officer" to AccessLevel.VIEW,
            "Surveyor" to AccessLevel.EDIT,
            "Land Officer" to AccessLevel.FULL,
            "Admin" to AccessLevel.FULL
        ),
        CADASTRAL_MAPS to mapOf(
            "Citizen" to AccessLevel.FULL,
            "Revenue Officer" to AccessLevel.FULL,
            "Registration Officer" to AccessLevel.VIEW,
            "Surveyor" to AccessLevel.EDIT,
            "Land Officer" to AccessLevel.FULL,
            "Admin" to AccessLevel.FULL
        ),
        PARCEL_BOUNDARIES to mapOf(
            "Citizen" to AccessLevel.FULL,
            "Revenue Officer" to AccessLevel.VIEW,
            "Registration Officer" to AccessLevel.VIEW,
            "Surveyor" to AccessLevel.EDIT,
            "Land Officer" to AccessLevel.EDIT,
            "Admin" to AccessLevel.FULL
        ),
        PARCEL_ID to mapOf(
            "Citizen" to AccessLevel.FULL,
            "Revenue Officer" to AccessLevel.EDIT,
            "Registration Officer" to AccessLevel.VIEW,
            "Surveyor" to AccessLevel.EDIT,
            "Land Officer" to AccessLevel.EDIT,
            "Admin" to AccessLevel.FULL
        ),
        PARCEL_SEARCH to mapOf(
            "Citizen" to AccessLevel.FULL,
            "Revenue Officer" to AccessLevel.FULL,
            "Registration Officer" to AccessLevel.FULL,
            "Surveyor" to AccessLevel.FULL,
            "Land Officer" to AccessLevel.FULL,
            "Admin" to AccessLevel.FULL
        ),
        RECORD_OF_RIGHTS to mapOf(
            "Citizen" to AccessLevel.FULL,
            "Revenue Officer" to AccessLevel.EDIT,
            "Registration Officer" to AccessLevel.VIEW,
            "Surveyor" to AccessLevel.VIEW,
            "Land Officer" to AccessLevel.EDIT,
            "Admin" to AccessLevel.FULL
        ),
        OWNERSHIP_RECORDS to mapOf(
            "Citizen" to AccessLevel.FULL,
            "Revenue Officer" to AccessLevel.EDIT,
            "Registration Officer" to AccessLevel.VIEW,
            "Surveyor" to AccessLevel.VIEW,
            "Land Officer" to AccessLevel.EDIT,
            "Admin" to AccessLevel.FULL
        ),
        OWNERSHIP_HISTORY to mapOf(
            "Citizen" to AccessLevel.FULL,
            "Revenue Officer" to AccessLevel.EDIT,
            "Registration Officer" to AccessLevel.VIEW,
            "Surveyor" to AccessLevel.VIEW,
            "Land Officer" to AccessLevel.EDIT,
            "Admin" to AccessLevel.FULL
        ),
        LAND_REGISTRATION to mapOf(
            "Citizen" to AccessLevel.FULL,
            "Revenue Officer" to AccessLevel.VIEW,
            "Registration Officer" to AccessLevel.EDIT,
            "Surveyor" to AccessLevel.VIEW,
            "Land Officer" to AccessLevel.EDIT,
            "Admin" to AccessLevel.FULL
        ),
        TRANSACTION_TRACKING to mapOf(
            "Citizen" to AccessLevel.FULL,
            "Revenue Officer" to AccessLevel.VIEW,
            "Registration Officer" to AccessLevel.EDIT,
            "Surveyor" to AccessLevel.NONE,
            "Land Officer" to AccessLevel.EDIT,
            "Admin" to AccessLevel.FULL
        ),
        ENCUMBRANCE_RECORDS to mapOf(
            "Citizen" to AccessLevel.FULL,
            "Revenue Officer" to AccessLevel.EDIT,
            "Registration Officer" to AccessLevel.EDIT,
            "Surveyor" to AccessLevel.NONE,
            "Land Officer" to AccessLevel.EDIT,
            "Admin" to AccessLevel.FULL
        ),
        DOCUMENTS to mapOf(
            "Citizen" to AccessLevel.FULL,
            "Revenue Officer" to AccessLevel.EDIT,
            "Registration Officer" to AccessLevel.EDIT,
            "Surveyor" to AccessLevel.VIEW,
            "Land Officer" to AccessLevel.EDIT,
            "Admin" to AccessLevel.FULL
        ),
        DISPUTES to mapOf(
            "Citizen" to AccessLevel.FULL,
            "Revenue Officer" to AccessLevel.EDIT,
            "Registration Officer" to AccessLevel.EDIT,
            "Surveyor" to AccessLevel.VIEW,
            "Land Officer" to AccessLevel.EDIT,
            "Admin" to AccessLevel.FULL
        ),
        CREATE_PARCEL to mapOf(
            "Citizen" to AccessLevel.FULL,
            "Revenue Officer" to AccessLevel.EDIT,
            "Registration Officer" to AccessLevel.NONE,
            "Surveyor" to AccessLevel.EDIT,
            "Land Officer" to AccessLevel.EDIT,
            "Admin" to AccessLevel.FULL
        ),
        EDIT_PARCEL to mapOf(
            "Citizen" to AccessLevel.FULL,
            "Revenue Officer" to AccessLevel.EDIT,
            "Registration Officer" to AccessLevel.NONE,
            "Surveyor" to AccessLevel.EDIT,
            "Land Officer" to AccessLevel.EDIT,
            "Admin" to AccessLevel.FULL
        )
    )

    fun accessLevel(role: String, feature: String): AccessLevel {
        return matrix[feature]?.get(role) ?: AccessLevel.NONE
    }

    fun canView(role: String, feature: String): Boolean {
        val level = accessLevel(role, feature)
        return level != AccessLevel.NONE
    }

    fun canEdit(role: String, feature: String): Boolean {
        val level = accessLevel(role, feature)
        return level == AccessLevel.EDIT || level == AccessLevel.FULL
    }

    fun isOfficerRole(role: String): Boolean {
        val officerRoles = listOf("Citizen", "Land Officer", "Revenue Officer", "Registration Officer", "Surveyor", "Admin")
        return officerRoles.any { it.equals(role, ignoreCase = true) }
    }
}
