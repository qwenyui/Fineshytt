package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MangaBlack
import com.example.ui.theme.MangaBorderGray
import com.example.ui.theme.MangaDarkGray
import com.example.ui.theme.MangaLightGray
import com.example.ui.theme.MangaWhite
import com.example.ui.viewmodel.ChatMessage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatHistoryDialog(
    isOpen: Boolean,
    chatHistory: List<ChatMessage>,
    onDismiss: () -> Unit,
    onClearHistory: () -> Unit
) {
    if (!isOpen) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val listState = rememberLazyListState()
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MangaBlack,
        shape = RectangleShape,
        dragHandle = null,
        modifier = Modifier.testTag("chat_history_modal")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .background(MangaBlack, RectangleShape)
                .border(2.dp, MangaWhite, RectangleShape)
                .padding(16.dp)
        ) {
            // Header (90° Sharp Rectangle)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MangaWhite, RectangleShape)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "[ SYS // CHAT_HISTORY_LOG ]",
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    color = MangaBlack,
                    letterSpacing = 1.sp
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (chatHistory.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .background(MangaBlack, RectangleShape)
                                .border(1.dp, MangaWhite, RectangleShape)
                                .clickable { onClearHistory() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("clear_chat_history_button")
                        ) {
                            Text(
                                text = "CLEAR",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MangaWhite
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .background(MangaBlack, RectangleShape)
                            .clickable { onDismiss() }
                            .testTag("close_chat_history_button"),
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
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MangaBorderGray, thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            if (chatHistory.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "NO_RECORDS // LOG IS EMPTY",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = MangaLightGray
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(chatHistory, key = { it.id }) { msg ->
                        val isUser = msg.sender == "USER"
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isUser) MangaDarkGray else MangaBlack, RectangleShape)
                                .border(1.dp, if (isUser) MangaLightGray else MangaWhite, RectangleShape)
                                .padding(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (isUser) ">> USER" else "<< ${msg.sender.uppercase()} ${msg.mood?.let { "[${it.name}]" } ?: ""}",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp,
                                    color = if (isUser) MangaLightGray else MangaWhite
                                )

                                Text(
                                    text = timeFormat.format(Date(msg.timestamp)),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = MangaLightGray
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = msg.text,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                color = MangaWhite
                            )
                        }
                    }
                }
            }
        }
    }
}
