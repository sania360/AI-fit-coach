package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.FitViewModel
import com.example.data.ChatMessage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AICoachChatScreen(viewModel: FitViewModel) {
    val chatHistory by viewModel.chatHistory.collectAsStateWithLifecycle()
    val isSending by viewModel.isSendingChat.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    var messageText by remember { mutableStateOf("") }

    // Quick suggestion options
    val suggestions = listOf(
        "Can I eat pizza?",
        "How much protein do I need?",
        "I missed today's workout.",
        "Can I replace chicken with eggs?",
        "What exercises reduce belly fat?"
    )

    // Scroll to bottom on new messages
    LaunchedEffect(chatHistory.size) {
        if (chatHistory.isNotEmpty()) {
            listState.animateScrollToItem(chatHistory.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Coach FitAI Chat", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { viewModel.clearChat() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Clear Chat", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Chat history list
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                }

                items(chatHistory) { message ->
                    ChatBubble(message)
                }

                if (isSending) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Coach FitAI is typing...", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                        }
                    }
                }
            }

            // Quick suggestions chips scrollable container
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            suggestions.forEach { suggestion ->
                                Card(
                                    modifier = Modifier
                                        .clickable {
                                            viewModel.sendChatMessage(suggestion)
                                        },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = suggestion,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Input bar
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = { Text("Ask anything about diet or workouts...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field"),
                        maxLines = 3,
                        shape = RoundedCornerShape(24.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    FloatingActionButton(
                        onClick = {
                            if (messageText.trim().isNotEmpty()) {
                                viewModel.sendChatMessage(messageText)
                                messageText = ""
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("chat_send_btn"),
                        shape = RoundedCornerShape(24.dp),
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val isUser = message.sender == "user"
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        )
                    )
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = if (isUser) "You" else "Coach FitAI",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (isUser) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    if (isUser) {
                        Text(
                            text = message.text,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        // Render formatted Markdown for Coach answers!
                        MarkdownText(message.text)
                    }
                }
            }
        }
    }
}
