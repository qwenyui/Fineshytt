package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.CharacterDisplay
import com.example.ui.components.ChatHistoryDialog
import com.example.ui.components.ConfigDrawerSheet
import com.example.ui.components.PartnerInputBar
import com.example.ui.components.VisualNovelDialogueBox
import com.example.ui.theme.MangaBlack
import com.example.ui.theme.MangaDarkGray
import com.example.ui.theme.MangaLightGray
import com.example.ui.theme.MangaWhite
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.StudyPartnerViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: StudyPartnerViewModel = viewModel()
                StudyPartnerMainScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun StudyPartnerMainScreen(
    viewModel: StudyPartnerViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isConfigOpen by remember { mutableStateOf(false) }
    var isChatHistoryOpen by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    BackHandler(enabled = isConfigOpen || isChatHistoryOpen) {
        if (isChatHistoryOpen) isChatHistoryOpen = false
        else if (isConfigOpen) isConfigOpen = false
    }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("main_scaffold"),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MangaBlack
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MangaBlack)
        ) {
            val isLandscape = maxWidth > maxHeight

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                // Top App Header: Includes dedicated TTS Enable Button, Chat History Button, and Settings Button
                TopHeaderBar(
                    characterName = uiState.characterName,
                    ttsEnabled = uiState.ttsEnabled,
                    onToggleTts = { viewModel.toggleTtsEnabled() },
                    onOpenChatHistory = { isChatHistoryOpen = true },
                    onOpenConfig = { isConfigOpen = true }
                )

                // Error Banner
                AnimatedVisibility(visible = uiState.errorMessage != null) {
                    uiState.errorMessage?.let { err ->
                        ErrorNotificationBanner(
                            errorMessage = err,
                            onFixApiKey = { isConfigOpen = true }
                        )
                    }
                }

                if (isLandscape) {
                    // LANDSCAPE LAYOUT: Side-by-Side Cyberpunk Manga Studio
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    ) {
                        // Left Pane: Character Display Viewport
                        Box(
                            modifier = Modifier
                                .weight(0.42f)
                                .fillMaxHeight()
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CharacterDisplay(
                                mood = uiState.activeMood,
                                customUri = uiState.customMoodDrawables[uiState.activeMood],
                                isSpeaking = uiState.ttsStatus.isPlaying,
                                isThinking = uiState.isGenerating,
                                onPickImage = { isConfigOpen = true }
                            )
                        }

                        // Right Pane: Prompts, Dialogue (Subtitles or Scrollable Main Text), and Input Bar
                        Column(
                            modifier = Modifier
                                .weight(0.58f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            QuickStudyPrompts(
                                onSelectPrompt = { query -> viewModel.sendStudyQuery(query) }
                            )

                            // Dialogue Box at Bottom Right: Subtitles if TTS Enabled, Scrollable Main Text if TTS Disabled
                            VisualNovelDialogueBox(
                                speakerName = uiState.characterName,
                                dialogueText = uiState.dialogueText,
                                mood = uiState.activeMood,
                                ttsEnabled = uiState.ttsEnabled,
                                ttsStatus = uiState.ttsStatus,
                                onTogglePlayPause = { viewModel.togglePlayPause() },
                                onReplayAudio = { viewModel.replayAudio() },
                                onStopAudio = { viewModel.stopAudio() }
                            )

                            PartnerInputBar(
                                isGenerating = uiState.isGenerating,
                                ttsEnabled = uiState.ttsEnabled,
                                attachedDocName = uiState.attachedDocName,
                                studyContextText = uiState.studyContextText,
                                onSendQuery = { query -> viewModel.sendStudyQuery(query) },
                                onOpenSettings = { isConfigOpen = true },
                                onToggleTts = { viewModel.toggleTtsEnabled() },
                                onClearDoc = { viewModel.updateStudyContext("", null) }
                            )
                        }
                    }
                } else {
                    // PORTRAIT LAYOUT
                    QuickStudyPrompts(
                        onSelectPrompt = { query -> viewModel.sendStudyQuery(query) }
                    )

                    // Central Character Display Area
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CharacterDisplay(
                            mood = uiState.activeMood,
                            customUri = uiState.customMoodDrawables[uiState.activeMood],
                            isSpeaking = uiState.ttsStatus.isPlaying,
                            isThinking = uiState.isGenerating,
                            onPickImage = { isConfigOpen = true }
                        )
                    }

                    // Dialogue Box: Subtitles if TTS Enabled, Scrollable Main Text if TTS Disabled
                    VisualNovelDialogueBox(
                        speakerName = uiState.characterName,
                        dialogueText = uiState.dialogueText,
                        mood = uiState.activeMood,
                        ttsEnabled = uiState.ttsEnabled,
                        ttsStatus = uiState.ttsStatus,
                        onTogglePlayPause = { viewModel.togglePlayPause() },
                        onReplayAudio = { viewModel.replayAudio() },
                        onStopAudio = { viewModel.stopAudio() }
                    )

                    // Bottom Query Input Bar
                    PartnerInputBar(
                        isGenerating = uiState.isGenerating,
                        ttsEnabled = uiState.ttsEnabled,
                        attachedDocName = uiState.attachedDocName,
                        studyContextText = uiState.studyContextText,
                        onSendQuery = { query -> viewModel.sendStudyQuery(query) },
                        onOpenSettings = { isConfigOpen = true },
                        onToggleTts = { viewModel.toggleTtsEnabled() },
                        onClearDoc = { viewModel.updateStudyContext("", null) }
                    )
                }
            }

            // Chat History Dialog
            ChatHistoryDialog(
                isOpen = isChatHistoryOpen,
                chatHistory = uiState.chatHistory,
                onDismiss = { isChatHistoryOpen = false },
                onClearHistory = { viewModel.clearChatHistory() }
            )

            // Configuration & Personality Bottom Sheet
            ConfigDrawerSheet(
                isOpen = isConfigOpen,
                apiKey = uiState.apiKey,
                selectedModel = uiState.selectedModel,
                activePersona = uiState.activePersona,
                customPersonaPrompt = uiState.customPersonaPrompt,
                characterName = uiState.characterName,
                studyContextText = uiState.studyContextText,
                attachedDocName = uiState.attachedDocName,
                customMoodDrawables = uiState.customMoodDrawables,
                ttsSpeed = uiState.ttsSpeed,
                ttsPitch = uiState.ttsPitch,
                ttsModelMetadata = uiState.ttsModelMetadata,
                onDismiss = { isConfigOpen = false },
                onUpdateApiKey = { viewModel.updateApiKey(it) },
                onUpdateModel = { viewModel.updateSelectedModel(it) },
                onSelectPersona = { viewModel.selectPersona(it) },
                onUpdateCustomPrompt = { viewModel.updateCustomPersonaPrompt(it) },
                onUpdateCharacterName = { viewModel.updateCharacterName(it) },
                onUpdateStudyContext = { text, name -> viewModel.updateStudyContext(text, name) },
                onSetMoodAsset = { mood, uri -> viewModel.setMoodAsset(mood, uri) },
                onImportTtsModel = { viewModel.importCustomTtsModel(it) },
                onUpdateTtsSpeed = { viewModel.updateTtsSpeed(it) },
                onUpdateTtsPitch = { viewModel.updateTtsPitch(it) },
                onTestTtsVoice = { viewModel.testTtsVoice() }
            )
        }
    }
}

@Composable
private fun TopHeaderBar(
    characterName: String,
    ttsEnabled: Boolean,
    onToggleTts: () -> Unit,
    onOpenChatHistory: () -> Unit,
    onOpenConfig: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .background(MangaBlack, RectangleShape)
            .border(1.5.dp, MangaWhite, RectangleShape)
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("top_header_bar")
    ) {
        Column {
            Text(
                text = "AI_STUDY_PARTNER",
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                color = MangaWhite,
                letterSpacing = 1.sp
            )
            Text(
                text = "UNIT: $characterName // FLASH-LITE",
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                color = MangaLightGray
            )
        }

        // Top Action Buttons: TTS Enable Toggle, Chat History, Settings
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Button at top to enable TTS
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(if (ttsEnabled) MangaWhite else MangaBlack, RectangleShape)
                    .border(1.5.dp, MangaWhite, RectangleShape)
                    .clickable { onToggleTts() }
                    .padding(horizontal = 8.dp, vertical = 5.dp)
                    .testTag("top_tts_enable_button")
            ) {
                Icon(
                    imageVector = if (ttsEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                    contentDescription = "Toggle TTS",
                    tint = if (ttsEnabled) MangaBlack else MangaLightGray,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (ttsEnabled) "TTS: ON" else "TTS: OFF",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = if (ttsEnabled) MangaBlack else MangaWhite
                )
            }

            // Chat History Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(MangaBlack, RectangleShape)
                    .border(1.5.dp, MangaWhite, RectangleShape)
                    .clickable { onOpenChatHistory() }
                    .padding(horizontal = 8.dp, vertical = 5.dp)
                    .testTag("top_chat_history_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Forum,
                    contentDescription = "Chat History",
                    tint = MangaWhite,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "LOG",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = MangaWhite
                )
            }

            // Settings Button
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(MangaBlack, RectangleShape)
                    .border(1.5.dp, MangaWhite, RectangleShape)
                    .clickable { onOpenConfig() }
                    .testTag("header_settings_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MangaWhite,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun QuickStudyPrompts(
    onSelectPrompt: (String) -> Unit
) {
    val prompts = listOf(
        "QUIZ // TOPICS",
        "EXPLAIN // ANALOGY",
        "SUMMARIZE // NOTES",
        "FORMULA // BREAKDOWN",
        "EXAM // 3_TIPS"
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 3.dp)
            .testTag("quick_prompts_row")
    ) {
        items(prompts) { prompt ->
            Box(
                modifier = Modifier
                    .background(MangaBlack, RectangleShape)
                    .border(1.dp, MangaWhite, RectangleShape)
                    .clickable { onSelectPrompt(prompt) }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "[ $prompt ]",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MangaWhite,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ErrorNotificationBanner(
    errorMessage: String,
    onFixApiKey: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .background(MangaDarkGray, RectangleShape)
            .border(1.5.dp, MangaWhite, RectangleShape)
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("error_banner")
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Warning",
            tint = MangaWhite,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = errorMessage,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = MangaWhite,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .background(MangaWhite, RectangleShape)
                .clickable { onFixApiKey() }
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = "SETUP_KEY",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                color = MangaBlack
            )
        }
    }
}
