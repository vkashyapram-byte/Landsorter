package com.govtech.landstack.data.repository

import com.govtech.landstack.data.model.Registration
import kotlinx.coroutines.flow.Flow

interface RegistrationRepository {
    fun getRegistration(ulpin: String): Flow<Registration?>
}
