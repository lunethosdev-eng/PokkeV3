package com.pokke.music.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pokke.music.data.Profile
import com.pokke.music.data.SocialRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val profile: Profile? = null,
    val isLoading: Boolean = false,
    val isFollowing: Boolean = false,
    val followers: List<Profile> = emptyList(),
    val following: List<Profile> = emptyList(),
    val error: String? = null,
    val isEditing: Boolean = false,
)

class ProfileViewModel(
    private val repository: SocialRepository = SocialRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun loadProfile(userId: String, currentUserId: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val profile = repository.getProfile(userId)
            val isFollowing = if (currentUserId != null && currentUserId != userId) {
                repository.isFollowing(currentUserId, userId)
            } else false
            val followers = repository.getFollowers(userId)
            val following = repository.getFollowing(userId)
            _uiState.value = _uiState.value.copy(
                profile = profile,
                isLoading = false,
                isFollowing = isFollowing,
                followers = followers,
                following = following,
                error = if (profile == null) "No se pudo cargar el perfil" else null
            )
        }
    }

    fun toggleFollow(currentUserId: String, targetUserId: String) {
        viewModelScope.launch {
            val currentlyFollowing = _uiState.value.isFollowing
            val success = if (currentlyFollowing) {
                repository.unfollow(currentUserId, targetUserId)
            } else {
                repository.follow(currentUserId, targetUserId)
            }
            if (success) {
                _uiState.value = _uiState.value.copy(isFollowing = !currentlyFollowing)
                // Refresh counts
                loadProfile(targetUserId, currentUserId)
            }
        }
    }

    fun updateBio(userId: String, bio: String) {
        viewModelScope.launch {
            val ok = repository.updateProfile(userId, bio = bio)
            if (ok) loadProfile(userId)
        }
    }

    fun updateDisplayName(userId: String, name: String) {
        viewModelScope.launch {
            val ok = repository.updateProfile(userId, displayName = name)
            if (ok) loadProfile(userId)
        }
    }

    fun setEditing(editing: Boolean) {
        _uiState.value = _uiState.value.copy(isEditing = editing)
    }
}
