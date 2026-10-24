package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.MoodHappyColor
import com.example.ui.theme.MoodNeutralColor
import com.example.ui.theme.MoodSadColor
import com.example.ui.theme.MoodSurprisedColor
import com.example.ui.theme.MoodThinkingColor

enum class Mood(
    val tag: String,
    val displayName: String,
    val iconEmoji: String,
    val color: Color,
    val pitchMultiplier: Float,
    val speedMultiplier: Float
) {
    HAPPY(
        tag = "[MOOD: HAPPY]",
        displayName = "HAPPY",
        iconEmoji = "▲",
        color = MoodHappyColor,
        pitchMultiplier = 1.15f,
        speedMultiplier = 1.1f
    ),
    THINKING(
        tag = "[MOOD: THINKING]",
        displayName = "THINKING",
        iconEmoji = "■",
        color = MoodThinkingColor,
        pitchMultiplier = 0.95f,
        speedMultiplier = 0.9f
    ),
    NEUTRAL(
        tag = "[MOOD: NEUTRAL]",
        displayName = "NEUTRAL",
        iconEmoji = "●",
        color = MoodNeutralColor,
        pitchMultiplier = 1.0f,
        speedMultiplier = 1.0f
    ),
    SURPRISED(
        tag = "[MOOD: SURPRISED]",
        displayName = "SURPRISED",
        iconEmoji = "◆",
        color = MoodSurprisedColor,
        pitchMultiplier = 1.25f,
        speedMultiplier = 1.15f
    ),
    SAD(
        tag = "[MOOD: SAD]",
        displayName = "SAD",
        iconEmoji = "▼",
        color = MoodSadColor,
        pitchMultiplier = 0.88f,
        speedMultiplier = 0.85f
    );

    companion object {
        fun fromTagOrName(value: String): Mood {
            val normalized = value.trim().uppercase()
            return entries.firstOrNull { mood ->
                normalized == mood.name ||
                normalized == mood.tag ||
                normalized.contains(mood.name)
            } ?: NEUTRAL
        }
    }
}
