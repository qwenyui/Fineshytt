package com.example.ui.components

import android.net.Uri
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Mood
import com.example.ui.theme.MangaBlack
import com.example.ui.theme.MangaDarkGray
import com.example.ui.theme.MangaLightGray
import com.example.ui.theme.MangaWhite

@Composable
fun CharacterDisplay(
    mood: Mood,
    customUri: Uri?,
    isSpeaking: Boolean,
    isThinking: Boolean,
    onPickImage: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "BlinkReticle")
    val reticleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = if (isThinking || isSpeaking) 1.0f else 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isThinking) 400 else 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ReticleAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag("character_display_box"),
        contentAlignment = Alignment.Center
    ) {
        // Outer Cyberpunk Manga Panel Border (90-Degree Sharp Corners)
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .height(300.dp)
                .background(MangaBlack, RectangleShape)
                .border(2.dp, MangaWhite, RectangleShape)
                .padding(4.dp)
        ) {
            // Inner Manga Panel Line
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(1.dp, MangaDarkGray, RectangleShape)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                // Technical Grid Crosshairs in Corners
                CornerCrosshairs()

                Crossfade(
                    targetState = Pair(mood, customUri),
                    animationSpec = tween(200),
                    label = "MoodCrossfade"
                ) { (currentMood, uri) ->
                    if (uri != null) {
                        AsyncImage(
                            model = uri,
                            contentDescription = "Avatar [${currentMood.displayName}]",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable(enabled = onPickImage != null) { onPickImage?.invoke() }
                                .testTag("custom_avatar_image")
                        )
                    } else {
                        // Empty Manga Panel Viewport
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable(enabled = onPickImage != null) { onPickImage?.invoke() }
                                .testTag("empty_avatar_placeholder"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                // Square Reticle
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .background(MangaBlack, RectangleShape)
                                        .border(2.dp, MangaWhite.copy(alpha = reticleAlpha), RectangleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add Avatar",
                                        tint = MangaWhite,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = "[ ${currentMood.iconEmoji} ${currentMood.displayName} // NO_ASSET ]",
                                    color = MangaWhite,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    letterSpacing = 1.sp
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "SLOT IS EMPTY. TAP TO LOAD IMAGE",
                                    color = MangaLightGray,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.5.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // Top-Left Panel Label
            Text(
                text = "PANEL_01 // VIEWPORT",
                color = MangaWhite,
                backgroundColor = MangaBlack,
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 10.dp, top = 2.dp)
            )

            // Top-Right Mood Chip (Sharp 90-degree rectangle)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 6.dp, top = 6.dp)
                    .background(MangaBlack, RectangleShape)
                    .border(1.5.dp, MangaWhite, RectangleShape)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
                    .testTag("active_mood_badge")
            ) {
                Text(
                    text = "${mood.iconEmoji} ${mood.displayName}",
                    color = MangaWhite,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
private fun CornerCrosshairs() {
    Box(modifier = Modifier.fillMaxSize()) {
        Text("+", color = MangaLightGray, fontFamily = FontFamily.Monospace, fontSize = 12.sp, modifier = Modifier.align(Alignment.TopStart))
        Text("+", color = MangaLightGray, fontFamily = FontFamily.Monospace, fontSize = 12.sp, modifier = Modifier.align(Alignment.TopEnd))
        Text("+", color = MangaLightGray, fontFamily = FontFamily.Monospace, fontSize = 12.sp, modifier = Modifier.align(Alignment.BottomStart))
        Text("+", color = MangaLightGray, fontFamily = FontFamily.Monospace, fontSize = 12.sp, modifier = Modifier.align(Alignment.BottomEnd))
    }
}

@Composable
private fun Text(
    text: String,
    color: Color,
    fontFamily: FontFamily,
    fontSize: androidx.compose.ui.unit.TextUnit,
    fontWeight: FontWeight? = null,
    letterSpacing: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified,
    textAlign: TextAlign? = null,
    backgroundColor: Color = Color.Transparent,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Text(
        text = text,
        color = color,
        fontFamily = fontFamily,
        fontSize = fontSize,
        fontWeight = fontWeight,
        letterSpacing = letterSpacing,
        textAlign = textAlign,
        modifier = modifier.background(backgroundColor)
    )
}
