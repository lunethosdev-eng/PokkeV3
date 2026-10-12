package com.pokke.music.ui.screen.social

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.pokke.music.viewmodel.ProfileViewModel

/**
 * Pantalla de Perfil de Pokke
 * Muestra banner, avatar, bio, contadores de seguidores y botones de seguir/editar.
 * Usa el mismo estilo visual de SimpMusic (Material3 + Compose).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userId: String,
    currentUserId: String?,
    viewModel: ProfileViewModel,
    onBack: () -> Unit,
    onOpenFriends: () -> Unit,
    onOpenChat: (String) -> Unit, // username
) {
    val state by viewModel.uiState.collectAsState()
    val isOwnProfile = currentUserId != null && currentUserId == userId

    LaunchedEffect(userId) {
        viewModel.loadProfile(userId, currentUserId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.profile?.username ?: "Perfil") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (isOwnProfile) {
                        IconButton(onClick = { viewModel.setEditing(!state.isEditing) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar")
                        }
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

        val profile = state.profile
        if (profile == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(state.error ?: "Perfil no encontrado")
            }
            return@Scaffold
        }

        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Banner
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                if (!profile.bannerUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = profile.bannerUrl,
                        contentDescription = "Banner",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF7C4DFF), Color(0xFF448AFF))
                                )
                            )
                    )
                }
            }

            // Avatar + info
            Column(
                Modifier
                    .fillMaxWidth()
                    .offset(y = (-40).dp)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar
                Box {
                    if (!profile.avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = profile.avatarUrl,
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                (profile.displayName ?: profile.username ?: "?").take(1).uppercase(),
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = profile.displayName ?: profile.username ?: "Usuario",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                if (!profile.username.isNullOrBlank()) {
                    Text(
                        text = "@${profile.username}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (!profile.bio.isNullOrBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = profile.bio,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Contadores
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(profile.followersCount.toString(), "Seguidores") {
                        onOpenFriends()
                    }
                    StatItem(profile.followingCount.toString(), "Siguiendo") {
                        onOpenFriends()
                    }
                    StatItem(profile.visitsCount.toString(), "Visitas")
                }

                Spacer(Modifier.height(20.dp))

                // Botones de acción
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
                ) {
                    if (isOwnProfile) {
                        OutlinedButton(onClick = { viewModel.setEditing(true) }) {
                            Text("Editar perfil")
                        }
                    } else {
                        Button(
                            onClick = {
                                currentUserId?.let {
                                    viewModel.toggleFollow(it, userId)
                                }
                            }
                        ) {
                            Icon(
                                if (state.isFollowing) Icons.Default.PersonRemove else Icons.Default.PersonAdd,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(if (state.isFollowing) "Dejar de seguir" else "Seguir")
                        }

                        if (profile.allowMessages && !profile.username.isNullOrBlank()) {
                            OutlinedButton(onClick = { onOpenChat(profile.username) }) {
                                Text("Mensaje")
                            }
                        }
                    }
                }
            }

            // Sección de listas públicas (placeholder)
            if (profile.showPlaylists || profile.showFavorites) {
                Spacer(Modifier.height(24.dp))
                Text(
                    "Actividad musical",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                // Aquí se pueden conectar las playlists/favorites del propio SimpMusic
            }
        }
    }
}

@Composable
private fun StatItem(value: String, label: String, onClick: (() -> Unit)? = null) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = if (onClick != null) Modifier else Modifier
    ) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
