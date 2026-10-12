package com.pokke.music.ui.screen.social

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pokke.music.data.Conversation
import com.pokke.music.data.Message
import com.pokke.music.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    myNickname: String,
    viewModel: ChatViewModel,
    onBack: () -> Unit,
    onOpenConversation: (String) -> Unit,
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(myNickname) {
        viewModel.loadConversations(myNickname)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mensajes") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        if (state.conversations.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(
                    "No tienes conversaciones aún.\nVe a un perfil y pulsa \"Mensaje\".",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return@Scaffold
        }

        LazyColumn(Modifier.padding(padding)) {
            items(state.conversations, key = { it.id }) { conv ->
                ListItem(
                    headlineContent = { Text(conv.title ?: "Chat") },
                    supportingContent = {
                        Text(
                            "Última actualización: ${conv.updatedAt?.take(16) ?: ""}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    modifier = Modifier.clickableCompat { onOpenConversation(conv.id) }
                )
                HorizontalDivider()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatConversationScreen(
    conversationId: String,
    myNickname: String,
    viewModel: ChatViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(conversationId) {
        viewModel.openConversation(conversationId)
    }

    // Auto-scroll al final cuando llegan mensajes nuevos
    LaunchedEffect(state.currentMessages.size) {
        if (state.currentMessages.isNotEmpty()) {
            listState.animateScrollToItem(state.currentMessages.lastIndex)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.closeConversation()
                        onBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        bottomBar = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = state.messageText,
                    onValueChange = { viewModel.onMessageTextChange(it) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Escribe un mensaje...") },
                    maxLines = 4
                )
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = { viewModel.sendMessage(myNickname) },
                    enabled = state.messageText.isNotBlank()
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Enviar")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(state.currentMessages, key = { it.id }) { msg ->
                MessageBubble(
                    message = msg,
                    isMine = msg.senderNickname == myNickname
                )
            }
        }
    }
}

@Composable
private fun MessageBubble(message: Message, isMine: Boolean) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isMine) 16.dp else 4.dp,
                bottomEnd = if (isMine) 4.dp else 16.dp
            ),
            color = if (isMine)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                if (!isMine) {
                    Text(
                        message.senderNickname,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    message.body,
                    color = if (isMine)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Helper para clickable sin importar foundation.clickable si hay conflicto
@Composable
private fun Modifier.clickableCompat(onClick: () -> Unit): Modifier {
    return this.then(
        Modifier.then(
            androidx.compose.foundation.clickable(onClick = onClick)
        )
    )
}
