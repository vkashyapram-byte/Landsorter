package com.govtech.landstack.feature.aiassistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.govtech.landstack.data.local.LandStackDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import com.govtech.landstack.data.model.Parcel
import javax.inject.Inject

@HiltViewModel
class AIAssistantViewModel @Inject constructor(
    private val database: LandStackDatabase
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(ChatMessage("Hello! I am your AI Land Assistant. You can ask me questions about parcel zoning, permitted uses, and restrictions.", false))
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    fun handleQuery(query: String, ulpin: String?) {
        val lowerQuery = query.lowercase()
        
        // Add user message
        val currentMessages = _messages.value.toMutableList()
        currentMessages.add(ChatMessage(query, true))
        _messages.value = currentMessages

        viewModelScope.launch {
            val response = generateResponse(lowerQuery, ulpin)
            val updatedMessages = _messages.value.toMutableList()
            updatedMessages.add(ChatMessage(response, false))
            _messages.value = updatedMessages
        }
    }

    private suspend fun generateResponse(query: String, ulpin: String?): String {
        // Intent parsing
        if (query.contains("hi") || query.contains("hello") || query.contains("hey")) {
            return "Hello there! How can I help you with land records today?"
        }

        if (ulpin.isNullOrBlank()) {
            return "Please select a parcel first from My Property or Search to ask specific questions about zoning or restrictions."
        }

        val parcel = database.parcelDao().getParcelSync(ulpin)
            ?: return "I couldn't find data for parcel $ulpin."

        val parcelModel = try {
            Json { ignoreUnknownKeys = true }.decodeFromString<Parcel>(parcel.parcelDataJson)
        } catch (e: Exception) { null }

        if (query.contains("zone") || query.contains("zoning") || query.contains("use")) {
            return "Parcel $ulpin is classified as '${parcelModel?.base?.parcelType ?: "Unknown"}' with zoning '${parcel.zoningClassification}'. Permitted use: ${parcel.permittedUse}."
        }

        if (query.contains("restriction") || query.contains("limit")) {
            val restrictions = database.restrictionDao().getRestrictionsByUlpinSync(ulpin)
            if (restrictions.isEmpty()) {
                return "There are no active development restrictions for parcel $ulpin."
            }
            val restrictionText = restrictions.joinToString("; ") { it.restrictionType + ": " + it.description }
            return "Parcel $ulpin has the following restrictions: $restrictionText."
        }
        
        if (query.contains("area") || query.contains("size")) {
            return "The area of parcel $ulpin is ${parcelModel?.base?.areaSqm ?: "Unknown"} sqm."
        }

        return "I see you are asking about '$query'. Based on the land records for parcel $ulpin, I am not sure how to answer that specifically. Try asking about zoning, use, restrictions, or area."
    }
}
