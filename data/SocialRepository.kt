package com.pokke.music.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repositorio social que se conecta a Kokoro Supabase.
 * Usa las tablas ya existentes: profiles, profile_follows, conversations, messages, etc.
 */
class SocialRepository {

    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = SupabaseConfig.SUPABASE_URL,
            supabaseKey = SupabaseConfig.SUPABASE_ANON_KEY
        ) {
            install(Auth)
            install(Postgrest)
            install(Realtime)
            install(Storage)
        }
    }

    // ---------- PROFILES ----------

    suspend fun getProfile(userId: String): Profile? = withContext(Dispatchers.IO) {
        try {
            client.from("profiles")
                .select { filter { eq("id", userId) } }
                .decodeSingleOrNull<Profile>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getProfileByUsername(username: String): Profile? = withContext(Dispatchers.IO) {
        try {
            client.from("profiles")
                .select { filter { eq("username", username) } }
                .decodeSingleOrNull<Profile>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun updateProfile(
        userId: String,
        displayName: String? = null,
        bio: String? = null,
        username: String? = null,
        avatarUrl: String? = null,
        bannerUrl: String? = null,
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val updates = buildMap {
                displayName?.let { put("display_name", it) }
                bio?.let { put("bio", it) }
                username?.let { put("username", it) }
                avatarUrl?.let { put("avatar_url", it) }
                bannerUrl?.let { put("banner_url", it) }
            }
            if (updates.isEmpty()) return@withContext true
            client.from("profiles").update(updates) {
                filter { eq("id", userId) }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun searchProfiles(query: String, limit: Int = 20): List<Profile> = withContext(Dispatchers.IO) {
        try {
            client.from("profiles")
                .select {
                    filter {
                        or {
                            ilike("username", "%$query%")
                            ilike("display_name", "%$query%")
                        }
                    }
                    limit(limit.toLong())
                }
                .decodeList<Profile>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // ---------- FOLLOWS (Amigos) ----------

    suspend fun follow(followerId: String, followingId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            client.from("profile_follows").insert(
                mapOf(
                    "follower_id" to followerId,
                    "following_id" to followingId
                )
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun unfollow(followerId: String, followingId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            client.from("profile_follows").delete {
                filter {
                    eq("follower_id", followerId)
                    eq("following_id", followingId)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getFollowers(userId: String): List<Profile> = withContext(Dispatchers.IO) {
        try {
            // Primero obtenemos los IDs de followers
            val follows = client.from("profile_follows")
                .select { filter { eq("following_id", userId) } }
                .decodeList<ProfileFollow>()
            if (follows.isEmpty()) return@withContext emptyList()
            val ids = follows.map { it.followerId }
            client.from("profiles")
                .select { filter { isIn("id", ids) } }
                .decodeList<Profile>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getFollowing(userId: String): List<Profile> = withContext(Dispatchers.IO) {
        try {
            val follows = client.from("profile_follows")
                .select { filter { eq("follower_id", userId) } }
                .decodeList<ProfileFollow>()
            if (follows.isEmpty()) return@withContext emptyList()
            val ids = follows.map { it.followingId }
            client.from("profiles")
                .select { filter { isIn("id", ids) } }
                .decodeList<Profile>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun isFollowing(followerId: String, followingId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val result = client.from("profile_follows")
                .select {
                    filter {
                        eq("follower_id", followerId)
                        eq("following_id", followingId)
                    }
                }
                .decodeList<ProfileFollow>()
            result.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    // ---------- CHATS ----------

    suspend fun getConversationsForUser(nickname: String): List<Conversation> = withContext(Dispatchers.IO) {
        try {
            val members = client.from("conversation_members")
                .select { filter { eq("user_nickname", nickname) } }
                .decodeList<ConversationMember>()
            if (members.isEmpty()) return@withContext emptyList()
            val ids = members.map { it.conversationId }
            client.from("conversations")
                .select { filter { isIn("id", ids) } }
                .decodeList<Conversation>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getMessages(conversationId: String, limit: Int = 50): List<Message> = withContext(Dispatchers.IO) {
        try {
            client.from("messages")
                .select {
                    filter { eq("conversation_id", conversationId) }
                    order("created_at", ascending = true)
                    limit(limit.toLong())
                }
                .decodeList<Message>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun sendMessage(conversationId: String, senderNickname: String, body: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                client.from("messages").insert(
                    mapOf(
                        "conversation_id" to conversationId,
                        "sender_nickname" to senderNickname,
                        "body" to body
                    )
                )
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

    suspend fun createDirectConversation(myNickname: String, otherNickname: String): String? =
        withContext(Dispatchers.IO) {
            try {
                val conv = client.from("conversations").insert(
                    mapOf(
                        "is_group" to false,
                        "title" to "$myNickname & $otherNickname",
                        "created_by" to myNickname
                    )
                ) {
                    select()
                }.decodeSingle<Conversation>()

                client.from("conversation_members").insert(
                    listOf(
                        mapOf("conversation_id" to conv.id, "user_nickname" to myNickname, "role" to "member"),
                        mapOf("conversation_id" to conv.id, "user_nickname" to otherNickname, "role" to "member")
                    )
                )
                conv.id
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
}
