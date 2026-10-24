package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Mood
import com.example.data.model.PersonaPreset
import com.example.data.service.GeminiStudyService
import com.example.tts.TtsModelMetadata
import com.example.ui.theme.MangaBlack
import com.example.ui.theme.MangaBorderGray
import com.example.ui.theme.MangaDarkGray
import com.example.ui.theme.MangaLightGray
import com.example.ui.theme.MangaMediumGray
import com.example.ui.theme.MangaWhite

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ConfigDrawerSheet(
    isOpen: Boolean,
    apiKey: String,
    selectedModel: String,
    activePersona: PersonaPreset,
    customPersonaPrompt: String,
    characterName: String,
    studyContextText: String,
    attachedDocName: String?,
    customMoodDrawables: Map<Mood, Uri?>,
    ttsSpeed: Float,
    ttsPitch: Float,
    ttsModelMetadata: TtsModelMetadata?,
    onDismiss: () -> Unit,
    onUpdateApiKey: (String) -> Unit,
    onUpdateModel: (String) -> Unit,
    onSelectPersona: (PersonaPreset) -> Unit,
    onUpdateCustomPrompt: (String) -> Unit,
    onUpdateCharacterName: (String) -> Unit,
    onUpdateStudyContext: (String, String?) -> Unit,
    onSetMoodAsset: (Mood, Uri?) -> Unit,
    onImportTtsModel: (Uri) -> Unit,
    onUpdateTtsSpeed: (Float) -> Unit,
    onUpdateTtsPitch: (Float) -> Unit,
    onTestTtsVoice: () -> Unit
) {
    if (!isOpen) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    var isApiKeyMasked by remember { mutableStateOf(true) }
    var targetMoodToPick by remember { mutableStateOf<Mood?>(null) }

    val docPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { docUri ->
            try {
                val inputStream = context.contentResolver.openInputStream(docUri)
                val text = inputStream?.bufferedReader()?.use { it.readText() }.orEmpty()
                val cursor = context.contentResolver.query(docUri, null, null, null, null)
                var displayName = "DOCUMENT"
                cursor?.use {
                    if (it.moveToFirst()) {
                        val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) displayName = it.getString(nameIndex)
                    }
                }
                onUpdateStudyContext(text, displayName)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val moodImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        targetMoodToPick?.let { mood ->
            onSetMoodAsset(mood, uri)
        }
    }

    val ttsModelPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImportTtsModel(it) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MangaBlack,
        shape = RectangleShape,
        dragHandle = null,
        modifier = Modifier.testTag("config_modal_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.94f)
                .background(MangaBlack, RectangleShape)
                .border(2.dp, MangaWhite, RectangleShape)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header (90-Degree Monochrome Cyberpunk)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MangaWhite, RectangleShape)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "[ SYS // CONFIG_STUDIO ]",
                    fontSize = 15.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    color = MangaBlack,
                    letterSpacing = 1.sp
                )

                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .background(MangaBlack, RectangleShape)
                        .clickable { onDismiss() }
                        .testTag("close_config_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MangaWhite,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. GEMINI API KEY & MODEL
            CyberSectionTitle("01 // GEMINI_API_INTERFACE", Icons.Default.Key)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = apiKey,
                onValueChange = onUpdateApiKey,
                label = { Text("GEMINI_API_KEY", fontFamily = FontFamily.Monospace, fontSize = 11.sp) },
                placeholder = { Text("AIzaSy...", color = MangaLightGray, fontFamily = FontFamily.Monospace) },
                singleLine = true,
                visualTransformation = if (isApiKeyMasked) PasswordVisualTransformation() else VisualTransformation.None,
                trailingIcon = {
                    IconButton(onClick = { isApiKeyMasked = !isApiKeyMasked }) {
                        Icon(
                            imageVector = if (isApiKeyMasked) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle Visibility",
                            tint = MangaWhite
                        )
                    }
                },
                colors = cyberpunkTextFieldColors(),
                shape = RectangleShape,
                modifier = Modifier.fillMaxWidth().testTag("api_key_field")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Model Selection (Sharp 90° Chips)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("MODEL:", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = MangaLightGray, fontWeight = FontWeight.Bold)

                CyberSquareChip(
                    text = "3.1-FLASH-LITE",
                    selected = selectedModel == GeminiStudyService.DEFAULT_MODEL,
                    onClick = { onUpdateModel(GeminiStudyService.DEFAULT_MODEL) },
                    testTag = "model_flash_lite_chip"
                )

                CyberSquareChip(
                    text = "3.5-FLASH",
                    selected = selectedModel == GeminiStudyService.BACKUP_MODEL,
                    onClick = { onUpdateModel(GeminiStudyService.BACKUP_MODEL) },
                    testTag = "model_flash_chip"
                )
            }

            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider(color = MangaBorderGray, thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            // 2. PERSONA & SYSTEM INSTRUCTIONS
            CyberSectionTitle("02 // PERSONA_AND_TRAITS", Icons.Default.Psychology)
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                PersonaPreset.PRESETS.forEach { preset ->
                    CyberSquareChip(
                        text = preset.characterName.uppercase(),
                        selected = activePersona.id == preset.id,
                        onClick = { onSelectPersona(preset) },
                        testTag = "persona_chip_${preset.id}"
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = characterName,
                onValueChange = onUpdateCharacterName,
                label = { Text("OPERATOR_NAME", fontFamily = FontFamily.Monospace, fontSize = 11.sp) },
                singleLine = true,
                colors = cyberpunkTextFieldColors(),
                shape = RectangleShape,
                modifier = Modifier.fillMaxWidth().testTag("character_name_input")
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = customPersonaPrompt,
                onValueChange = onUpdateCustomPrompt,
                label = { Text("SYSTEM_INSTRUCTIONS // PROMPT", fontFamily = FontFamily.Monospace, fontSize = 11.sp) },
                minLines = 4,
                maxLines = 8,
                colors = cyberpunkTextFieldColors(),
                shape = RectangleShape,
                modifier = Modifier.fillMaxWidth().testTag("persona_prompt_textarea")
            )

            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider(color = MangaBorderGray, thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            // 3. STUDY CONTEXT
            CyberSectionTitle("03 // STUDY_DOCUMENT_CONTEXT", Icons.Default.Description)
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CyberSquareButton(
                    text = "ATTACH FILE",
                    icon = Icons.Default.FileUpload,
                    onClick = { docPickerLauncher.launch("*/*") },
                    testTag = "attach_document_button"
                )

                if (studyContextText.isNotBlank()) {
                    CyberSquareButton(
                        text = "CLEAR CONTEXT",
                        icon = Icons.Default.Close,
                        onClick = { onUpdateStudyContext("", null) },
                        testTag = "clear_context_button"
                    )
                }
            }

            if (attachedDocName != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "LOADED: $attachedDocName (${studyContextText.length} bytes)",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = MangaWhite,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = studyContextText,
                onValueChange = { onUpdateStudyContext(it, attachedDocName) },
                label = { Text("RAW_STUDY_MATERIAL", fontFamily = FontFamily.Monospace, fontSize = 11.sp) },
                placeholder = { Text("Paste notes, textbook text, formula tables...", color = MangaLightGray, fontFamily = FontFamily.Monospace) },
                minLines = 3,
                maxLines = 6,
                colors = cyberpunkTextFieldColors(),
                shape = RectangleShape,
                modifier = Modifier.fillMaxWidth().testTag("study_context_textarea")
            )

            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider(color = MangaBorderGray, thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            // 4. MOOD ASSET MAPPING
            CyberSectionTitle("04 // MOOD_ASSET_MAPPING (EMPTY SLOTS)", Icons.Default.Psychology)
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Assign your custom images into each mood slot. Slots remain empty until you load an asset.",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = MangaLightGray
            )

            Spacer(modifier = Modifier.height(10.dp))

            Mood.entries.forEach { mood ->
                CyberMoodRow(
                    mood = mood,
                    customUri = customMoodDrawables[mood],
                    onPick = {
                        targetMoodToPick = mood
                        moodImagePickerLauncher.launch("image/*")
                    },
                    onClear = { onSetMoodAsset(mood, null) }
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider(color = MangaBorderGray, thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            // 5. OFFLINE TTS & ONNX MODEL PIPELINE
            CyberSectionTitle("05 // OFFLINE_TTS_ENGINE", Icons.Default.GraphicEq)
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MangaDarkGray, RectangleShape)
                    .border(1.5.dp, MangaWhite, RectangleShape)
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "MODEL // ${ttsModelMetadata?.modelName ?: "Airi-Anime-Acoustic-V1"}",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        color = MangaWhite,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "FORMAT: ${ttsModelMetadata?.format ?: "ONNX Acoustic"} • 22050Hz • OFFLINE ONLY",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = MangaLightGray
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CyberSquareButton(
                            text = "IMPORT .ONNX/.JSON",
                            icon = Icons.Default.FileUpload,
                            onClick = { ttsModelPickerLauncher.launch("*/*") },
                            testTag = "import_custom_tts_button"
                        )

                        CyberSquareButton(
                            text = "TEST VOICE",
                            icon = Icons.Default.PlayArrow,
                            onClick = onTestTtsVoice,
                            testTag = "test_tts_voice_button"
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "SPEED FACTOR: ${String.format("%.2f", ttsSpeed)}x",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = MangaWhite
                    )
                    Slider(
                        value = ttsSpeed,
                        onValueChange = onUpdateTtsSpeed,
                        valueRange = 0.8f..1.5f,
                        colors = SliderDefaults.colors(thumbColor = MangaWhite, activeTrackColor = MangaWhite, inactiveTrackColor = MangaMediumGray),
                        modifier = Modifier.fillMaxWidth().testTag("tts_speed_slider")
                    )

                    Text(
                        text = "PITCH FACTOR: ${String.format("%.2f", ttsPitch)}x",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = MangaWhite
                    )
                    Slider(
                        value = ttsPitch,
                        onValueChange = onUpdateTtsPitch,
                        valueRange = 0.8f..1.4f,
                        colors = SliderDefaults.colors(thumbColor = MangaWhite, activeTrackColor = MangaWhite, inactiveTrackColor = MangaMediumGray),
                        modifier = Modifier.fillMaxWidth().testTag("tts_pitch_slider")
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun CyberSectionTitle(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = MangaWhite, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            color = MangaWhite,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun CyberSquareChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        modifier = Modifier
            .background(if (selected) MangaWhite else MangaBlack, RectangleShape)
            .border(1.5.dp, MangaWhite, RectangleShape)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag(testTag)
    ) {
        Text(
            text = text,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) MangaBlack else MangaWhite
        )
    }
}

@Composable
private fun CyberSquareButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(MangaBlack, RectangleShape)
            .border(1.5.dp, MangaWhite, RectangleShape)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag(testTag)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MangaWhite, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MangaWhite
        )
    }
}

@Composable
private fun CyberMoodRow(
    mood: Mood,
    customUri: Uri?,
    onPick: () -> Unit,
    onClear: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .background(MangaDarkGray, RectangleShape)
            .border(1.dp, MangaWhite, RectangleShape)
            .padding(8.dp)
            .testTag("mood_row_${mood.name.lowercase()}")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MangaBlack, RectangleShape)
                    .border(1.dp, MangaWhite, RectangleShape),
                contentAlignment = Alignment.Center
            ) {
                if (customUri != null) {
                    AsyncImage(
                        model = customUri,
                        contentDescription = mood.displayName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text("+", color = MangaLightGray, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = "${mood.iconEmoji} ${mood.tag}",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    color = MangaWhite,
                    fontSize = 12.sp
                )
                Text(
                    text = if (customUri != null) "ASSIGNED" else "EMPTY SLOT",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = MangaLightGray
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .background(MangaBlack, RectangleShape)
                    .border(1.dp, MangaWhite, RectangleShape)
                    .clickable { onPick() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .testTag("pick_image_${mood.name.lowercase()}")
            ) {
                Text(
                    text = if (customUri != null) "SWAP" else "ASSIGN",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MangaWhite
                )
            }

            if (customUri != null) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(MangaBlack, RectangleShape)
                        .border(1.dp, MangaWhite, RectangleShape)
                        .clickable { onClear() }
                        .testTag("reset_image_${mood.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = MangaWhite, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@Composable
private fun cyberpunkTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MangaBlack,
    unfocusedContainerColor = MangaBlack,
    focusedTextColor = MangaWhite,
    unfocusedTextColor = MangaWhite,
    focusedBorderColor = MangaWhite,
    unfocusedBorderColor = MangaBorderGray,
    focusedLabelColor = MangaWhite,
    unfocusedLabelColor = MangaLightGray
)
