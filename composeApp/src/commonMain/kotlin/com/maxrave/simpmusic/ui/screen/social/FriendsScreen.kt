package com.pokke.music.ui.screen.social

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.pokke.music.data.Profile
import com.pokke.music.viewmodel.FriendsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    userId: String,
    currentUserId: String?,
    viewModel: FriendsViewModel,
    onBack: () -> Unit,
    onOpenProfile: (String) -> Unit,
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(userId) {
        viewModel.loadFriends(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Amigos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // Tabs
            TabRow(selectedTabIndex = state.selectedTab) {
                Tab(
                    selected = state.selectedTab == 0,
                    onClick = { viewModel.setTab(0) },
                    text = { Text("Siguiendo (${state.following.size})") }
                )
                Tab(
                    selected = state.selectedTab == 1,
                    onClick = { viewModel.setTab(1) },
                    text = { Text("Seguidores (${state.followers.size})") }
                )
                Tab(
                    selected = state.selectedTab == 2,
                    onClick = { viewModel.setTab(2) },
                    text = { Text("Buscar") }
                )
            }

            when (state.selectedTab) {
                0 -> ProfileList(
                    profiles = state.following,
                    isLoading = state.isLoading,
                    onProfileClick = onOpenProfile,
                    emptyMessage = "Aún no sigues a nadie"
                )
                1 -> ProfileList(
                    profiles = state.followers,
                    isLoading = state.isLoading,
                    onProfileClick = onOpenProfile,
                    emptyMessage = "Aún no tienes seguidores"
                )
                2 -> {
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = { viewModel.search(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        placeholder = { Text("Buscar usuarios...") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        singleLine = true
                    )
                    ProfileList(
                        profiles = state.searchResults,
                        isLoading = false,
                        onProfileClick = onOpenProfile,
                        emptyMessage = if (state.searchQuery.length < 2)
                            "Escribe al menos 2 caracteres"
                        else
                            "No se encontraron usuarios"
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileList(
    profiles: List<Profile>,
    isLoading: Boolean,
    onProfileClick: (String) -> Unit,
    emptyMessage: String,
) {
    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (profiles.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(emptyMessage, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    LazyColumn {
        items(profiles, key = { it.id }) { profile ->
            ProfileListItem(profile = profile, onClick = { onProfileClick(profile.id) })
            HorizontalDivider()
        }
    }
}

@Composable
fun ProfileListItem(profile: Profile, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = {
            Text(
                profile.displayName ?: profile.username ?: "Usuario",
                fontWeight = FontWeight.Medium
            )
        },
        supportingContent = {
            if (!profile.username.isNullOrBlank()) {
                Text("@${profile.username}")
            }
        },
        leadingContent = {
            if (!profile.avatarUrl.isNullOrBlank()) {
                AsyncImage(
                    model = profile.avatarUrl,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .then(Modifier),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                (profile.displayName ?: "?").take(1).uppercase(),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }
    )
}
