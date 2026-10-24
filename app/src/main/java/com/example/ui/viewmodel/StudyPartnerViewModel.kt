package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.model.Mood
import com.example.data.model.PersonaPreset
import com.example.data.service.GeminiStudyService
import com.example.tts.AudioQueuePlayer
import com.example.tts.EmbeddedTtsEngine
import com.example.tts.TtsModelMetadata
import com.example.tts.TtsPlaybackStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: String,
    val text: String,
    val mood: Mood? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class StudyPartnerUiState(
    val apiKey: String = "",
    val selectedModel: String = GeminiStudyService.DEFAULT_MODEL,
    val activeMood: Mood = Mood.NEUTRAL,
    val activePersona: PersonaPreset = PersonaPreset.PRESETS.first(),
    val customPersonaPrompt: String = PersonaPreset.PRESETS.first().defaultPrompt,
    val characterName: String = PersonaPreset.PRESETS.first().characterName,
    val studyContextText: String = "",
    val attachedDocName: String? = null,
    val isGenerating: Boolean = false,
    val dialogueText: String = "Hello! I'm your study partner. Attach your lecture notes, paste an essay topic, or ask me any question to begin!",
    val currentSentences: List<String> = emptyList(),
    val customMoodDrawables: Map<Mood, Uri?> = emptyMap(),
    val ttsEnabled: Boolean = true,
    val ttsSpeed: Float = 1.0f,
    val ttsPitch: Float = 1.0f,
    val ttsModelMetadata: TtsModelMetadata? = null,
    val ttsStatus: TtsPlaybackStatus = TtsPlaybackStatus(),
    val chatHistory: List<ChatMessage> = listOf(
        ChatMessage(
            sender = PersonaPreset.PRESETS.first().characterName,
            text = "Hello! I'm your study partner. Attach your lecture notes, paste an essay topic, or ask me any question to begin!",
            mood = Mood.NEUTRAL
        )
    ),
    val errorMessage: String? = null,
    val snackbarMessage: String? = null
)

class StudyPartnerViewModel(application: Application) : AndroidViewModel(application) {

    private val geminiService = GeminiStudyService()
    val ttsEngine = EmbeddedTtsEngine(application)
    val audioQueuePlayer = AudioQueuePlayer(application, ttsEngine, viewModelScope)

    private val _uiState = MutableStateFlow(
        StudyPartnerUiState(
            apiKey = getInitialApiKey(),
            ttsModelMetadata = ttsEngine.metadata
        )
    )
    val uiState: StateFlow<StudyPartnerUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            audioQueuePlayer.status.collect { status ->
                _uiState.value = _uiState.value.copy(ttsStatus = status)
            }
        }
    }

    private fun getInitialApiKey(): String {
        return try {
            val key = BuildConfig.GEMINI_API_KEY
            if (key != "MY_GEMINI_API_KEY" && key.isNotBlank()) key else ""
        } catch (_: Exception) {
            ""
        }
    }

    fun sendStudyQuery(query: String) {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isBlank()) return

        val currentState = _uiState.value
        val effectiveApiKey = currentState.apiKey.ifBlank {
            try { BuildConfig.GEMINI_API_KEY } catch (_: Exception) { "" }
        }

        if (effectiveApiKey.isBlank() || effectiveApiKey == "MY_GEMINI_API_KEY") {
            _uiState.value = currentState.copy(
                errorMessage = "Please enter your Gemini API Key in the Settings panel."
            )
            return
        }

        audioQueuePlayer.stopAll()

        val updatedHistoryWithUser = currentState.chatHistory + ChatMessage(
            sender = "USER",
            text = trimmedQuery
        )

        _uiState.value = currentState.copy(
            isGenerating = true,
            activeMood = Mood.THINKING,
            dialogueText = "Thinking through your study question...",
            chatHistory = updatedHistoryWithUser,
            errorMessage = null
        )

        viewModelScope.launch {
            val result = geminiService.sendStudyQuery(
                apiKey = effectiveApiKey,
                userQuery = trimmedQuery,
                personaPrompt = currentState.customPersonaPrompt,
                studyContext = currentState.studyContextText,
                modelName = currentState.selectedModel
            )

            result.onSuccess { parsed ->
                val finalHistory = _uiState.value.chatHistory + ChatMessage(
                    sender = currentState.characterName,
                    text = parsed.cleanText,
                    mood = parsed.mood
                )

                _uiState.value = _uiState.value.copy(
                    isGenerating = false,
                    activeMood = parsed.mood,
                    dialogueText = parsed.cleanText,
                    currentSentences = parsed.sentences,
                    chatHistory = finalHistory
                )

                if (_uiState.value.ttsEnabled && parsed.sentences.isNotEmpty()) {
                    audioQueuePlayer.startPipeline(
                        sentences = parsed.sentences,
                        mood = parsed.mood,
                        speechRate = _uiState.value.ttsSpeed,
                        speechPitch = _uiState.value.ttsPitch
                    )
                }
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isGenerating = false,
                    activeMood = Mood.SAD,
                    dialogueText = "Error: ${err.localizedMessage}",
                    errorMessage = err.localizedMessage
                )
            }
        }
    }

    fun clearChatHistory() {
        _uiState.value = _uiState.value.copy(chatHistory = emptyList())
    }

    fun updateApiKey(key: String) {
        _uiState.value = _uiState.value.copy(apiKey = key.trim())
    }

    fun updateSelectedModel(model: String) {
        _uiState.value = _uiState.value.copy(selectedModel = model)
    }

    fun selectPersona(preset: PersonaPreset) {
        _uiState.value = _uiState.value.copy(
            activePersona = preset,
            customPersonaPrompt = preset.defaultPrompt,
            characterName = preset.characterName
        )
    }

    fun updateCustomPersonaPrompt(prompt: String) {
        _uiState.value = _uiState.value.copy(customPersonaPrompt = prompt)
    }

    fun updateCharacterName(name: String) {
        _uiState.value = _uiState.value.copy(characterName = name)
    }

    fun updateStudyContext(contextText: String, docName: String? = null) {
        _uiState.value = _uiState.value.copy(
            studyContextText = contextText,
            attachedDocName = docName
        )
    }

    fun setMoodAsset(mood: Mood, uri: Uri?) {
        val currentMap = _uiState.value.customMoodDrawables.toMutableMap()
        if (uri != null) {
            currentMap[mood] = uri
        } else {
            currentMap.remove(mood)
        }
        _uiState.value = _uiState.value.copy(customMoodDrawables = currentMap)
    }

    fun importCustomTtsModel(uri: Uri) {
        val result = ttsEngine.importCustomModel(uri)
        result.onSuccess { metadata ->
            _uiState.value = _uiState.value.copy(
                ttsModelMetadata = metadata,
                snackbarMessage = "Custom model '${metadata.modelName}' loaded successfully!"
            )
        }.onFailure { err ->
            _uiState.value = _uiState.value.copy(
                errorMessage = "Failed to import model: ${err.localizedMessage}"
            )
        }
    }

    fun testTtsVoice() {
        val sentences = listOf(
            "Speech synthesis unit online.",
            "Ready for study session."
        )
        audioQueuePlayer.startPipeline(
            sentences = sentences,
            mood = _uiState.value.activeMood,
            speechRate = _uiState.value.ttsSpeed,
            speechPitch = _uiState.value.ttsPitch
        )
    }

    fun updateTtsSpeed(speed: Float) {
        _uiState.value = _uiState.value.copy(ttsSpeed = speed)
    }

    fun updateTtsPitch(pitch: Float) {
        _uiState.value = _uiState.value.copy(ttsPitch = pitch)
    }

    fun toggleTtsEnabled() {
        val next = !_uiState.value.ttsEnabled
        if (!next) {
            audioQueuePlayer.stopAll()
        }
        _uiState.value = _uiState.value.copy(ttsEnabled = next)
    }

    fun togglePlayPause() {
        audioQueuePlayer.togglePlayPause()
    }

    fun replayAudio() {
        audioQueuePlayer.replayAll(_uiState.value.activeMood)
    }

    fun stopAudio() {
        audioQueuePlayer.stopAll()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearSnackbar() {
        _uiState.value = _uiState.value.copy(snackbarMessage = null)
    }

    override fun onCleared() {
        super.onCleared()
        audioQueuePlayer.release()
    }
}
