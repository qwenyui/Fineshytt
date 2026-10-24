package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Mood
import com.example.tts.TtsPlaybackStatus
import com.example.ui.theme.MangaBlack
import com.example.ui.theme.MangaBorderGray
import com.example.ui.theme.MangaDarkGray
import com.example.ui.theme.MangaLightGray
import com.example.ui.theme.MangaMediumGray
import com.example.ui.theme.MangaWhite

@Composable
fun VisualNovelDialogueBox(
    speakerName: String,
    dialogueText: String,
    mood: Mood,
    ttsEnabled: Boolean,
    ttsStatus: TtsPlaybackStatus,
    onTogglePlayPause: () -> Unit,
    onReplayAudio: () -> Unit,
    onStopAudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .testTag("visual_novel_dialogue_box")
    ) {
        // Speaker Header Badge (90° Sharp Box)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(MangaBlack, RectangleShape)
                .border(2.dp, MangaWhite, RectangleShape)
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .testTag("speaker_nameplate")
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(MangaWhite, RectangleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "[ UNIT // ${speakerName.uppercase()} ]",
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = MangaWhite,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = if (ttsEnabled) "[ MODE: SUBTITLES ]" else "[ MODE: FULL_TEXT ]",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = MangaLightGray
            )

            if (ttsEnabled && ttsStatus.isPlaying) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "▶ VOICE_ACTIVE",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MangaWhite
                )
            }
        }

        // Main Frame (90° Sharp Box, Pure Black Background)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MangaBlack, RectangleShape)
                .border(2.dp, MangaWhite, RectangleShape)
                .padding(10.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (ttsEnabled) {
                    // WHEN TTS IS ENABLED: Main text is gone, subtitles displayed instead
                    val currentSubtitle = if (ttsStatus.activeSentenceText.isNotBlank()) {
                        ttsStatus.activeSentenceText
                    } else if (dialogueText.isNotBlank()) {
                        dialogueText.lines().firstOrNull()?.take(140) ?: "..."
                    } else {
                        "..."
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 60.dp, max = 110.dp)
                            .background(MangaDarkGray, RectangleShape)
                            .border(1.dp, MangaWhite, RectangleShape)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "// SUBTITLE //",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MangaLightGray,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "\"$currentSubtitle\"",
                            color = MangaWhite,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp,
                            modifier = Modifier.testTag("subtitle_text")
                        )
                    }

                    // TTS Subtitle Queue & Audio Controls
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Voice Active",
                                tint = MangaWhite,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (ttsStatus.totalSentences > 0) {
                                    "SUBTITLE: ${ttsStatus.currentSentenceIndex + 1}/${ttsStatus.totalSentences}"
                                } else {
                                    "TTS ENGINE ARMED"
                                },
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = MangaWhite,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Playback Controls
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            SquareButton(
                                onClick = onTogglePlayPause,
                                icon = if (ttsStatus.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                desc = if (ttsStatus.isPlaying) "Pause" else "Play",
                                testTag = "toggle_play_pause_button"
                            )
                            SquareButton(
                                onClick = onReplayAudio,
                                icon = Icons.Default.Replay,
                                desc = "Replay",
                                testTag = "replay_audio_button"
                            )
                            SquareButton(
                                onClick = onStopAudio,
                                icon = Icons.Default.Stop,
                                desc = "Stop",
                                testTag = "stop_audio_button"
                            )
                        }
                    }

                    // Progress bar
                    if (ttsStatus.totalSentences > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        val progressFraction = ((ttsStatus.currentSentenceIndex + (if (ttsStatus.isPlaying) 1f else 0f)) / ttsStatus.totalSentences).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { progressFraction },
                            color = MangaWhite,
                            trackColor = MangaMediumGray,
                            modifier = Modifier.fillMaxWidth().height(2.dp)
                        )
                    }
                } else {
                    // WHEN TTS IS DISABLED: SCROLLABLE MAIN TEXT AT BOTTOM
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 60.dp, max = 130.dp)
                            .verticalScroll(scrollState)
                    ) {
                        Text(
                            text = dialogueText,
                            color = MangaWhite,
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Normal,
                            letterSpacing = 0.3.sp,
                            modifier = Modifier.testTag("dialogue_text")
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SquareButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    desc: String,
    testTag: String
) {
    Box(
        modifier = Modifier
            .size(26.dp)
            .background(MangaBlack, RectangleShape)
            .border(1.dp, MangaWhite, RectangleShape)
            .clickable { onClick() }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = desc,
            tint = MangaWhite,
            modifier = Modifier.size(14.dp)
        )
    }
}
