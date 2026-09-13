package com.govtech.landstack.data.repository

sealed class SyncResult {
    object Success : SyncResult()
    data class NetworkError(val message: String? = null) : SyncResult()
    object PermissionDenied : SyncResult()
    data class Conflict(val ulpin: String, val message: String) : SyncResult()
    data class UnknownError(val message: String?) : SyncResult()
}
