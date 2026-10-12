package com.pokke.music.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String,
    @SerialName("display_name") val displayName: String? = null,
    val username: String? = null,
    val bio: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("banner_url") val bannerUrl: String? = null,
    @SerialName("is_public") val isPublic: Boolean = true,
    @SerialName("show_listening") val showListening: Boolean = true,
    @SerialName("show_playlists") val showPlaylists: Boolean = true,
    @SerialName("show_favorites") val showFavorites: Boolean = true,
    @SerialName("allow_messages") val allowMessages: Boolean = true,
    @SerialName("followers_count") val followersCount: Int = 0,
    @SerialName("following_count") val followingCount: Int = 0,
    @SerialName("is_artist") val isArtist: Boolean = false,
    val badge: String? = "none",
    @SerialName("visits_count") val visitsCount: Int = 0,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class ProfileFollow(
    @SerialName("follower_id") val followerId: String,
    @SerialName("following_id") val followingId: String,
    @SerialName("created_at") val createdAt: String? = null,
)

@Serializable
data class Conversation(
    val id: String,
    @SerialName("is_group") val isGroup: Boolean = false,
    val title: String? = null,
    @SerialName("wallpaper_url") val wallpaperUrl: String? = null,
    @SerialName("created_by") val createdBy: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class Message(
    val id: String,
    @SerialName("conversation_id") val conversationId: String? = null,
    @SerialName("sender_nickname") val senderNickname: String,
    val body: String,
    @SerialName("created_at") val createdAt: String? = null,
)

@Serializable
data class MessageRequest(
    val id: String,
    @SerialName("from_nickname") val fromNickname: String,
    @SerialName("to_nickname") val toNickname: String,
    val body: String? = null,
    val status: String = "pending",
    @SerialName("created_at") val createdAt: String? = null,
)

@Serializable
data class ConversationMember(
    @SerialName("conversation_id") val conversationId: String,
    @SerialName("user_nickname") val userNickname: String,
    val role: String = "member",
    @SerialName("joined_at") val joinedAt: String? = null,
)
