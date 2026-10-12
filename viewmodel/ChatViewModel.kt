package com.pokke.music.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pokke.music.data.Conversation
import com.pokke.music.data.Message
import com.pokke.music.data.SocialRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatUiState(
    val conversations: List<Conversation> = emptyList(),
    val currentMessages: List<Message> = emptyList(),
    val currentConversationId: String? = null,
    val isLoading: Boolean = false,
    val messageText: String = "",
    val error: String? = null,
)

class ChatViewModel(
    private val repository: SocialRepository = SocialRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun loadConversations(nickname: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val convs = repository.getConversationsForUser(nickname)
            _uiState.value = _uiState.value.copy(
                conversations = convs,
                isLoading = false
            )
        }
    }

    fun openConversation(conversationId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                currentConversationId = conversationId,
                isLoading = true
            )
            val messages = repository.getMessages(conversationId)
            _uiState.value = _uiState.value.copy(
                currentMessages = messages,
                isLoading = false
            )
        }
    }

    fun onMessageTextChange(text: String) {
        _uiState.value = _uiState.value.copy(messageText = text)
    }

    fun sendMessage(myNickname: String) {
        val convId = _uiState.value.currentConversationId ?: return
        val body = _uiState.value.messageText.trim()
        if (body.isEmpty()) return

        viewModelScope.launch {
            val ok = repository.sendMessage(convId, myNickname, body)
            if (ok) {
                _uiState.value = _uiState.value.copy(messageText = "")
                openConversation(convId) // refresh
            }
        }
    }

    fun startChatWith(myNickname: String, otherNickname: String) {
        viewModelScope.launch {
            val convId = repository.createDirectConversation(myNickname, otherNickname)
            if (convId != null) {
                openConversation(convId)
            }
        }
    }

    fun closeConversation() {
        _uiState.value = _uiState.value.copy(
            currentConversationId = null,
            currentMessages = emptyList()
        )
    }
}
