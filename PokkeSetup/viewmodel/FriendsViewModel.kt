package com.pokke.music.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pokke.music.data.Profile
import com.pokke.music.data.SocialRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FriendsUiState(
    val followers: List<Profile> = emptyList(),
    val following: List<Profile> = emptyList(),
    val searchResults: List<Profile> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val selectedTab: Int = 0, // 0 = following, 1 = followers, 2 = search
    val error: String? = null,
)

class FriendsViewModel(
    private val repository: SocialRepository = SocialRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(FriendsUiState())
    val uiState: StateFlow<FriendsUiState> = _uiState.asStateFlow()

    fun loadFriends(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val followers = repository.getFollowers(userId)
            val following = repository.getFollowing(userId)
            _uiState.value = _uiState.value.copy(
                followers = followers,
                following = following,
                isLoading = false
            )
        }
    }

    fun search(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        if (query.length < 2) {
            _uiState.value = _uiState.value.copy(searchResults = emptyList())
            return
        }
        viewModelScope.launch {
            val results = repository.searchProfiles(query)
            _uiState.value = _uiState.value.copy(searchResults = results)
        }
    }

    fun setTab(tab: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun follow(currentUserId: String, targetId: String) {
        viewModelScope.launch {
            repository.follow(currentUserId, targetId)
            loadFriends(currentUserId)
        }
    }

    fun unfollow(currentUserId: String, targetId: String) {
        viewModelScope.launch {
            repository.unfollow(currentUserId, targetId)
            loadFriends(currentUserId)
        }
    }
}
