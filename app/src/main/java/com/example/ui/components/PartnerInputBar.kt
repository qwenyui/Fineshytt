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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MangaBlack
import com.example.ui.theme.MangaBorderGray
import com.example.ui.theme.MangaDarkGray
import com.example.ui.theme.MangaLightGray
import com.example.ui.theme.MangaWhite

@Composable
fun PartnerInputBar(
    isGenerating: Boolean,
    ttsEnabled: Boolean,
    attachedDocName: String?,
    studyContextText: String,
    onSendQuery: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onToggleTts: () -> Unit,
    onClearDoc: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag("partner_input_bar")
    ) {
        // Study Context Badge (90-Degree Sharp Box)
        if (studyContextText.isNotBlank()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                    .background(MangaDarkGray, RectangleShape)
                    .border(1.dp, MangaWhite, RectangleShape)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = "Study Context",
                        tint = MangaWhite,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "DOC // ${attachedDocName ?: "RAW_CONTEXT (${studyContextText.length} CHARS)"}",
                        color = MangaWhite,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Clear Context",
                    tint = MangaLightGray,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { onClearDoc() }
                )
            }
        }

        // Main Input Bar Row (90-Degree Sharp Elements)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Settings Button (90-degree square)
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(MangaBlack, RectangleShape)
                    .border(1.5.dp, MangaWhite, RectangleShape)
                    .clickable { onOpenSettings() }
                    .testTag("settings_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MangaWhite,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // TTS Toggle Button (90-degree square)
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(if (ttsEnabled) MangaWhite else MangaBlack, RectangleShape)
                    .border(1.5.dp, MangaWhite, RectangleShape)
                    .clickable { onToggleTts() }
                    .testTag("tts_toggle_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (ttsEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                    contentDescription = if (ttsEnabled) "Voice Speech Enabled" else "Voice Speech Muted",
                    tint = if (ttsEnabled) MangaBlack else MangaLightGray,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 90-Degree Sharp Input Field
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = {
                    Text(
                        text = "INPUT_QUERY >>",
                        color = MangaLightGray,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    )
                },
                singleLine = true,
                maxLines = 1,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MangaBlack,
                    unfocusedContainerColor = MangaBlack,
                    focusedTextColor = MangaWhite,
                    unfocusedTextColor = MangaWhite,
                    focusedBorderColor = MangaWhite,
                    unfocusedBorderColor = MangaBorderGray
                ),
                shape = RectangleShape,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("study_query_input")
            )

            Spacer(modifier = Modifier.width(8.dp))

            // 90-Degree Sharp Send Button
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(
                        if (inputText.isNotBlank() && !isGenerating) MangaWhite
                        else MangaDarkGray,
                        RectangleShape
                    )
                    .border(1.5.dp, MangaWhite, RectangleShape)
                    .clickable(enabled = inputText.isNotBlank() && !isGenerating) {
                        val queryToSend = inputText
                        inputText = ""
                        onSendQuery(queryToSend)
                    }
                    .testTag("send_query_button"),
                contentAlignment = Alignment.Center
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MangaWhite
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (inputText.isNotBlank()) MangaBlack else MangaLightGray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
